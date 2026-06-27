#include "actionlayer.h"

ActionLayer::ActionLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString ActionLayer::type() const
{
    return "action";
}

const QByteArray ActionLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeString(m_pr);
    ws.writeShort(m_pver);
    ws.writeString(m_name);
    ws.writeString(m_reserved);
    ws.writeShort((char)m_hk);
    ws.writeShort(m_ad.length());
    for (int i = 0; i < m_ad.length(); ++i)
        ws.writeString(m_ad[i]);
    return ret;
}

bool ActionLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_pr = s.getString();
        m_pver = s.getShort();
        m_name = s.getString();
        m_reserved = s.getString(); qDebug() << "ACTION RESERVED:" << m_reserved;
        m_hk = (char)s.getShort();
        int len = (unsigned short)s.getShort();
        for (int i = 0; i < len; ++i)
            m_ad << s.getString();
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
