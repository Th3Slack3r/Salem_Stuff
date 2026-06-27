#ifndef TOOLTIPLAYER_H
#define TOOLTIPLAYER_H

#include "layer.h"

class TooltipLayer : public Layer
{
public:
    TooltipLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QString t() const {return m_t;}

    void setT(const QString &t) {m_t = t;}

private:
    QString m_t;
};

#endif // TOOLTIPLAYER_H
