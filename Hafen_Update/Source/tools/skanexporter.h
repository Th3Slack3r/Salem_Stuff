#ifndef SKANEXPORTER_H
#define SKANEXPORTER_H

#include <QString>

class SkanLayer;

class SkanExporter
{
public:
    static bool exportGltf(const SkanLayer *layer, const QString &gltfPath, QString *error);
};

#endif // SKANEXPORTER_H
