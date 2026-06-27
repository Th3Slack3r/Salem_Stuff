#ifndef OBJEXPORTER_H
#define OBJEXPORTER_H

#include "resources/layers/vbuf2layer.h"
#include <QVector3D>

class ObjExporter
{
public:
    ObjExporter(Vbuf2Layer* layer);

    bool saveAs(const QString& file, const QList<MeshLayer*>& meshes,
                const QString& mtlFile = QString(), const QString& materialName = QString());
    const QString& error() const { return m_error; }

private:
    Vbuf2Layer* m_layer;
    QString m_error;

    const QVector3D rotateVector(const QVector3D& v);
    const QString vectorToString(const QVector3D& v);
    void writeSublayer(
        const QString& prefix, int dimension, const QList<float>& dots, QTextStream& stream, bool reverse = false);
};

#endif // OBJEXPORTER_H


