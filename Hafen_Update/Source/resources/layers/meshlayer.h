#ifndef MESHLAYER_H
#define MESHLAYER_H

#include "layer.h"

class MeshLayer : public Layer
{
public:
    MeshLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int fl() const {return m_fl;}
    int matid() const {return m_matid;}
    int id() const {return m_id;}
    int ref() const {return m_ref;}
    const QList<int> ind() const {return m_ind;}
    const QList<QString> rdat() const {return m_rdat;}

    void setMatid(int i) {m_matid = i;}
    void setId(int i) {m_id = i;}
    void setRef(int r) {m_ref = r;}
    void setInd(const QList<int> &i) {m_ind = i;}
    void setRdat(const QList<QString> &r) {m_rdat = r;}

private:
    int m_fl; // Read only
    int m_num;
    int m_matid;
    int m_id;
    int m_ref;
    QList<QString> m_rdat;
    QList<int> m_ind;
};

#endif // MESHLAYER_H
