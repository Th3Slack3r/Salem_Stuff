#ifndef CODEENTRYLAYER_H
#define CODEENTRYLAYER_H

#include "layer.h"

class CodeentryLayer : public Layer
{
public:
    CodeentryLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QMap<QString, QString> map() const {return m_map;}
    int t() const {return m_t;}

    void setMap(const QMap<QString, QString> &m) {m_map = m;}

private:
    int m_t;
    QMap<QString, QString> m_map;
};

#endif // CODEENTRYLAYER_H
