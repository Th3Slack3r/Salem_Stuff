#ifndef CLAMBLAYER_H
#define CLAMBLAYER_H

#include "layer.h"

class ClambLayer : public Layer
{
public:
    ClambLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int ver() const {return m_ver;}
    const QString pnm() const {return m_pnm;}
    int pid() const {return m_pid;}
    int minc() const {return m_minc;}
    int maxc() const {return m_maxc;}
    int maxi() const {return m_maxi;}
    float bvol() const {return m_bvol;}
    const QList<QString> cnms() const {return m_cnms;}
    const QList<double> ieps() const {return m_ieps;}

    void setPnm(const QString &p) {m_pnm = p;}
    void setPid(int p) {m_pid = p;}
    void setMinc(int m) {m_minc = m;}
    void setMaxc(int m) {m_maxc = m;}
    void setMaxi(int m) {m_maxi = m;}
    void setBvol(float b) {m_bvol = b;}
    void setCnms(const QList<QString> &c) {m_cnms = c;}
    void setIeps(const QList<double> &i) {m_ieps = i;}

private:
    int m_ver;
    QString m_pnm;
    int m_pid;
    int m_minc;
    int m_maxc;
    int m_maxi;
    float m_bvol;
    QList<QString> m_cnms;
    QList<double> m_ieps;
};

#endif // CLAMBLAYER_H
