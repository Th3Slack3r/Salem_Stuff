#ifndef RLINKLAYER_H
#define RLINKLAYER_H

#include "layer.h"

class RlinkLayer : public Layer
{
public:
    RlinkLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int id() const {return m_id;}
    int t() const {return m_t;}
    const QString meshnm() const {return m_meshnm;}
    int meshver() const {return m_meshver;}
    int meshid() const {return m_meshid;}
    const QString matnm() {return m_matnm;}
    int matver() const {return m_matver;}
    int matid() const {return m_matid;}
    const QString nm() const {return m_nm;}
    int ver() const {return m_ver;}

    void setId(int i) {m_id = i;}
    void setMeshnm(const QString &m) {m_meshnm = m;}
    void setMeshver(int m) {m_meshver = m;}
    void setMeshid(int m) {m_meshid = m;}
    void setMatnm(const QString &m) {m_matnm = m;}
    void setMathver(int m) {m_matver = m;}
    void setMathid(int m) {m_matid = m;}
    void setNm(const QString &n) {m_nm = n;}
    void setVer(int v) {m_ver = v;}


private:
    int m_lver;
    int m_id;
    int m_t; // Read only
    QString m_meshnm;
    int m_meshver;
    int m_meshid;
    QString m_matnm;
    int m_matver;
    int m_matid;
    QString m_nm;
    int m_ver;
};

#endif // RLINKLAYER_H
