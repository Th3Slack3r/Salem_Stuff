#include "codeeditor.h"

#include "../../resources/layers/codelayer.h"
#include "../../tools/decompiler.h"

#include "../fields/stringfield.h"
#include "../fields/textfield.h"

CodeEditor::CodeEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    CodeLayer *nl = dynamic_cast<CodeLayer *>(l);

    // Set up fields widgets and add them to layout
    // name
    StringField *f_name = new StringField("name", nl->name(), this);
    f_name->setReadOnly(true);
    m_layout->addWidget(f_name);
    // code
    TextField *f_code = new TextField("code", "", this);
    f_code->setReadOnly(true);
    m_layout->addWidget(f_code);
    Decompiler *d = new Decompiler(nl->name(), nl->data(), this);
    connect(d, &Decompiler::codeDecompiled, f_code, &TextField::setValue);
    d->decompile();

    // No connections due to all fields are read only
}
