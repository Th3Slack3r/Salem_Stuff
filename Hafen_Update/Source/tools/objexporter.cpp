#include "objexporter.h"
#include <QFile>
#include <QTextStream>
#include <QFileInfo>

#include <cmath>
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#include "resources/layers/meshlayer.h"

ObjExporter::ObjExporter(Vbuf2Layer* layer)
{
    m_layer = layer;
}

bool ObjExporter::saveAs(const QString& file, const QList<MeshLayer*>& meshes,
                         const QString& mtlFile, const QString& materialName)
{
    m_error.clear();
    if (!m_layer) {
        m_error = "Missing vbuf layer";
        return false;
    }
    if (file.isEmpty()) {
        m_error = "Invalid output file name";
        return false;
    }
    if (meshes.isEmpty()) {
        m_error = "No mesh layers to export";
        return false;
    }

    QFile expFile(file);
    if (!expFile.open(QIODevice::WriteOnly | QIODevice::Text)) {
        m_error = expFile.errorString();
        return false;
    }

    QTextStream stream(&expFile);
    stream.setRealNumberNotation(QTextStream::FixedNotation);
    stream.setRealNumberPrecision(6);

    bool hasPos = m_layer->sublayers().contains("pos");
    bool hasNrm = m_layer->sublayers().contains("nrm");
    bool hasTex = m_layer->sublayers().contains("tex") || m_layer->sublayers().contains("otex");

    if (!hasPos) {
        m_error = "VBUF has no position sublayer";
        expFile.close();
        return false;
    }

    QList<float> positions = m_layer->sublayer("pos");
    if (positions.isEmpty()) {
        m_error = "VBUF position data is empty";
        expFile.close();
        return false;
    }

    // Optional material reference
    if (!mtlFile.isEmpty()) {
        QFileInfo fi(mtlFile);
        stream << "mtllib " << fi.fileName() << "\n";
        if (!materialName.isEmpty())
            stream << "usemtl " << materialName << "\n";
    }

    // Writing vertexes
    writeSublayer("v", m_layer->dimension("pos"), positions, stream);

    // Writing normales
    writeSublayer("vn", m_layer->dimension("nrm"), m_layer->sublayer("nrm"), stream);

    // Writing skinning
    bool hasOtex = m_layer->sublayers().contains("otex");
    if (hasTex) {
        writeSublayer(
            "vt", m_layer->dimension(hasOtex ? "otex" : "tex"), m_layer->sublayer(hasOtex ? "otex" : "tex"), stream, true);
    }

    int vertexCount = positions.length() / m_layer->dimension("pos");
    if (vertexCount <= 0) {
        m_error = "VBUF position count is zero";
        expFile.close();
        return false;
    }

    // Writing faces by given ids
    for (int i = 0; i < meshes.length(); ++i)
    {
        MeshLayer* mesh = meshes.at(i);
        const QList<int> indices = mesh->ind();
        int len = indices.length();
        if (len == 0)
            continue;

        int maxIndex = indices.at(0);
        int minIndex = indices.at(0);
        for (int idx : indices) {
            if (idx > maxIndex) maxIndex = idx;
            if (idx < minIndex) minIndex = idx;
        }

        // Detect whether indices are 0-based or 1-based.
        bool zeroBased = (maxIndex < vertexCount);
        if (!zeroBased && maxIndex - 1 < vertexCount && minIndex >= 1) {
            zeroBased = false;
        }

        // Validate indices against vertex count.
        if (zeroBased && maxIndex >= vertexCount) {
            m_error = "Mesh indices exceed vertex count (0-based)";
            expFile.close();
            return false;
        }
        if (!zeroBased && maxIndex > vertexCount) {
            m_error = "Mesh indices exceed vertex count (1-based)";
            expFile.close();
            return false;
        }

        for (int j = 0; j + 2 < len; j += 3) {
            int v1 = zeroBased ? indices[j] + 1 : indices[j];
            int v2 = zeroBased ? indices[j + 1] + 1 : indices[j + 1];
            int v3 = zeroBased ? indices[j + 2] + 1 : indices[j + 2];

            // OBJ format: f v/vt/vn
            // If a value is missing, we leave it blank: e.g., "f 1//1" if no texture
            stream << "f "
                   << v1 << "/" << (hasTex ? QString::number(v1) : "") << "/" << (hasNrm ? QString::number(v1) : "") << " "
                   << v2 << "/" << (hasTex ? QString::number(v2) : "") << "/" << (hasNrm ? QString::number(v2) : "") << " "
                   << v3 << "/" << (hasTex ? QString::number(v3) : "") << "/" << (hasNrm ? QString::number(v3) : "") << "\n";
        }
    }

    stream.flush();
    expFile.close();
    return true;
}

const QVector3D ObjExporter::rotateVector(const QVector3D& v)
{
    // If the input itself is NaN, don't try to math it
    if (qIsNaN(v.x()) || qIsNaN(v.y()) || qIsNaN(v.z())) return v;

    float angle = -M_PI / 2.0f;
    float s = sin(angle);
    float c = cos(angle);

    QVector3D ret = v;
    // Y' = y*cos - z*sin
    // Z' = y*sin + z*cos
    ret.setY(v.y() * c - v.z() * s);
    ret.setZ(v.y() * s + v.z() * c);
    return ret;
}

const QString ObjExporter::vectorToString(const QVector3D& v)
{
    // Replace NaN with 0.0 so the 3D viewer doesn't crash/go blank
    float x = std::isfinite(v.x()) ? v.x() : 0.0f;
    float y = std::isfinite(v.y()) ? v.y() : 0.0f;
    float z = std::isfinite(v.z()) ? v.z() : 0.0f;

    return QString(" %1 %2 %3").arg(x, 0, 'f', 6).arg(y, 0, 'f', 6).arg(z, 0, 'f', 6);
}

void ObjExporter::writeSublayer(
    const QString& prefix, int dimension, const QList<float>& dots, QTextStream& stream, bool reverse)
{
    if (prefix.isEmpty() || dots.isEmpty())
        return;

    // Really weird shit goes here
    // for example vectors and normales shold be turned -pi/2
    // textures are also should be turned upside down
    // is it loftar's mistake or OGRE shit?
    // Also: I don't know what should be done for (bi)tangets, anyway I don't know how to unload them
    for (int i = 0; i < dots.length(); i += dimension)
    {
        stream << prefix;
        if (prefix == "v" || prefix == "vn")
        {
            QVector3D v(dots[i], dots[i + 1], dots[i + 2]);

            // DEBUG: Check if numbers are valid
            if (qIsNaN(v.x()) || qIsNaN(v.y()) || qIsNaN(v.z())) {
                qDebug() << "Warning: NaN detected in" << prefix << "at index" << i;
            }

            v = rotateVector(v);
            stream << vectorToString(v);
        }
        else
        {
            for (int j = 0; j < dimension; ++j)
            {
                float dot = (reverse && j ? 1.0f - dots[i + j] : dots[i + j]);
                stream << " " << dot;
            }
        }
        stream << "\n";
    }
}

