#include "tilelayer.h"

TileLayer::TileLayer(const QByteArray &d, Resource *r)
    : Layer(d, r), m_t(0), m_id(0), m_w(0)
{
}

const QString TileLayer::type() const
{
    return "tile";
}

const QByteArray TileLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar((unsigned char)m_t);
    ws.writeChar((unsigned char)m_id);
    ws.writeShort((unsigned short)m_w);
    ws.writeImage(m_image);
    return ret;
}

bool TileLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_t = (unsigned char)s.getChar();
        m_id = (unsigned char)s.getChar();
        m_w = (unsigned short)s.getShort();
        QByteArray imageData = s.getBytes();
        if (!m_image.loadFromData(imageData)) {
            m_error = "Can't load tile image from resource";
            return false;
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
