#include "tilesetlayer.h"

TilesetLayer::TilesetLayer(const QByteArray &d, Resource *r)
    : Layer(d, r), m_fl(0), m_flavprob(0)
{
}

const QString TilesetLayer::type() const
{
    return "tileset";
}

const QByteArray TilesetLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar((unsigned char)m_fl);
    ws.writeShort((unsigned short)m_flavobjs.length());
    ws.writeShort((unsigned short)m_flavprob);
    foreach (const Tileset2Layer::Flavobj &f, m_flavobjs) {
        ws.writeString(f.fln);
        ws.writeShort((unsigned short)f.flv);
        ws.writeChar((unsigned char)f.flw);
    }
    return ret;
}

bool TilesetLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_fl = (unsigned char)s.getChar();
        int flnum = (unsigned short)s.getShort();
        m_flavprob = (unsigned short)s.getShort();
        for (int i = 0; i < flnum; ++i) {
            Tileset2Layer::Flavobj f;
            f.fln = s.getString();
            f.flv = (unsigned short)s.getShort();
            f.flw = (unsigned char)s.getChar();
            m_flavobjs << f;
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
