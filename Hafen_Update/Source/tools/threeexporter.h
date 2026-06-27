#pragma once

#include <QList>
#include <QString>
#include <QVector3D>

#include "resources/layers/meshlayer.h"
#include "resources/layers/vbuf2layer.h"

class ThreeExporter
{
public:
    ThreeExporter(Vbuf2Layer* layer = nullptr);

    bool saveAs(const QString& name, const QString& file, const QList<MeshLayer*>& meshes);

protected:
    void writeVerticles(const QString& name, const QList<float>& dots, MeshLayer* mesh, QTextStream& stream);
    void writeDots(const QString& name, const QList<float>& dots, MeshLayer* mesh, QTextStream& stream) const;

private:
    Vbuf2Layer* m_layer;
};
