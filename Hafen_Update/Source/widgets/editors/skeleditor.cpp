#include "skeleditor.h"

#include "../../resources/layers/skellayer.h"

#include "../fields/bonelistfield.h"

SkelEditor::SkelEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    SkelLayer *nl = dynamic_cast<SkelLayer *>(l);

    // Set up fields widgets and add them to layout
    // bones
    BoneListField *f_bones = new BoneListField("bones", nl->bones(), this);
    m_layout->addWidget(f_bones);

    // Add connections for fields
    connect(f_bones, &BoneListField::fieldChanged, [nl, f_bones, this](){nl->setBones(f_bones->getValue()); this->onFieldChanged();});
}
