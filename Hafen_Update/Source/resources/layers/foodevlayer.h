#ifndef FOODEVLAYER_H
#define FOODEVLAYER_H

#include "layer.h"

class FoodevLayer : public Layer
{
public:
    FoodevLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int ver() const {return m_ver;}
    const QColor col() const {return m_col;}
    const QString nm() const {return m_nm;}
    int sort() const {return m_sort;}

    void setCol(const QColor &c) {m_col = c;}
    void setNm(const QString &n) {m_nm = n;}
    void setSort(int s) {m_sort = s;}

private:
    int m_ver;
    QColor m_col;
    QString m_nm;
    int m_sort;
};

#endif // FOODEVLAYER_H
