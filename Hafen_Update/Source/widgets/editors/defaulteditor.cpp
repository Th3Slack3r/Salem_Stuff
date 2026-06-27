#include "defaulteditor.h"

#include "../fields/defaultfield.h"

DefaultEditor::DefaultEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Set up fields widgets and add them to layout
    DefaultField *f_default = new DefaultField(
                QString("Editor for field %1 is not implemented yet (size: %2 bytes)")
                .arg(l->type()).arg(l->toByteArray().size()), this);
    m_layout->addWidget(f_default);
}
