#include "unknownlayer.h"

UnknownLayer::UnknownLayer(const QString &type, const QByteArray &d, Resource *r)
    : Layer(d, r)
{
    m_type = type;
}

const QString UnknownLayer::type() const
{
    return m_type;
}

bool UnknownLayer::parse()
{
    return true;
}

void UnknownLayer::setErrorString(const QString &err)
{
    m_error = err;
}
