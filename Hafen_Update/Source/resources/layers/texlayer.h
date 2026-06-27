#pragma once

#include "layer.h"

class TexLayer : public Layer
{
public:
    TexLayer(const QByteArray& d, Resource* r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int id() const
    {
        return m_id;
    }
    int minf() const
    {
        return m_minf;
    }
    int magf() const
    {
        return m_magf;
    }
    int ma() const
    {
        return m_ma;
    }
    const QList<int> types() const
    {
        return m_types;
    }
    const QPoint off() const
    {
        return m_off;
    }
    const QPoint sz() const
    {
        return m_sz;
    }
    const QByteArray image() const
    {
        return m_image;
    }
    const QByteArray mask() const
    {
        return m_mask;
    }

    void setId(int i)
    {
        m_id = i;
    }
    void setMinf(int m)
    {
        m_minf = m;
    }
    void setMagf(int m)
    {
        m_magf = m;
    }
    void setMa(int m)
    {
        m_ma = m;
    }
    void setOff(const QPoint& o)
    {
        m_off = o;
    }
    void setSz(const QPoint& o)
    {
        m_sz = o;
    }
    void setImage(const QByteArray& i)
    {
        m_image = i;
    }
    void setMask(const QByteArray& i)
    {
        m_mask = i;
    }

private:
    int m_id;
    int m_minf;
    int m_magf;
    int m_ma;
    QList<int> m_types;
    QPoint m_off;
    QPoint m_sz;
    QByteArray m_image;
    QByteArray m_mask;
};
