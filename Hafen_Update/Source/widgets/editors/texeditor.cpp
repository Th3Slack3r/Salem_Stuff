#include "texeditor.h"

#include "../../resources/layers/texlayer.h"

#include "../fields/dropdownfield.h"
#include "../fields/intfield.h"
#include "../fields/pixmapfield.h"
#include "../fields/pointfield.h"

#include <QtCore/QBuffer>

TexEditor::TexEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    TexLayer *nl = dynamic_cast<TexLayer *>(l);
    QList<int> types = nl->types();

    // Set up fields widgets and add them to layout
    // id (short)
    IntField *f_id = new IntField("id", nl->id(), this, -32768, 32767);
    m_layout->addWidget(f_id);
    // off (coord16)
    PointField *f_off = new PointField("off", nl->off(), true, this);
    m_layout->addWidget(f_off);
    // sz (coord16)
    PointField *f_sz = new PointField("sz", nl->sz(), true, this);
    m_layout->addWidget(f_sz);
    // image
    if (types.contains(0)) {
        QPixmap image;
        image.loadFromData(nl->image(), nullptr);
        PixmapField *f_image = new PixmapField("img", image, this);
        m_layout->addWidget(f_image);
        connect(f_image, &PixmapField::fieldChanged, [nl, f_image, this](){
            QByteArray ba;
            QBuffer buffer(&ba);
            buffer.open(QIODevice::WriteOnly);
            f_image->getValue().save(&buffer, "PNG");
            buffer.close();
            nl->setImage(ba);
            this->onFieldChanged();
        });
    }
    // ma (5)
    if (types.contains(1)) {
        QList<QString> mipmap;
        mipmap << "Mipmapper.avg" << "Mipmapper.avg" << "Mipmapper.rnd" << "Mipmapper.cnt" << "Mipmapper.dav";
        DropdownField *f_ma = new DropdownField("ma", mipmap, nl->ma(), this);
        m_layout->addWidget(f_ma);
        connect(f_ma, &DropdownField::fieldChanged, [nl, f_ma, this](){nl->setMa(f_ma->getIndex()); this->onFieldChanged();});
    }
    // magf (2)
    if (types.contains(2)) {
        QList<QString> magf;
        magf << "GL.GL_NEAREST" << "GL.GL_LINEAR";
        DropdownField *f_magf = new DropdownField("magf", magf, nl->magf(), this);
        m_layout->addWidget(f_magf);
        connect(f_magf, &DropdownField::fieldChanged, [nl, f_magf, this](){nl->setMagf(f_magf->getIndex()); this->onFieldChanged();});
    }
    // minf (6)
    if (types.contains(3)) {
        QList<QString> minf;
        minf << "GL.GL_NEAREST" << "GL.GL_LINEAR" << "GL.GL_NEAREST_MIPMAP_NEAREST"
             << "GL.GL_NEAREST_MIPMAP_LINEAR" << "GL.GL_LINEAR_MIPMAP_NEAREST" << "GL.GL_LINEAR_MIPMAP_LINEAR";
        DropdownField *f_minf = new DropdownField("minf", minf, nl->minf(), this);
        m_layout->addWidget(f_minf);
        connect(f_minf, &DropdownField::fieldChanged, [nl, f_minf, this](){nl->setMinf(f_minf->getIndex()); this->onFieldChanged();});
    }
    // mask
    if (types.contains(4)) {
        QPixmap mask;
        mask.loadFromData(nl->mask(), "PNG");
        PixmapField *f_mask = new PixmapField("mask", mask, this);
        m_layout->addWidget(f_mask);
        connect(f_mask, &PixmapField::fieldChanged, [nl, f_mask, this](){
            QByteArray ba;
            QBuffer buffer(&ba);
            buffer.open(QIODevice::WriteOnly);
            f_mask->getValue().save(&buffer, "PNG");
            buffer.close();
            nl->setImage(ba);
            this->onFieldChanged();
        });
    }

    // Add connections for fields
    connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    connect(f_off, &PointField::fieldChanged, [nl, f_off, this](){nl->setOff(f_off->getValue()); this->onFieldChanged();});
    connect(f_sz, &PointField::fieldChanged, [nl, f_sz, this](){nl->setSz(f_sz->getValue()); this->onFieldChanged();});
}
