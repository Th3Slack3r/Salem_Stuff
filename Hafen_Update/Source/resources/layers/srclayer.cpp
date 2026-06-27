#include "srclayer.h"

SrcLayer::SrcLayer(const QByteArray& d, Resource* r)
    : Layer(d, r)
{
    //
}

const QString SrcLayer::type() const
{
    return "src";
}

const QByteArray SrcLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar(mType);
    ws.writeString(mName);
    ws.writeBytes(mData);
    return ret;
}

bool SrcLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        mType = s.getChar();
        mName = s.getString();
        mData = s.getBytes();
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}
