import sys
import os
import zipfile
import re
from PyQt5.QtCore import Qt, QThread, pyqtSignal
from PyQt5.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QSplitter, QTreeWidget, QTreeWidgetItem, QLineEdit, QPushButton,
    QTextEdit, QLabel, QFileDialog, QMessageBox, QComboBox, QFrame
)
from PyQt5.QtGui import QTextCursor, QTextCharFormat, QColor, QSyntaxHighlighter

# -------------------------
# Java syntax highlighter
# -------------------------
class JavaHighlighter(QSyntaxHighlighter):
    def __init__(self, document):
        super().__init__(document)
        self.highlight_rules = []

        keywords = [
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "if", "goto", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while"
        ]
        kw_format = QTextCharFormat()
        kw_format.setForeground(QColor("#569CD6"))
        for kw in keywords:
            self.highlight_rules.append((re.compile(rf"\b{kw}\b"), kw_format))

        # Strings
        str_format = QTextCharFormat()
        str_format.setForeground(QColor("#D69D85"))
        self.highlight_rules.append((re.compile(r'"[^"\\]*(\\.[^"\\]*)*"'), str_format))
        self.highlight_rules.append((re.compile(r"'[^'\\]*(\\.[^'\\]*)*'"), str_format))

        # Comments
        comment_format = QTextCharFormat()
        comment_format.setForeground(QColor("#6A9955"))
        self.highlight_rules.append((re.compile(r"//[^\n]*"), comment_format))
        self.highlight_rules.append((re.compile(r"/\*.*?\*/", re.DOTALL), comment_format))

    def highlightBlock(self, text):
        for pattern, fmt in self.highlight_rules:
            for match in pattern.finditer(text):
                start, end = match.span()
                self.setFormat(start, end - start, fmt)

# -------------------------
# Code viewer with header
# -------------------------
class CodeViewer(QWidget):
    def __init__(self):
        super().__init__()
        layout = QVBoxLayout(self)
        layout.setContentsMargins(0,0,0,0)
        layout.setSpacing(0)

        # header
        self.file_label = QLabel("No file loaded")
        self.file_label.setStyleSheet(
            "background-color:#2d2d2d; color:#cccccc; padding:6px; font-family: monospace;"
        )
        layout.addWidget(self.file_label)

        # separator
        line = QFrame()
        line.setFrameShape(QFrame.HLine)
        line.setStyleSheet("color:#444444;")
        layout.addWidget(line)

        # code area
        self.text = QTextEdit()
        self.text.setReadOnly(True)
        self.text.setLineWrapMode(QTextEdit.NoWrap)
        self.text.setStyleSheet("background-color:#1e1e1e; color:#ffffff; font-family: monospace;")
        layout.addWidget(self.text)

        self.highlighter = JavaHighlighter(self.text.document())

    def set_code(self, file_path, text, highlight_term=None, line_number=None):
        self.file_label.setText(f"{os.path.basename(file_path)} — {file_path}")
        self.text.setPlainText(text)

        # move cursor to line
        if line_number:
            cursor = self.text.textCursor()
            cursor.movePosition(QTextCursor.Start)
            for _ in range(line_number-1):
                cursor.movePosition(QTextCursor.Down)
            self.text.setTextCursor(cursor)
            self.text.ensureCursorVisible()

        # highlight term
        if highlight_term:
            self.highlight_term(highlight_term)

    def highlight_term(self, term):
        cursor = self.text.textCursor()
        cursor.movePosition(QTextCursor.Start)
        fmt = QTextCharFormat()
        fmt.setBackground(QColor("yellow"))
        fmt.setForeground(QColor("black"))

        while True:
            cursor = self.text.document().find(term, cursor)
            if cursor.isNull():
                break
            cursor.mergeCharFormat(fmt)

