#include "audio2layer.h"

Audio2Layer::Audio2Layer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{
    m_bvol = 1.0;
}

const QString Audio2Layer::type() const
{
    return "audio2";
}

const QByteArray Audio2Layer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar(static_cast<char>(m_ver));
    ws.writeString(m_id);
    if (m_ver == 2)
        ws.writeShort(static_cast<short>(m_bvol * 1000));
    ws.writeBytes(m_audio);
    return ret;
}

bool Audio2Layer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_ver = s.getChar();
        if (m_ver && m_ver < 3) {
            m_id = s.getString();
            if (m_ver == 2)
                m_bvol = (s.getShort() / 1000.0);
            m_audio = s.getBytes();
        } else {
            m_error = QString("Unknown audio layer version: %1").arg(m_ver);
            return false;
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }
    return true;
}
