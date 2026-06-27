#include "foodevlayer.h"

FoodevLayer::FoodevLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString FoodevLayer::type() const
{
    return "foodev";
}

const QByteArray FoodevLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar((unsigned char)m_ver);
    ws.writeColor(m_col);
    ws.writeString(m_nm);
    ws.writeShort(m_sort);
    return ret;
}

bool FoodevLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_ver = (unsigned char)s.getChar();
        if (m_ver == 1) {
            m_col = s.getColor();
            m_nm = s.getString();
            m_sort = s.getShort();
        } else {
            m_error = QString("Unknown foodev version: %1").arg(m_ver);
            return false;
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
