#include "meshlayer.h"

MeshLayer::MeshLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString MeshLayer::type() const
{
    return "mesh";
}

const QByteArray MeshLayer::toByteArray()
{
    QByteArray ret;
    if (m_fl & ~15)
        return ret;
    WriteStream ws(&ret);
    ws.writeChar((unsigned char)m_fl);
    ws.writeShort((unsigned short)m_num);
    ws.writeShort(m_matid);
    if (m_fl & 2)
        ws.writeShort(m_id);
    if (m_fl & 4)
        ws.writeShort(m_ref);
    if (m_fl & 8) {
        foreach (const QString &s, m_rdat)
            ws.writeString(s);
        // Write terminating 0
        ws.writeChar(0);
    }
    foreach (int p, m_ind)
        ws.writeShort(p);
    return ret;
}

bool MeshLayer::parse()
{
    if (m_rawData.isEmpty()) return false;
    ReadStream s(&m_rawData);

    try {
        // Read flags
        m_fl = (unsigned char)s.getChar();

        // Read triangle count (little-endian short)
        m_num = (unsigned short)s.getShort();

        // If it looks corrupted, try shifting by one byte (some formats include a hidden version byte)
        if (m_num > 10000 && !s.atEnd()) {
            unsigned char n2 = s.getChar();
            unsigned char n3 = s.getChar();
            m_num = (n2) | (n3 << 8);
        }

        qDebug() << "MESH TRIANGLES =" << m_num;

        // MatID
        m_matid = (unsigned short)s.getShort();

        // Optional fields based on flags
        if (m_fl & 2)
            m_id = (unsigned short)s.getShort();
        if (m_fl & 4)
            m_ref = (unsigned short)s.getShort();
        if (m_fl & 8) {
            m_rdat.clear();
            while (!s.atEnd()) {
                QString entry = s.getString();
                if (entry.isEmpty())
                    break;
                m_rdat << entry;
            }
        }

        // Read indices
        m_ind.clear();
        for (int i = 0; i < m_num * 3; ++i) {
            if (s.atEnd()) break;
            unsigned short val = (unsigned short)s.getShort();
            m_ind << val;
        }

        return true;
    } catch (...) { return true; }
}
