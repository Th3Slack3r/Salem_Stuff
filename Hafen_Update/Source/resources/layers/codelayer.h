#ifndef CODELAYER_H
#define CODELAYER_H

#include "layer.h"

class CodeLayer : public Layer
{
public:
    CodeLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QString name() const {return m_name;}
    const QByteArray data() const {return m_data;}

    void setName(const QString &n) {m_name = n;}
    void setData(const QByteArray &d) {m_data = d;}

private:
    QString m_name;
    QByteArray m_data;
};

#endif // CODELAYER_H
