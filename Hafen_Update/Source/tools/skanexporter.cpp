#include "skanexporter.h"

#include <QFile>
#include <QFileInfo>
#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>
#include <QtMath>

#include "../resources/layers/skanlayer.h"

namespace {

static void appendPadding(QByteArray &bin)
{
    const int pad = (4 - (bin.size() % 4)) % 4;
    if (pad)
        bin.append(QByteArray(pad, '\0'));
}

static void appendFloat(QByteArray &bin, float v)
{
    bin.append(reinterpret_cast<const char *>(&v), sizeof(float));
}

static int addAccessor(QJsonArray &accessors,
                       QJsonArray &bufferViews,
                       QByteArray &bin,
                       const QByteArray &payload,
                       int componentType,
                       const QString &type,
                       int count)
{
    appendPadding(bin);
    const int byteOffset = bin.size();
    bin.append(payload);

    QJsonObject view;
    view["buffer"] = 0;
    view["byteOffset"] = byteOffset;
    view["byteLength"] = payload.size();
    bufferViews.append(view);
    const int viewIndex = bufferViews.size() - 1;

    QJsonObject acc;
    acc["bufferView"] = viewIndex;
    acc["componentType"] = componentType;
    acc["count"] = count;
    acc["type"] = type;
    accessors.append(acc);
    return accessors.size() - 1;
}

static QByteArray packFloats(const QVector<float> &vals)
{
    QByteArray out;
    out.reserve(vals.size() * sizeof(float));
    for (float v : vals)
        appendFloat(out, v);
    return out;
}

static void axisAngleToQuat(float ax, float ay, float az, float angle,
                            float &qx, float &qy, float &qz, float &qw)
{
    const float len = qSqrt(ax * ax + ay * ay + az * az);
    if (len < 1e-6f || qIsNaN(len)) {
        qx = qy = qz = 0.0f;
        qw = 1.0f;
        return;
    }
    ax /= len; ay /= len; az /= len;
    const float half = angle * 0.5f;
    const float s = qSin(half);
    qx = ax * s;
    qy = ay * s;
    qz = az * s;
    qw = qCos(half);
}

}

bool SkanExporter::exportGltf(const SkanLayer *layer, const QString &gltfPath, QString *error)
{
    if (!layer) {
        if (error) *error = "No skan layer";
        return false;
    }

    const QList<SkanLayer::Bone> bones = layer->bones();
    if (bones.isEmpty()) {
        if (error) *error = "No bones to export";
        return false;
    }

    QFileInfo gltfInfo(gltfPath);
    const QString baseName = gltfInfo.completeBaseName();
    const QString dirPath = gltfInfo.absolutePath();
    const QString binName = baseName + ".bin";
    const QString binPath = dirPath + "/" + binName;

    QJsonArray nodes;
    QJsonArray rootChildren;

    QJsonObject rootNode;
    rootNode["name"] = "skan_root";
    nodes.append(rootNode);

    for (int i = 0; i < bones.size(); ++i) {
        QJsonObject n;
        n["name"] = bones.at(i).name;
        nodes.append(n);
        rootChildren.append(i + 1);
    }
    {
        QJsonObject rootObj = nodes.at(0).toObject();
        rootObj.insert("children", rootChildren);
        nodes[0] = rootObj;
    }

    QJsonArray accessors;
    QJsonArray bufferViews;
    QByteArray bin;

    QJsonArray samplers;
    QJsonArray channels;

    for (int i = 0; i < bones.size(); ++i) {
        const SkanLayer::Bone &b = bones.at(i);
        if (b.frames.isEmpty())
            continue;

        QVector<float> times;
        QVector<float> translations;
        QVector<float> rotations;
        times.reserve(b.frames.length());
        translations.reserve(b.frames.length() * 3);
        rotations.reserve(b.frames.length() * 4);

        for (const SkanLayer::Frame &f : b.frames) {
            times.append(f.tm);
            translations.append(f.tx);
            translations.append(f.ty);
            translations.append(f.tz);
            float qx, qy, qz, qw;
            axisAngleToQuat(f.rax, f.ray, f.raz, f.rang, qx, qy, qz, qw);
            rotations.append(qx);
            rotations.append(qy);
            rotations.append(qz);
            rotations.append(qw);
        }

        const int timeAccessor = addAccessor(accessors, bufferViews, bin,
                                             packFloats(times), 5126, "SCALAR", times.size());
        const int transAccessor = addAccessor(accessors, bufferViews, bin,
                                              packFloats(translations), 5126, "VEC3", times.size());
        const int rotAccessor = addAccessor(accessors, bufferViews, bin,
                                            packFloats(rotations), 5126, "VEC4", times.size());

        QJsonObject sampTrans;
        sampTrans["input"] = timeAccessor;
        sampTrans["output"] = transAccessor;
        samplers.append(sampTrans);
        int sampTransIdx = samplers.size() - 1;

        QJsonObject chanTrans;
        chanTrans["sampler"] = sampTransIdx;
        QJsonObject targetTrans;
        targetTrans["node"] = i + 1;
        targetTrans["path"] = "translation";
        chanTrans["target"] = targetTrans;
        channels.append(chanTrans);

        QJsonObject sampRot;
        sampRot["input"] = timeAccessor;
        sampRot["output"] = rotAccessor;
        samplers.append(sampRot);
        int sampRotIdx = samplers.size() - 1;

        QJsonObject chanRot;
        chanRot["sampler"] = sampRotIdx;
        QJsonObject targetRot;
        targetRot["node"] = i + 1;
        targetRot["path"] = "rotation";
        chanRot["target"] = targetRot;
        channels.append(chanRot);
    }

    QJsonObject anim;
    anim["name"] = "skan";
    anim["samplers"] = samplers;
    anim["channels"] = channels;
    QJsonArray animations;
    animations.append(anim);

    QJsonArray buffers;
    QJsonObject buf;
    buf["byteLength"] = bin.size();
    buf["uri"] = binName;
    buffers.append(buf);

    QJsonArray scenes;
    QJsonObject scene;
    QJsonArray sceneNodes;
    sceneNodes.append(0);
    scene["nodes"] = sceneNodes;
    scenes.append(scene);

    QJsonObject root;
    root["asset"] = QJsonObject{{"version", "2.0"}, {"generator", "HafenResTool skan exporter"}};
    root["scene"] = 0;
    root["scenes"] = scenes;
    root["nodes"] = nodes;
    root["animations"] = animations;
    root["buffers"] = buffers;
    root["bufferViews"] = bufferViews;
    root["accessors"] = accessors;

    QJsonDocument doc(root);
    QFile gltfFile(gltfPath);
    if (!gltfFile.open(QIODevice::WriteOnly | QIODevice::Truncate)) {
        if (error) *error = "Failed to write glTF file";
        return false;
    }
    gltfFile.write(doc.toJson(QJsonDocument::Indented));
    gltfFile.close();

    QFile binFile(binPath);
    if (!binFile.open(QIODevice::WriteOnly | QIODevice::Truncate)) {
        if (error) *error = "Failed to write glTF .bin file";
        return false;
    }
    binFile.write(bin);
    binFile.close();

    return true;
}
