#include "negeditor.h"

#include "../../resources/layers/neglayer.h"

#include "../fields/labelfield.h"
#include "../fields/pointfield.h"

NegEditor::NegEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    NegLayer *nl = dynamic_cast<NegLayer *>(l);

    // Set up fields widgets and add them to layout
    PointField *f_cc = new PointField("cc", nl->cc(), true, this);
    m_layout->addWidget(f_cc);

    // Add connections for fields
    connect(f_cc, &PointField::fieldChanged, [nl, f_cc, this](){nl->setCc(f_cc->getValue()); this->onFieldChanged();});

    // Read only fields
    LabelField *f_label1 = new LabelField("<b>Note:</b> this values are read only", this);
    m_layout->addWidget(f_label1);
    PointField *f_tl = new PointField("topLeft", nl->tl(), true, this);
    f_tl->setReadOnly(true);
    m_layout->addWidget(f_tl);
    PointField *f_br = new PointField("botRight", nl->br(), true, this);
    m_layout->addWidget(f_br);
    f_br->setReadOnly(true);
    PointField *f_oc = new PointField("objCenter", nl->oc(), true, this);
    m_layout->addWidget(f_oc);
    f_oc->setReadOnly(true);

    // I hadn't seen any resource with 'ep' vector, so there is no editor for this field
}
