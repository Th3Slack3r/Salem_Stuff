#include "tooltipeditor.h"

#include "../../resources/layers/tooltiplayer.h"

#include "../fields/stringfield.h"

TooltipEditor::TooltipEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    TooltipLayer *nl = dynamic_cast<TooltipLayer *>(l);

    // Set up fields widgets and add them to layout
    StringField *f_t = new StringField("t", nl->t(), this);
    m_layout->addWidget(f_t);

    // Add connections for fields
    connect(f_t, &StringField::fieldChanged, [nl, f_t, this](){nl->setT(f_t->getValue()); this->onFieldChanged();});
}
