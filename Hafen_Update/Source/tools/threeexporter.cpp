#include "threeexporter.h"

#define M_PI 3.14159

#include <QFile>

ThreeExporter::ThreeExporter(Vbuf2Layer* layer)
    : m_layer(layer)
{
}

bool ThreeExporter::saveAs(const QString& name, const QString& file, const QList<MeshLayer*>& meshes)
{
    if (m_layer == nullptr || file.isEmpty() || meshes.isEmpty())
        return false;

    QFile expFile(file);
    if (!expFile.open(QIODevice::WriteOnly | QIODevice::Text))
        return false;

    QTextStream stream(&expFile);
    stream.setRealNumberNotation(QTextStream::FixedNotation);
    stream.setRealNumberPrecision(6);

    // Writing start
    stream << "{\n";
    // Writing name
    stream << "    \"name\": \"" << name << "\",\n";
    // Meshes field
    stream << "    \"meshes\": [\n";
    int index = 0;
    for (auto* mesh : meshes)
    {
        stream << "        {\n";
        stream << "            \"name\": \"mesh_" << index << "\",\n";
        writeVerticles("position", m_layer->sublayer("pos"), mesh, stream);
        stream << ",\n";
        writeVerticles("normal", m_layer->sublayer("nrm"), mesh, stream);
        stream << ",\n";
        /*if (m_layer->sublayers().contains("tan"))
        {
            writeVerticles("tangent", m_layer->sublayer("tan"), mesh, stream);
            stream << ",\n";
        }
        if (m_layer->sublayers().contains("bit"))
        {
            writeVerticles("bitangent", m_layer->sublayer("bit"), mesh, stream);
            stream << ",\n";
        }*/
        writeDots("uv", m_layer->sublayer("tex"), mesh, stream);
        if (m_layer->sublayers().contains("otex"))
        {
            stream << ",\n";
            writeDots("uv2", m_layer->sublayer("otex"), mesh, stream);
        }
        stream << ",\n";
        // Write default texture
        stream << "            \"texture\": {\"type\": \"WoodInner\", \"def\": \"Pine\", \"readonly\": false}\n";
        stream << "        }";
        if (mesh != meshes.last())
        {
            stream << ",";
        }
        stream << "\n";
        index++;
    }
    stream << "    ]\n";
    // Finish writing
    stream << "}";

    stream.flush();
    expFile.close();
    return true;
}

void ThreeExporter::writeVerticles(const QString& name, const QList<float>& dots, MeshLayer* mesh, QTextStream& stream)
{
    stream << "            \"" << name << "\": [";
    for (int j = 0; j < mesh->ind().size(); j += 3)
    {
        for (int ic = 0; ic < 3; ++ic)
        {
            // Get index
            int index = mesh->ind()[j + ic];
            // Check validity
            Q_ASSERT(index > -1 && index < dots.size() / 3);
            // Get vector by index
            float vx = dots[index * 3 + 0];
            float vy = dots[index * 3 + 1];
            float vz = dots[index * 3 + 2];
            // Rotate vector
            float yc = vy; // copy
            vy = yc * static_cast<float>(cos(-M_PI / 2)) - vz * static_cast<float>(sin(-M_PI / 2));
            vz = yc * static_cast<float>(sin(-M_PI / 2)) + vz * static_cast<float>(cos(-M_PI / 2));
            stream << vx << ", " << vy << ", " << vz;
            if (ic != 2)
                stream << ", ";
        }
        if (j < mesh->ind().size() - 3 - 1)
            stream << ", ";
    }
    stream << "]";
}

void ThreeExporter::writeDots(const QString& name, const QList<float>& dots, MeshLayer* mesh, QTextStream& stream) const
{
    stream << "            \"" << name << "\": [";
    for (int j = 0; j < mesh->ind().size(); j += 3)
    {
        for (int ic = 0; ic < 3; ++ic)
        {
            // Get index
            int index = mesh->ind()[j + ic];
            // Check validity
            Q_ASSERT(index > -1 && index < dots.size() / 2 && (index * 2) < dots.size());
            // Get point by index
            float px = dots[index * 2 + 0];
            float py = dots[index * 2 + 1];
            stream << px << ", " << py;

            if (ic != 2)
                stream << ", ";
        }
        if (j < mesh->ind().size() - 3 - 1)
            stream << ", ";
    }
    stream << "]";
}
