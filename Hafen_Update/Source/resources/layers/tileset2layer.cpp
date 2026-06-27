#include "tileset2layer.h"

Tileset2Layer::Tileset2Layer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString Tileset2Layer::type() const
{
    return "tileset2";
}

const QByteArray Tileset2Layer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar((unsigned char)m_p);
    switch (m_p) {
    case 0:
        ws.writeString(m_tn);
        ws.writeList(m_ta);
        break;
    case 1:
        ws.writeShort((unsigned short)m_flavobjs.length());
        ws.writeShort((unsigned short)m_flavprob);
        foreach (const Flavobj &f, m_flavobjs) {
            ws.writeString(f.fln);
            ws.writeShort((unsigned short)f.flv);
            ws.writeChar((unsigned char)f.flw);
        }
        break;
    case 2:
        ws.writeChar((char)m_tags.length());
        foreach (const QString &s, m_tags)
            ws.writeString(s);
    default: break;
    }
    return ret;
}

bool Tileset2Layer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        while (!s.atEnd()) {
            m_p = (unsigned char)s.getChar();

            int flnum(0);
            int l(0);
            switch (m_p) {
            case 0:
                m_tn = s.getString();
                m_ta = s.getList();
                break;
            case 1:
                flnum = (unsigned short)s.getShort();
                m_flavprob = (unsigned short)s.getShort();
                for (int i = 0; i < flnum; ++i) {
                    Flavobj f;
                    f.fln = s.getString();
                    f.flv = (unsigned short)s.getShort();
                    f.flw = (unsigned char)s.getChar();
                    m_flavobjs << f;
                }
                break;
            case 2:
                l = s.getChar();
                for (int i = 0; i < l; ++i)
                    m_tags << s.getString();
                break;
            default:
                m_error = QString("Invalid tileset part %1").arg(m_p);
                return false;
            }
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