# -------------------------
# Search workers
# -------------------------
class FolderSearchWorker(QThread):
    found_match = pyqtSignal(str,int,str,str)
    search_done = pyqtSignal()
    def __init__(self, folder_path, search_term):
        super().__init__()
        self.folder_path = folder_path
        self.search_term = search_term.lower()
        self.running = True
    def run(self):
        for root, _, files in os.walk(self.folder_path):
            if not self.running:
                break
            for file in files:
                if not self.running:
                    break
                if file.endswith(".java"):
                    fp = os.path.join(root, file)
                    try:
                        with open(fp, 'r', encoding='utf-8') as f:
                            for i,line in enumerate(f,1):
                                if self.search_term in line.lower():
                                    self.found_match.emit(fp,i,self.search_term,line.strip())
                    except:
                        pass
        self.search_done.emit()
    def stop(self):
        self.running=False

class ZipSearchWorker(QThread):
    found_match = pyqtSignal(str,int,str,str)
    search_done = pyqtSignal()
    def __init__(self, zip_path, search_term):
        super().__init__()
        self.zip_path = zip_path
        self.search_term = search_term.lower()
        self.running=True
    def run(self):
        try:
            with zipfile.ZipFile(self.zip_path,'r') as z:
                for name in z.namelist():
                    if not self.running: break
                    if name.endswith(".java"):
                        try:
                            with z.open(name) as f:
                                content = f.read().decode('utf-8',errors='ignore').splitlines()
                                for i,line in enumerate(content,1):
                                    if self.search_term in line.lower():
                                        self.found_match.emit(name,i,self.search_term,line.strip())
                        except: pass
        finally:
            self.search_done.emit()
    def stop(self):
        self.running=False

