#ifndef SRCLAYER_H
#define SRCLAYER_H

#include "layer.h"

class SrcLayer : public Layer
{
public:
    SrcLayer(const QByteArray &d, Resource *r = nullptr);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    char srctype() const {return mType;}
    QString name() const {return mName;}
    QByteArray data() const {return mData;}

private:
    char mType = 0;
    QString mName;
    QByteArray mData;
};

#endif // SRCLAYER_H
