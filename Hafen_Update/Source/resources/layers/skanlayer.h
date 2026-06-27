#ifndef SKANLAYER_H
#define SKANLAYER_H

#include "layer.h"

class SkanLayer : public Layer
{
public:
    struct Frame {
        float tm;
        float tx;
        float ty;
        float tz;
        float rang;
        float rax;
        float ray;
        float raz;
    };

    struct Bone {
        QString name;
        QList<Frame> frames;
    };

    SkanLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int id() const { return m_id; }
    int fl() const { return m_fl; }
    int mode() const { return m_mode; }
    float len() const { return m_len; }
    float nspeed() const { return m_nspeed; }
    const QList<Bone> bones() const { return m_bones; }

    void setId(int v) { m_id = v; }
    void setFl(int v) { m_fl = v; }
    void setMode(int v) { m_mode = v; }
    void setLen(float v) { m_len = v; }
    void setNspeed(float v) { m_nspeed = v; }

private:
    int m_id;
    int m_fl;
    int m_mode;
    float m_len;
    float m_nspeed;
    QList<Bone> m_bones;
};

#endif // SKANLAYER_H
