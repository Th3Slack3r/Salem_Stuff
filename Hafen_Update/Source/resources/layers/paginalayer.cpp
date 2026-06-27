#include "paginalayer.h"

PaginaLayer::PaginaLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString PaginaLayer::type() const
{
    return "pagina";
}

const QByteArray PaginaLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeBytes(m_text.toUtf8());
    return ret;
}

bool PaginaLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    try {
        m_text = QString::fromUtf8(m_rawData);
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
