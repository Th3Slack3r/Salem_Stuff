#ifndef NEGLAYER_H
#define NEGLAYER_H

#include "layer.h"

class NegLayer : public Layer
{
public:
    NegLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QPoint cc() const {return m_cc;}
    const QPoint tl() const {return m_tl;}
    const QPoint br() const {return m_br;}
    const QPoint oc() const {return m_oc;}
    const QVector<QVector<QPoint> > ep() const {return m_ep;}

    void setCc(const QPoint &p) {m_cc = p;}
    void setEp(const QVector<QVector<QPoint> > &e) {m_ep = e;}

private:
    QPoint m_cc;
    QPoint m_tl; // Top left offset
    QPoint m_br; // Bottom right offset
    QPoint m_oc; // Object center offset? (usually 0;0)
    int m_en;
    QVector<QVector<QPoint> > m_ep;
};

#endif // NEGLAYER_H
