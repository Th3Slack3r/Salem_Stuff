#ifndef PAGINALAYER_H
#define PAGINALAYER_H

#include "layer.h"

class PaginaLayer : public Layer
{
public:
    PaginaLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QString text() const {return m_text;}

    void setText(const QString &t) {m_text = t;}

private:
    QString m_text;
};

#endif // PAGINALAYER_H
