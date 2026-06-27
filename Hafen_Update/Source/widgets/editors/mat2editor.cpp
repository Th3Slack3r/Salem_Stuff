#include "mat2editor.h"

#include "../../resources/layers/mat2layer.h"

#include "../fields/intfield.h"
#include "../fields/variantlistfield.h"

Mat2Editor::Mat2Editor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    Mat2Layer *nl = dynamic_cast<Mat2Layer *>(l);

    // Set up fields widgets and add them to layout
    // id (short)
    IntField *f_id = new IntField("id", nl->id(), this, -32768, 32767);
    m_layout->addWidget(f_id);
    // keyargs
    VariantListField *f_keyargs = new VariantListField("keyargs", nl->keyargs(), this);
    m_layout->addWidget(f_keyargs);

    // Add connections for fields
    connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    connect(f_keyargs, &VariantListField::fieldChanged, [nl, f_keyargs, this](){nl->setKeyargs(f_keyargs->getValue()); this->onFieldChanged();});
}
