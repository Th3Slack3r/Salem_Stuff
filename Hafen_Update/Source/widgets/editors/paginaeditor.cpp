#include "paginaeditor.h"

#include "../../resources/layers/paginalayer.h"

#include "../fields/textfield.h"

PaginaEditor::PaginaEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    PaginaLayer *nl = dynamic_cast<PaginaLayer *>(l);

    // Set up fields widgets and add them to layout
    TextField *f_text = new TextField("text", nl->text(), this);
    m_layout->addWidget(f_text);

    // Add connections for fields
    connect(f_text, &TextField::fieldChanged, [nl, f_text, this](){nl->setText(f_text->getValue()); this->onFieldChanged();});
}