# -------------------------
# Main app
# -------------------------
class CodeSearchApp(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Salem Search")
        self.resize(1200,800)

        self.worker = None
        self.matches = []
        self.mode="Folder"
        self.source_path=None
        self.parent_items={}

        layout = QVBoxLayout(self)

        # Toolbar
        toolbar = QHBoxLayout()
        self.mode_select = QComboBox()
        self.mode_select.addItems(["Folder","ZIP"])
        self.mode_select.currentTextChanged.connect(self.change_mode)
        self.path_display = QLineEdit()
        self.path_display.setReadOnly(True)
        self.select_btn = QPushButton("Select Source")
        self.select_btn.clicked.connect(self.select_codebase)
        toolbar.addWidget(self.mode_select)
        toolbar.addWidget(self.path_display,stretch=1)
        toolbar.addWidget(self.select_btn)
        layout.addLayout(toolbar)

        # Search
        search_layout = QHBoxLayout()
        self.search_input = QLineEdit()
        self.search_input.setPlaceholderText("Enter search term...")
        self.search_input.returnPressed.connect(self.perform_search)
        self.search_btn = QPushButton("Search")
        self.search_btn.clicked.connect(self.perform_search)
        search_layout.addWidget(self.search_input)
        search_layout.addWidget(self.search_btn)
        layout.addLayout(search_layout)

        # Splitter results + viewer
        self.results_tree = QTreeWidget()
        self.results_tree.setHeaderHidden(True)
        self.results_tree.itemClicked.connect(self.on_result_item_clicked)

        self.code_viewer = CodeViewer()

        splitter = QSplitter(Qt.Vertical)
        splitter.addWidget(self.results_tree)
        splitter.addWidget(self.code_viewer)
        splitter.setSizes([300,500])
        layout.addWidget(splitter)

    def change_mode(self,text):
        self.mode=text
        self.path_display.clear()
        self.source_path=None
        self.results_tree.clear()
        self.parent_items.clear()
        self.matches.clear()

    def select_codebase(self):
        if self.mode=="Folder":
            path=QFileDialog.getExistingDirectory(self,"Select Folder")
        else:
            path,_=QFileDialog.getOpenFileName(self,"Select ZIP File","","ZIP Files (*.zip)")
        if path:
            self.source_path=path
            self.path_display.setText(path)

    def perform_search(self):
        term=self.search_input.text().strip()
        if not term:
            QMessageBox.warning(self,"Error","Please enter a search term.")
            return
        if not self.source_path:
            QMessageBox.warning(self,"Error",f"Please select a {self.mode.lower()}.")
            return

        self.results_tree.clear()
        self.parent_items.clear()
        self.matches.clear()
        self.search_btn.setEnabled(False)
        self.search_btn.setText("Searching...")

        if self.mode=="Folder":
            self.worker = FolderSearchWorker(self.source_path,term)
        else:
            self.worker = ZipSearchWorker(self.source_path,term)

        self.worker.found_match.connect(self.add_result)
        self.worker.search_done.connect(self.search_complete)
        self.worker.start()

    def add_result(self, file_id, line_number, term, preview):
        parent = self.parent_items.get(file_id)
        if parent is None:
            # Show relative path for the file instead of just basename
            if self.mode == "Folder":
                display_name = os.path.relpath(file_id, self.source_path)
            else:
                display_name = file_id  # for zip, file_id is already internal path

            parent = QTreeWidgetItem([display_name])
            parent.setExpanded(True)
            parent.setData(0, Qt.UserRole, (file_id, None))  # None line -> parent
            self.results_tree.addTopLevelItem(parent)
            self.parent_items[file_id] = parent

        # add child as before
        child_text = f"Line {line_number}: {preview}"
        child = QTreeWidgetItem([child_text])
        child.setData(0, Qt.UserRole, (file_id, line_number))
        parent.addChild(child)

        self.matches.append((file_id, line_number, term))


    def search_complete(self):
        self.search_btn.setEnabled(True)
        self.search_btn.setText("Search")
        if not self.matches:
            QMessageBox.information(self,"Done","No matches found.")

    def on_result_item_clicked(self,item,column):
        data=item.data(0,Qt.UserRole)
        if data:
            file_id,line_number=data
            if line_number is None:
                code_text=self._read_file_text(file_id)
                if code_text:
                    self.code_viewer.set_code(file_id,code_text)
            else:
                code_text=self._read_file_text(file_id)
                if code_text:
                    search_term=self.search_input.text().strip()
                    self.code_viewer.set_code(file_id,code_text,highlight_term=search_term,line_number=line_number)

    def _read_file_text(self,file_id):
        if self.mode=="Folder":
            try:
                with open(file_id,'r',encoding='utf-8') as f:
                    return f.read()
            except:
                try:
                    with open(file_id,'r',encoding='utf-8',errors='ignore') as f:
                        return f.read()
                except:
                    return None
        else:
            try:
                with zipfile.ZipFile(self.source_path,'r') as z:
                    with z.open(file_id) as f:
                        return f.read().decode('utf-8',errors='ignore')
            except:
                return None

# -------------------------
# Run
# -------------------------
if __name__ == "__main__":
    app = QApplication(sys.argv)
    app.setStyleSheet("""
QWidget {
    background-color: #2d2d2d;
    color: #cccccc;
    font-family: monospace;
}
QLineEdit, QComboBox, QTextEdit, QTreeWidget {
    background-color: #1e1e1e;
    color: #ffffff;
    border: 1px solid #555555;
}
QPushButton {
    background-color: #3c3c3c;
    color: #ffffff;
    border: 1px solid #555555;
    padding: 4px 8px;
}
QPushButton:hover {
    background-color: #505050;
}
QPushButton:pressed {
    background-color: #606060;
}
QTreeWidget {
    background-color: #1e1e1e;
    color: #ffffff;
}
QTreeWidget::item:selected {
    background-color: #264f78;
    color: #ffffff;
}
QHeaderView::section {
    background-color: #3c3c3c;
    color: #ffffff;
    border: none;
}
QSplitter::handle {
    background-color: #444444;
}
QLabel {
    color: #cccccc;
}
""")

    window = CodeSearchApp()   # create the main window
    window.show()              # show it
    sys.exit(app.exec_())      # start the Qt event loop