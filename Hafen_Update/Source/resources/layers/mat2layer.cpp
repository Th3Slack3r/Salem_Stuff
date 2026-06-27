#include "mat2layer.h"

Mat2Layer::Mat2Layer(const QByteArray& d, Resource* r)
    : Layer(d, r)
{
}

const QString Mat2Layer::type() const
{
    return "mat2";
}

const QByteArray Mat2Layer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeShort(m_id);
    foreach(const QString& k, m_keyargs.keys())
    {
        ws.writeString(k);
        ws.writeList(m_keyargs[k]);
    }
    return ret;
}

bool Mat2Layer::parse()
{
    if (m_rawData.isEmpty())
    {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try
    {
        m_id = s.getShort();
        while (!s.atEnd())
        {
            QString name = s.getString();
            VariantList list = s.getList();
            m_keyargs.insert(name, list);
        }
    }
    catch (const runtime_error& e)
    {
        m_error = QString(e.what());
        return false;
    }
    return true;
}

const QMap<QString, VariantList> Mat2Layer::keyargs() const
{
    return m_keyargs;
}

void Mat2Layer::removeKey(const QString& k)
{
    m_keyargs.remove(k);
}

void Mat2Layer::removeValue(const QString& k, int index)
{
    m_keyargs[k].remove(index);
}

void Mat2Layer::updateValue(const QString& k, int index, const QVariant& v)
{
    m_keyargs[k].update(index, v);
}
