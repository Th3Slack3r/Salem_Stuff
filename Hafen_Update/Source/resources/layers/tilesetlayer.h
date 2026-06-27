#ifndef TILESETLAYER_H
#define TILESETLAYER_H

#include "layer.h"
#include "tileset2layer.h"

class TilesetLayer : public Layer
{
public:
    TilesetLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int fl() const { return m_fl; }
    int flavprob() const { return m_flavprob; }
    const QList<Tileset2Layer::Flavobj> flavobjs() const { return m_flavobjs; }

    void setFl(int v) { m_fl = v; }
    void setFlavprob(int v) { m_flavprob = v; }
    void setFlavobjs(const QList<Tileset2Layer::Flavobj> &v) { m_flavobjs = v; }

private:
    int m_fl;
    int m_flavprob;
    QList<Tileset2Layer::Flavobj> m_flavobjs;
};

#endif // TILESETLAYER_H
