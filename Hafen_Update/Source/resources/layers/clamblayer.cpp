#include "clamblayer.h"

ClambLayer::ClambLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString ClambLayer::type() const
{
    return "clamb";
}

const QByteArray ClambLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar((unsigned char)m_ver);
    if (m_ver == 2) {
        ws.writeString(m_pnm);
        if (m_pnm.length())
            ws.writeShort((unsigned short)m_pid);
    }
    ws.writeChar((unsigned char)m_minc);
    ws.writeChar((unsigned char)m_maxc);
    ws.writeShort((unsigned short)m_maxi);
    ws.writeFloat(m_bvol);
    ws.writeChar((unsigned char)m_cnms.length());
    for (int i = 0; i < m_cnms.length(); ++i) {
        ws.writeString(m_cnms[i]);
        ws.writeFloat(m_ieps[i]);
    }
    return ret;
}

bool ClambLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_ver = (unsigned char)s.getChar();
        if (m_ver < 1 || m_ver > 2) {
            m_error = QString("Unknown clip-ambiance version: %1").arg(m_ver);
            return false;
        }
        if (m_ver == 2) { // >= 2 by loftar?
            m_pnm = s.getString();
            if (m_pnm.length())
                m_pid = (unsigned short)s.getShort();
        }
        m_minc = (unsigned char)s.getChar();
        m_maxc = (unsigned char)s.getChar();
        m_maxi = (unsigned short)s.getShort();
        m_bvol = s.getFloat();
        int n = (unsigned char)s.getChar();
        for (int i = 0; i < n; ++i) {
            m_cnms << s.getString();
            m_ieps << s.getFloat();
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
