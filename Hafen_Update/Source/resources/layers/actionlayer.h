#ifndef ACTIONLAYER_H
#define ACTIONLAYER_H

#include "layer.h"

class ActionLayer : public Layer
{
public:
    ActionLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QString pr() const {return m_pr;}
    const QString name() const {return m_name;}
    const QList<QString> ad() const {return m_ad;}
    int pver() const {return m_pver;}
    int hk() const {return m_hk;}

    void setPr(const QString &p) {m_pr = p;}
    void setName(const QString &n) {m_name = n;}
    void setAd(const QList<QString> &l) {m_ad = l;}
    void setPver(int p) {m_pver = p;}
    void setHk(int h) {m_hk = h;}

private:
    QString m_pr;
    QString m_reserved;
    int m_pver;
    QString m_name;
    int m_hk;
    QList<QString> m_ad;
};

#endif // ACTIONLAYER_H
