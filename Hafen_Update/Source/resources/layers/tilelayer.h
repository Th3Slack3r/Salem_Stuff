#ifndef TILELAYER_H
#define TILELAYER_H

#include <QPixmap>

#include "layer.h"

class TileLayer : public Layer
{
public:
    TileLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int t() const { return m_t; }
    int id() const { return m_id; }
    int w() const { return m_w; }
    QPixmap image() const { return m_image; }

    void setT(int v) { m_t = v; }
    void setId(int v) { m_id = v; }
    void setW(int v) { m_w = v; }
    void setImage(const QPixmap &p) { m_image = p; }

private:
    int m_t;
    int m_id;
    int m_w;
    QPixmap m_image;
};

#endif // TILELAYER_H
