#include "srceditor.h"

#include "../../resources/layers/srclayer.h"

#include "../fields/stringfield.h"
#include "../fields/intfield.h"
#include "../fields/textfield.h"

SrcEditor::SrcEditor(Layer* l, QWidget* parent)
    : Editor(l, parent)
{
    // Cast layer
    SrcLayer *nl = dynamic_cast<SrcLayer *>(l);

    // Set up fields widgets and add them to layout
    // type
    IntField *f_type = new IntField("type", nl->srctype(), this);
    f_type->setReadOnly(true);
    m_layout->addWidget(f_type);
    // name
    StringField *f_name = new StringField("name", nl->name(), this);
    f_name->setReadOnly(true);
    m_layout->addWidget(f_name);
    // code
    TextField *f_code = new TextField("source", QString{nl->data()}, this);
    f_code->setReadOnly(true);
    m_layout->addWidget(f_code);

    // No connections due to all fields are read only
}
