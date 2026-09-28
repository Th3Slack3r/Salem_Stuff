import os
import glob
import shutil
import re
import requests
import sys
import ctypes
import tkinter as tk
from tkinter import ttk, messagebox
from PIL import Image, ImageTk
from io import BytesIO

# --- WINDOWS DARK MODE API ---
def set_dark_title_bar(window):
    window.update()
    DWMWA_USE_IMMERSIVE_DARK_MODE = 20
    set_browser_dark_mode = ctypes.windll.dwmapi.DwmSetWindowAttribute
    hwnd = ctypes.windll.user32.GetParent(window.winfo_id())
    rendering_policy = ctypes.c_int(1)
    set_browser_dark_mode(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, ctypes.byref(rendering_policy), ctypes.sizeof(rendering_policy))

# --- PATH HANDLING ---
if getattr(sys, 'frozen', False):
    BASE_PATH = os.path.dirname(sys.executable)
else:
    BASE_PATH = os.path.dirname(os.path.abspath(__file__))

API_URL = "http://2.56.246.128:30131/skins/api.php"
BASE_ASSET_URL = "http://2.56.246.128:30131/skins/"
LOCAL_SKINS_DIR = os.path.join(BASE_PATH, "my_local_skins")

CATEGORY_MAP = {
    "flags": r"gfx\terobjs\flags",
    "clothes": r"gfx\avatars",
}

# --- THEME COLORS ---
BG_MAIN = "#121212"
BG_SIDEBAR = "#181818"
BG_TILE = "#202020"
ACCENT = "#1DB954"
ACCENT_BLUE = "#0078D4"
TEXT_MAIN = "#FFFFFF"
TEXT_DIM = "#B3B3B3"
SCROLL_THUMB = "#333333"

class SkinApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Salem Skin Manager")
        self.geometry("1100x800")
        self.configure(bg=BG_MAIN)
        
        try: set_dark_title_bar(self)
        except: pass

        if not os.path.exists(LOCAL_SKINS_DIR):
            os.makedirs(LOCAL_SKINS_DIR)

        self.server_data = {}
        self.active_category_label = None
        self.setup_styles()
        self.setup_ui()
        self.fetch_server_data()

    def setup_styles(self):
        self.style = ttk.Style()
        self.style.theme_use('clam')
        self.style.configure("Vertical.TScrollbar", gripcount=0, background=SCROLL_THUMB, 
                             darkcolor=BG_MAIN, lightcolor=BG_MAIN, troughcolor=BG_MAIN, 
                             bordercolor=BG_MAIN, arrowsize=1)
        self.style.map("Vertical.TScrollbar", background=[('active', '#444444')])

    def setup_ui(self):
        self.sidebar = tk.Frame(self, width=220, bg=BG_SIDEBAR)
        self.sidebar.pack(side="left", fill="y")
        self.sidebar.pack_propagate(False)

        tk.Label(self.sidebar, text="CATEGORIES", bg=BG_SIDEBAR, fg=ACCENT, 
                 font=("Segoe UI", 10, "bold")).pack(pady=(30, 10), padx=20, anchor="w")

        self.btn_back = tk.Button(self.sidebar, text="← Back to Root", bg="#252525", fg="white", 
                                 relief="flat", command=self.render_root_categories, 
                                 font=("Segoe UI", 8), cursor="hand2")
        self.btn_back.pack(fill="x", padx=20, pady=5)
        self.btn_back.pack_forget()

        self.cat_container = tk.Frame(self.sidebar, bg=BG_SIDEBAR)
        self.cat_container.pack(fill="both", expand=True)

        self.main_container = tk.Frame(self, bg=BG_MAIN)
        self.main_container.pack(side="right", fill="both", expand=True)

        self.canvas = tk.Canvas(self.main_container, bg=BG_MAIN, highlightthickness=0)
        self.scroll = ttk.Scrollbar(self.main_container, orient="vertical", 
                                    command=self.canvas.yview, style="Vertical.TScrollbar")
        
        # This frame holds the tiles
        self.gallery = tk.Frame(self.canvas, bg=BG_MAIN)
        
        # Reset the scrollregion whenever the gallery size changes
        self.gallery.bind("<Configure>", lambda e: self.canvas.configure(scrollregion=self.canvas.bbox("all")))

        self.canvas_window = self.canvas.create_window((0, 0), window=self.gallery, anchor="nw")
        self.canvas.configure(yscrollcommand=self.scroll.set)
        
        self.scroll.pack(side="right", fill="y")
        self.canvas.pack(side="left", fill="both", expand=True, padx=20, pady=20)

        # Better mousewheel scrolling (Windows specific)
        self.canvas.bind_all("<MouseWheel>", self._on_mousewheel)

    def _on_mousewheel(self, event):
        # Only scroll if the content is larger than the canvas
        if self.canvas.bbox("all")[3] > self.canvas.winfo_height():
            self.canvas.yview_scroll(int(-1*(event.delta/120)), "units")

    def fetch_server_data(self):
        try:
            response = requests.get(API_URL, timeout=10)
            self.server_data = response.json()
            self.render_root_categories()
        except Exception as e:
            messagebox.showerror("Error", f"Server connection failed: {e}")

    def render_root_categories(self):
        self.btn_back.pack_forget()
        self.render_sidebar_level(self.server_data)

    def render_sidebar_level(self, data_subset, parent_path=""):
        for widget in self.cat_container.winfo_children(): widget.destroy()
        for key, value in data_subset.items():
            is_folder = isinstance(value, dict)
            display_text = f"  📁 {key.upper()}" if is_folder else f"  {key.upper()}"
            lbl = tk.Label(self.cat_container, text=display_text, bg=BG_SIDEBAR, fg=TEXT_DIM, 
                           font=("Segoe UI", 10), padx=10, pady=10, anchor="w", cursor="hand2")
            lbl.pack(fill="x")
            cp = f"{parent_path}/{key}".strip("/")
            if is_folder: lbl.bind("<Button-1>", lambda e, v=value, p=cp: self.enter_folder(v, p))
            else: lbl.bind("<Button-1>", lambda e, v=value, p=cp, l=lbl: self.select_category(v, p, l))

    def enter_folder(self, subset, path):
        self.btn_back.pack(fill="x", padx=20, pady=5)
        self.render_sidebar_level(subset, path)
        self.clear_gallery()

    def select_category(self, skins, path, label):
        if self.active_category_label: self.active_category_label.config(fg=TEXT_DIM, bg=BG_SIDEBAR)
        label.config(fg=TEXT_MAIN, bg="#333")
        self.active_category_label = label
        self.clear_gallery()
        cols = 4
        for i, img_name in enumerate(skins):
            self.create_tile(path, img_name, i // cols, i % cols)
        
        # Jump scroll back to top
        self.canvas.yview_moveto(0)

    def clear_gallery(self):
        for widget in self.gallery.winfo_children():
            widget.destroy()
        self.canvas.configure(scrollregion=(0,0,0,0)) # Reset scroll area

    def create_tile(self, category_path, img_name, row, col):
        tile = tk.Frame(self.gallery, bg=BG_TILE, padx=15, pady=15, highlightthickness=1, highlightbackground="#333")
        tile.grid(row=row, column=col, padx=12, pady=12)
        base_name = os.path.splitext(img_name)[0]
        local_dir = os.path.join(LOCAL_SKINS_DIR, category_path.replace("/", os.sep))
        local_path = os.path.join(local_dir, f"{base_name}.cached")

        try:
            resp = requests.get(f"{BASE_ASSET_URL}{category_path}/{img_name}", timeout=5)
            img_data = Image.open(BytesIO(resp.content))
            img_data.thumbnail((160, 160))
            photo = ImageTk.PhotoImage(img_data)
            lbl_img = tk.Label(tile, image=photo, bg=BG_TILE)
            lbl_img.image = photo
            lbl_img.pack(pady=(0, 10))
        except:
            tk.Label(tile, text="No Preview", bg=BG_TILE, fg="#555").pack(pady=40)

        tk.Label(tile, text=base_name.capitalize(), bg=BG_TILE, fg=TEXT_MAIN, font=("Segoe UI", 9, "bold")).pack()
        btn_container = tk.Frame(tile, bg=BG_TILE)
        btn_container.pack(fill="x", pady=(15, 0))

        def refresh_btns():
            for w in btn_container.winfo_children(): w.destroy()
            if not os.path.exists(local_path):
                tk.Button(btn_container, text="Download", bg=ACCENT_BLUE, fg=TEXT_MAIN, relief="flat", font=("Segoe UI", 9, "bold"), pady=6, cursor="hand2", command=lambda: [self.download_skin(category_path, base_name), refresh_btns()]).pack(fill="x")
            else:
                tk.Button(btn_container, text="Install Skin", bg=ACCENT, fg=TEXT_MAIN, relief="flat", font=("Segoe UI", 9, "bold"), pady=6, cursor="hand2", command=lambda: self.install_skin(category_path, base_name)).pack(fill="x")
        refresh_btns()

    def download_skin(self, category_path, base_name):
        target_dir = os.path.join(LOCAL_SKINS_DIR, category_path.replace("/", os.sep))
        if not os.path.exists(target_dir): os.makedirs(target_dir)
        try:
            r_skin = requests.get(f"{BASE_ASSET_URL}{category_path}/{base_name}.cached")
            with open(os.path.join(target_dir, f"{base_name}.cached"), "wb") as f: f.write(r_skin.content)
            r_conf = requests.get(f"{BASE_ASSET_URL}{category_path}/.{base_name}")
            if r_conf.status_code == 200:
                with open(os.path.join(target_dir, f".{base_name}"), "wb") as f: f.write(r_conf.content)
        except Exception as e: messagebox.showerror("Error", f"Download failed: {e}")

    def install_skin(self, category_path, base_name):
        user = os.getlogin()
        cache_root = rf"C:\Users\{user}\AppData\Roaming\Salem\cache"
        folders = [f for f in glob.glob(os.path.join(cache_root, "*")) if os.path.isdir(f)]
        if not folders: return messagebox.showerror("Error", "Salem Cache not found.")
        local_dir = os.path.join(LOCAL_SKINS_DIR, category_path.replace("/", os.sep))
        config_path = os.path.join(local_dir, f".{base_name}")
        try:
            with open(config_path, 'r') as f:
                target_filename = re.search(r'obj=(.*)', f.read()).group(1).strip()
            root_cat = category_path.split('/')[0]
            game_base_path = CATEGORY_MAP.get(root_cat.lower(), "")
            full_game_subpath = os.path.join(game_base_path, *category_path.split('/')[1:])
            dest_dir = os.path.join(folders[0], "res", full_game_subpath)
            if not os.path.exists(dest_dir): os.makedirs(dest_dir, exist_ok=True)
            shutil.copy2(os.path.join(local_dir, f"{base_name}.cached"), os.path.join(dest_dir, target_filename))
            messagebox.showinfo("Success", f"Installed {base_name.capitalize()}!")
        except Exception as e: messagebox.showerror("Error", f"Installation failed: {e}")

if __name__ == "__main__":
    app = SkinApp()
    app.mainloop()