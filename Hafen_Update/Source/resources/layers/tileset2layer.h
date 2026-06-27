#ifndef TILESET2LAYER_H
#define TILESET2LAYER_H

#include "layer.h"

class Tileset2Layer : public Layer
{
public:
    struct Flavobj {
        QString fln;
        int flv;
        int flw;
    };

    Tileset2Layer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int p() const {return m_p;}
    const QString tn() const {return m_tn;}
    const VariantList ta() const {return m_ta;}
    int flavprob() const {return m_flavprob;}
    const QList<Flavobj> flavobjs() const {return m_flavobjs;}
    const QList<QString> tags() const {return m_tags;}

    void setTn(const QString &t) {m_tn = t;}
    void setTa(const VariantList &t) {m_ta = t;}
    void setFlavprob(int f) {m_flavprob = f;}
    void setFlavobjs(const QList<Flavobj> &f) {m_flavobjs = f;}
    void setTags(const QList<QString> &t) {m_tags = t;}

private:
    int m_p;
    QString m_tn;
    VariantList m_ta;
    int m_flavprob;
    QList<Flavobj> m_flavobjs;
    QList<QString> m_tags;
};

#endif // TILESET2LAYER_H
