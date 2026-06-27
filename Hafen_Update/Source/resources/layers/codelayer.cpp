#include "codelayer.h"

CodeLayer::CodeLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString CodeLayer::type() const
{
    return "code";
}

const QByteArray CodeLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeString(m_name);
    ws.writeBytes(m_data);
    return ret;
}

bool CodeLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_name = s.getString();
        m_data = s.getBytes();
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}
