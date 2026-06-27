#include "neglayer.h"

NegLayer::NegLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString NegLayer::type() const
{
    return "neg";
}

const QByteArray NegLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeCoord16(m_cc);
    ws.writeCoord16(m_tl);
    ws.writeCoord16(m_br);
    ws.writeCoord16(m_oc);
    ws.writeChar(m_en);
    for (int i = 0; i < m_en; ++i) {
        ws.writeChar(i);
        ws.writeShort(m_ep[i].size());
        for (int j = 0; j < m_ep[i].size(); ++j)
            ws.writeCoord16(m_ep[i][j]);
    }
    return ret;
}

bool NegLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_cc = s.getCoord16();

        // There are 3 hidden coords that skipped by client, but used by server for collisions
        m_tl = s.getCoord16();
        m_br = s.getCoord16();
        m_oc = s.getCoord16();

        m_ep.resize(8);
        m_en = s.getChar();
        for (int i = 0; i < m_en; ++i) {
            int epid = s.getChar();
            int cn = (unsigned short)s.getShort();
            m_ep[epid].resize(cn);
            for (int j = 0; j < cn; ++j) {
                m_ep[epid][j] = s.getCoord16();
            }
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
