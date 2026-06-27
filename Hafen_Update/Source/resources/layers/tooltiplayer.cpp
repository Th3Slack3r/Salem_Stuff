#include "tooltiplayer.h"

TooltipLayer::TooltipLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString TooltipLayer::type() const
{
    return "tooltip";
}

const QByteArray TooltipLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeBytes(m_t.toUtf8());
    return ret;
}

bool TooltipLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    try {
        m_t = QString::fromUtf8(m_rawData);
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
