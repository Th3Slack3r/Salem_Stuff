#ifndef SKELLAYER_H
#define SKELLAYER_H

#include "layer.h"
#include "../types/point3f.h"

class SkelLayer : public Layer
{
public:
    struct Bone {
        Point3F pos;
        Point3F rax;
        float rang;
        QString bp;
    };

    SkelLayer(const QByteArray &d, Resource *r = nullptr);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QList<QPair<QString, Bone>> bones() const {return m_bones;}
    void setBones(const QList<QPair<QString, Bone>> &b) {m_bones = b;}

private:
    QList<QPair<QString, Bone>> m_bones;

    bool hasBone(const QString& parent) const;

};

#endif // SKELLAYER_H
