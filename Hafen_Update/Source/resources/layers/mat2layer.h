#ifndef MAT2LAYER_H
#define MAT2LAYER_H

#include "layer.h"

class Mat2Layer : public Layer
{
public:
    Mat2Layer(const QByteArray& d, Resource* r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int id() const
    {
        return m_id;
    }
    const QMap<QString, VariantList> keyargs() const;
    void removeKey(const QString& k);
    void removeValue(const QString& k, int index);
    void updateValue(const QString& k, int index, const QVariant& v);

    void setId(int i)
    {
        m_id = i;
    }
    void setKeyargs(const QMap<QString, VariantList>& k)
    {
        m_keyargs = k;
    }

private:
    int m_id;
    QMap<QString, VariantList> m_keyargs;
};

#endif // MAT2LAYER_H
