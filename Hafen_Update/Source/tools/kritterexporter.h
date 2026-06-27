#ifndef KRITTEREXPORTER_H
#define KRITTEREXPORTER_H

#include <QString>

class KritterExporter
{
public:
    static bool exportFolderToGltf(const QString &folderPath, const QString &gltfPath, QString *error);
};

#endif // KRITTEREXPORTER_H
