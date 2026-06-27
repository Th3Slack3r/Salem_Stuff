#include "imagelayer.h"

ImageLayer::ImageLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString ImageLayer::type() const
{
    return "image";
}

const QByteArray ImageLayer::toByteArray()
{
    QByteArray ret;
    WriteStream s(&ret);
    s.writeShort(m_z);
    s.writeShort(m_subz);
    s.writeChar(m_flags);
    s.writeShort(m_id);
    s.writeCoord16(m_o);
    if ((m_flags & 4) != 0) {
        for (auto it = m_keydata.begin(); it != m_keydata.end(); ++it) {
            s.writeString(it.key());
            if (it.value().size() > 128) {
                s.writeChar(-1);
                s.writeInt(it.value().size());
            } else {
                s.writeChar(static_cast<char>(it.value().size()));
            }
            s.writeBytes(it.value());
        }
        s.writeString(""); // eol
    }
    s.writeImage(m_image);
    return ret;
}

bool ImageLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        m_z = s.getShort();
        m_subz = s.getShort();
        m_flags = s.getChar();
        m_nooff = (m_flags & 2) != 0;
        m_id = s.getShort();
        m_o = s.getCoord16();
        if ((m_flags & 4) != 0) {
            while (true) {
                QString key = s.getString();
                if (key.isEmpty())
                    break;
                int len = s.getChar();
                if((len & 0x80) != 0)
                    len = s.getInt();
                QByteArray data = s.getBytes(len);
                m_keydata.insert(key, data);
            }
        }
        QByteArray imageData = s.getBytes();
        if (!m_image.loadFromData(imageData)) {
            m_error = "Can't load image from resource";
            return false;
        }
    } catch (const std::runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}
