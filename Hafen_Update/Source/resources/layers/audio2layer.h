#ifndef AUDIO2LAYER_H
#define AUDIO2LAYER_H

#include "layer.h"

class Audio2Layer : public Layer
{
public:
    Audio2Layer(const QByteArray &d, Resource *r = 0);

    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();

    int ver() const {return m_ver;}
    const QString id() const {return m_id;}
    double bvol() const {return m_bvol;}
    const QByteArray audio() const {return m_audio;}

    void setId(const QString &i) {m_id = i;}
    void setBvol(double b) {m_bvol = b;}
    void setAudio(const QByteArray &a) {m_audio = a;}

private:
    int m_ver;
    QString m_id;
    double m_bvol;
    QByteArray m_audio;
};

#endif // AUDIO2LAYER_H
