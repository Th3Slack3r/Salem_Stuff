#pragma once
#include "layer.h"
#include <QMap>
#include <QList>
#include <QString>
#include <QByteArray>

class VbufLayerWrapper : public Vbuf2Layer {
public:
    VbufLayerWrapper(Layer* oldLayer)
        : Vbuf2Layer(QByteArray(), nullptr) // no resource, empty byte array
    {
        // Copy data from old layer (assumes oldLayer has sublayers() and sublayer() functions)
        // We'll just copy pos, nrm, tex if available
        for (const QString &key : {"pos","nrm","tex"}) {
            if (oldLayer->sublayers().contains(key)) {
                m_sublayers[key] = oldLayer->sublayer(key);
            }
        }
        m_num = m_sublayers.contains("pos") ? m_sublayers["pos"].size() / 3 : 0;
    }
};
