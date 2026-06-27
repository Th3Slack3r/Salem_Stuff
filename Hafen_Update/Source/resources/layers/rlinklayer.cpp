#include "rlinklayer.h"

RlinkLayer::RlinkLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString RlinkLayer::type() const
{
    return "rlink";
}

const QByteArray RlinkLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);

    if (m_t < 3) {
        ws.writeChar((unsigned char)m_t);
    } else if (m_t == 3) {
        ws.writeChar((unsigned char)m_lver);
        ws.writeShort(m_id);
        ws.writeChar((unsigned char)m_t);
    }

    if (m_t == 0) {
        ws.writeString(m_meshnm);
        ws.writeShort((unsigned short)m_meshver);
        ws.writeShort(m_meshid);
        ws.writeString(m_matnm);
        ws.writeShort((unsigned short)m_matver);
        ws.writeShort(m_matid);
    } else if (m_t == 1 || m_t == 2) {
        ws.writeString(m_nm);
        ws.writeShort((unsigned short)m_ver);
        if (m_t == 2)
            ws.writeShort(m_meshid);
    }

    return ret;
}

bool RlinkLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_lver = (unsigned char)s.getChar();
        if (m_lver < 3) {
            m_t = m_lver;
            m_id = -1;
        } else if (m_lver == 3) {
            m_id = s.getShort();
            m_t = (unsigned char)s.getChar();
        } else {
            m_error = QString("Invalid renderlink version: %1").arg(m_lver);
            return false;
        }

        if (m_t == 0) {
            m_meshnm = s.getString();
            m_meshver = (unsigned short)s.getShort();
            m_meshid = s.getShort();
            m_matnm = s.getString();
            m_matver = (unsigned short)s.getShort();
            m_matid = s.getShort();
        } else if (m_t == 1) {
            m_nm = s.getString();
            m_ver = (unsigned short)s.getShort();
        } else if (m_t == 2) {
            m_nm = s.getString();
            m_ver = (unsigned short)s.getShort();
            m_meshid = s.getShort();
        } else {
            m_error = QString("Invalid renderlink type: %1").arg(m_t);
            return false;
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}
