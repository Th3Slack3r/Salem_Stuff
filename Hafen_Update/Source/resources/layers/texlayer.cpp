#include "texlayer.h"
#include <QtCore/QBuffer>

TexLayer::TexLayer(const QByteArray& d, Resource* r)
    : Layer(d, r)
    , m_id(-1)
    , m_minf(-1)
    , m_magf(-1)
    , m_ma(0)
{
}

const QString TexLayer::type() const
{
    return "tex";
}

const QByteArray TexLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeShort(static_cast<short>(m_id));
    ws.writeCoord16(m_off);
    ws.writeCoord16(m_sz);
    foreach(int t, m_types)
    {
        ws.writeChar(static_cast<char>(t));
        switch (t)
        {
            case 0:
            {
                ws.writeInt(m_image.size());
                ws.writeBytes(m_image);
                break;
            }
            case 1:
                ws.writeChar(static_cast<char>(m_ma));
                break;
            case 2:
                ws.writeChar(static_cast<char>(m_magf));
                break;
            case 3:
                ws.writeChar(static_cast<char>(m_minf));
                break;
            case 4:
            {
                ws.writeInt(m_mask.size());
                ws.writeBytes(m_mask);
                break;
            }
            default:
                break;
        }
    }

    return ret;
}

bool TexLayer::parse()
{
    if (m_rawData.isEmpty())
    {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try
    {
        m_id = s.getShort();
        m_off = s.getCoord16();
        m_sz = s.getCoord16();
        while (!s.atEnd())
        {
            int t = s.getChar();
            m_types << t;
            switch (t)
            {
                case 0:
                    m_image = s.getBytes(s.getInt());
                    break;
                case 1:
                    m_ma = static_cast<unsigned char>(s.getChar());
                    break;
                case 2:
                    m_magf = static_cast<unsigned char>(s.getChar());
                    break;
                case 3:
                    m_minf = static_cast<unsigned char>(s.getChar());
                    break;
                case 4:
                    m_mask = s.getBytes(s.getInt());
                    break;
                case 5:
                    break;
                default:
                    m_error = QString("Unknown texture data part %1").arg(t);
                    return false;
            }
        }
    }
    catch (const runtime_error& e)
    {
        m_error = QString(e.what());
        qDebug() << m_error;
        return false;
    }

    return true;
}
