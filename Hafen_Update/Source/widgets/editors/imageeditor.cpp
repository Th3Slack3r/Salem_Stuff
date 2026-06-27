#include "imageeditor.h"

#include "../../resources/layers/imagelayer.h"

#include "../fields/intfield.h"
#include "../fields/pointfield.h"
#include "../fields/booleanfield.h"
#include "../fields/pixmapfield.h"

ImageEditor::ImageEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    ImageLayer *nl = dynamic_cast<ImageLayer *>(l);

    // Set up fields widgets and add them to layout
    // z (short)
    IntField *f_z = new IntField("z", nl->z(), this, -32768, 32767);
    m_layout->addWidget(f_z);
    // subz (short)
    IntField *f_subz = new IntField("subz", nl->subz(), this, -32768, 32767);
    m_layout->addWidget(f_subz);
    // id (short)
    IntField *f_id = new IntField("id", nl->id(), this, -32768, 32767);
    m_layout->addWidget(f_id);
    // o (coord16)
    PointField *f_o = new PointField("o", nl->o(), true, this);
    m_layout->addWidget(f_o);
    // nooff
    BooleanField *f_nooff = new BooleanField("nooff", nl->nooff(), this);
    m_layout->addWidget(f_nooff);
    // image
    PixmapField *f_image = new PixmapField("image", nl->image(), this);
    m_layout->addWidget(f_image);

    // Add connections for fields
    connect(f_z, &IntField::fieldChanged, [nl, f_z, this](){nl->setZ(f_z->getValue()); this->onFieldChanged();});
    connect(f_subz, &IntField::fieldChanged, [nl, f_subz, this](){nl->setSubz(f_subz->getValue()); this->onFieldChanged();});
    connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    connect(f_o, &IntField::fieldChanged, [nl, f_o, this](){nl->setO(f_o->getValue()); this->onFieldChanged();});
    connect(f_nooff, &BooleanField::fieldChanged, [nl, f_nooff, this](){nl->setNooff(f_nooff->getValue()); this->onFieldChanged();});
    connect(f_image, &PixmapField::fieldChanged, [nl, f_image, this](){nl->setImage(f_image->getValue()); this->onFieldChanged();});
}
