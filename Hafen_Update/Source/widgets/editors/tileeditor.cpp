#include "tileeditor.h"

#include "../../resources/layers/tilelayer.h"

#include "../fields/intfield.h"
#include "../fields/pixmapfield.h"

TileEditor::TileEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    TileLayer *nl = dynamic_cast<TileLayer *>(l);

    IntField *f_t = new IntField("t", nl->t(), this, 0, 255);
    m_layout->addWidget(f_t);

    IntField *f_id = new IntField("id", nl->id(), this, 0, 255);
    m_layout->addWidget(f_id);

    IntField *f_w = new IntField("w", nl->w(), this, 0, 65535);
    m_layout->addWidget(f_w);

    PixmapField *f_img = new PixmapField("image", nl->image(), this);
    m_layout->addWidget(f_img);

    connect(f_t, &IntField::fieldChanged, [nl, f_t, this](){nl->setT(f_t->getValue()); this->onFieldChanged();});
    connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    connect(f_w, &IntField::fieldChanged, [nl, f_w, this](){nl->setW(f_w->getValue()); this->onFieldChanged();});
    connect(f_img, &PixmapField::fieldChanged, [nl, f_img, this](){nl->setImage(f_img->getValue()); this->onFieldChanged();});
}
