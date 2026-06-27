#ifndef UNKNOWNLAYER_H
#define UNKNOWNLAYER_H

#include "layer.h"

class UnknownLayer : public Layer
{
public:
    UnknownLayer(const QString &type, const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual bool parse();

    void setErrorString(const QString &err);

private:
    QString m_type;
};

#endif // UNKNOWNLAYER_H
