#ifndef AUDIOLAYER_H
#define AUDIOLAYER_H

#include "layer.h"

class AudioLayer : public Layer
{
public:
    AudioLayer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    const QByteArray audio() const { return m_audio; }
    void setAudio(const QByteArray &a) { m_audio = a; }

private:
    QByteArray m_audio;
};

#endif // AUDIOLAYER_H
