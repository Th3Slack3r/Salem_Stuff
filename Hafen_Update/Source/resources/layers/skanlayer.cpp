#include "skanlayer.h"

SkanLayer::SkanLayer(const QByteArray &d, Resource *r)
    : Layer(d, r), m_id(0), m_fl(0), m_mode(0), m_len(0.0f), m_nspeed(0.0f)
{
}

const QString SkanLayer::type() const
{
    return "skan";
}

const QByteArray SkanLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeShort((unsigned short)m_id);
    ws.writeChar((unsigned char)m_fl);
    ws.writeChar((unsigned char)m_mode);
    ws.writeFloatD(m_len);
    if ((m_fl & 1) != 0)
        ws.writeFloatD(m_nspeed);

    foreach (const Bone &b, m_bones) {
        ws.writeString(b.name);
        ws.writeShort((unsigned short)b.frames.length());
        foreach (const Frame &f, b.frames) {
            ws.writeFloatD(f.tm);
            ws.writeFloatD(f.tx);
            ws.writeFloatD(f.ty);
            ws.writeFloatD(f.tz);
            ws.writeFloatD(f.rang);
            ws.writeFloatD(f.rax);
            ws.writeFloatD(f.ray);
            ws.writeFloatD(f.raz);
        }
    }

    return ret;
}

bool SkanLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    auto remaining = [&s]() -> int { return s.size() - s.position(); };
    try {
        if (remaining() < 2) {
            m_error = "Truncated skan header";
            return false;
        }
        m_id = (unsigned short)s.getShort();
        if (remaining() < 2) {
            m_error = "Truncated skan header";
            return false;
        }
        m_fl = (unsigned char)s.getChar();
        m_mode = (unsigned char)s.getChar();
        if (m_mode < 0 || m_mode > 3) {
            m_error = QString("Invalid skan mode: %1").arg(m_mode);
            return false;
        }
        if (remaining() < 5) {
            m_error = "Truncated skan header";
            return false;
        }
        m_len = s.getFloatD();
        if ((m_fl & 1) != 0) {
            if (remaining() >= 5)
                m_nspeed = s.getFloatD();
            else
                m_nspeed = 0.0f;
        } else {
            m_nspeed = 0.0f;
        }

        while (!s.atEnd()) {
            Bone b;
            try {
                b.name = s.getString();
            } catch (...) {
                break;
            }
            if (remaining() < 2)
                break;
            int frames = (unsigned short)s.getShort();
            const int bytesPerFrame = 8 * 5;
            if (frames * bytesPerFrame > remaining()) {
                frames = remaining() / bytesPerFrame;
            }
            for (int i = 0; i < frames; ++i) {
                if (remaining() < bytesPerFrame)
                    break;
                Frame f;
                f.tm = s.getFloatD();
                f.tx = s.getFloatD();
                f.ty = s.getFloatD();
                f.tz = s.getFloatD();
                f.rang = s.getFloatD();
                f.rax = s.getFloatD();
                f.ray = s.getFloatD();
                f.raz = s.getFloatD();
                b.frames << f;
            }
            m_bones << b;
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}
