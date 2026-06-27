#ifndef IMAGELAYER_H
#define IMAGELAYER_H

#include <QPoint>
#include <QPixmap>

#include "layer.h"


class ImageLayer : public Layer
{
public:
    ImageLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int z() const {return m_z;}
    int subz() const {return m_subz;}
    int id() const {return m_id;}
    QPoint o() const {return m_o;}
    bool nooff() const {return m_nooff;}
    QPixmap image() const {return m_image;}

    void setZ(int z) {m_z = z;}
    void setSubz(int s) {m_subz = s;}
    void setId(int i) {m_id = i;}
    void setO(const QPoint &p) {m_o = p;}
    void setNooff(bool n) {m_nooff = n;}
    void setImage(const QPixmap &p) {m_image = p;}

private:
    int m_z;
    int m_subz;
    char m_flags;
    int m_id;
    QPoint m_o;
    bool m_nooff;
    QPixmap m_image;
    QMap<QString, QByteArray> m_keydata;
};

#endif // IMAGELAYER_H
