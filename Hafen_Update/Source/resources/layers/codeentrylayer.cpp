#include "codeentrylayer.h"

CodeentryLayer::CodeentryLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString CodeentryLayer::type() const
{
    return "codeentry";
}

const QByteArray CodeentryLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar(m_t);
    QMap<QString, QString>::iterator it;
    for (it = m_map.begin(); it != m_map.end(); ++it) {
        if (it.key().isEmpty()) // Map keys are sorted, skip empty keys if any
            continue;

        ws.writeString(it.key());
        if (m_t == 1)
            ws.writeString(it.value());
        else if (m_t == 2)
            ws.writeShort(it.value().toInt(0));
    }
    // Write 'end' of map
    if (m_t == 1) {
        ws.writeString(QString());
        ws.writeString(QString());
    } else if (m_t == 2) {
        ws.writeString(QString());
    }

    return ret;
}

bool CodeentryLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        while (!s.atEnd()) {
            m_t = s.getChar();
            if (m_t == 1) {
                while (true) {
                    QString key = s.getString();
                    QString value = s.getString();
                    if (key.isEmpty()) // Loftar really checks after 2nd string has been read
                        break;

                    m_map.insert(key, value);
                }
            } else if (m_t == 2) {
                while (true) {
                    QString key = s.getString();
                    if (key.isEmpty())
                        break;

                    m_map.insert(key, QString("%1").arg(s.getShort()));
                }
            } else {
                m_error = QString("Unknown codeentry data type: %1").arg(m_t);
                return false;
            }
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}
