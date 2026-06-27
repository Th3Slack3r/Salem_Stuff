#include "audiolayer.h"

AudioLayer::AudioLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{
}

const QString AudioLayer::type() const
{
    return "audio";
}

const QByteArray AudioLayer::toByteArray()
{
    return m_audio;
}

bool AudioLayer::parse()
{
    // Some audio layers may legitimately be empty; treat that as valid.
    m_audio = m_rawData;
    return true;
}
