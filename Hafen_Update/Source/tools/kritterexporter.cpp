#include "kritterexporter.h"

#include <QDir>
#include <QFile>
#include <QFileInfo>
#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>
#include <QMap>
#include <QMatrix4x4>
#include <QQuaternion>
#include <QVector3D>
#include <QStringList>
#include <QtMath>
#include <functional>

#include "../resources/resource.h"
#include "../resources/layers/mat2layer.h"
#include "../resources/layers/meshlayer.h"
#include "../resources/layers/skanlayer.h"
#include "../resources/layers/skellayer.h"
#include "../resources/layers/texlayer.h"
#include "../resources/layers/vbuf2layer.h"

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

static void appendU16(QByteArray &bin, quint16 v)
{
    bin.append(reinterpret_cast<const char *>(&v), sizeof(quint16));
}

static void appendU32(QByteArray &bin, quint32 v)
{
    bin.append(reinterpret_cast<const char *>(&v), sizeof(quint32));
}

static QByteArray packFloats(const QVector<float> &vals)
{
    QByteArray out;
    out.reserve(vals.size() * sizeof(float));
    for (float v : vals)
        appendFloat(out, v);
    return out;
}

static QByteArray packU16(const QVector<quint16> &vals)
{
    QByteArray out;
    out.reserve(vals.size() * sizeof(quint16));
    for (quint16 v : vals)
        appendU16(out, v);
    return out;
}

static QByteArray packU32(const QVector<quint32> &vals)
{
    QByteArray out;
    out.reserve(vals.size() * sizeof(quint32));
    for (quint32 v : vals)
        appendU32(out, v);
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

static QMatrix4x4 makeTRS(float tx, float ty, float tz, float qx, float qy, float qz, float qw)
{
    QMatrix4x4 m;
    m.setToIdentity();
    QQuaternion q(qw, qx, qy, qz);
    m.translate(tx, ty, tz);
    m.rotate(q);
    return m;
}

static int addAccessor(QJsonArray &accessors,
                       QJsonArray &bufferViews,
                       QByteArray &bin,
                       const QByteArray &payload,
                       int componentType,
                       const QString &type,
                       int count,
                       const QJsonArray &min = QJsonArray(),
                       const QJsonArray &max = QJsonArray())
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
    if (!min.isEmpty())
        acc["min"] = min;
    if (!max.isEmpty())
        acc["max"] = max;
    accessors.append(acc);
    return accessors.size() - 1;
}

static int extractTexIdFromMat2(const Mat2Layer* mat)
{
    if (!mat) return -1;
    const QMap<QString, VariantList> keyargs = mat->keyargs();

    auto getNumericFromList = [](const VariantList& list, bool fromEnd) -> int {
        if (fromEnd) {
            for (int i = list.size() - 1; i >= 0; --i) {
                const QVariant v = list.value(i);
                if (v.canConvert<int>())
                    return v.toInt();
            }
        } else {
            for (int i = 0; i < list.size(); ++i) {
                const QVariant v = list.value(i);
                if (v.canConvert<int>())
                    return v.toInt();
            }
        }
        return -1;
    };

    if (keyargs.contains("tex")) {
        int id = getNumericFromList(keyargs.value("tex"), false);
        if (id >= 0) return id;
    }
    if (keyargs.contains("texlink")) {
        int id = getNumericFromList(keyargs.value("texlink"), true);
        if (id >= 0) return id;
    }

    for (auto it = keyargs.constBegin(); it != keyargs.constEnd(); ++it) {
        const QString key = it.key().toLower();
        if (key == "tex") {
            int id = getNumericFromList(it.value(), false);
            if (id >= 0) return id;
        } else if (key == "texlink") {
            int id = getNumericFromList(it.value(), true);
            if (id >= 0) return id;
        }
    }

    return -1;
}

static Resource* loadResource(const QString &path, QObject *parent, QString *error)
{
    if (!QFileInfo::exists(path)) {
        if (error) *error = "Resource not found: " + path;
        return nullptr;
    }
    Resource *res = new Resource(path, parent);
    res->init();
    if (res->layers().isEmpty()) {
        if (error) *error = res->getErrorMessage().isEmpty() ? "Failed to parse resource: " + path : res->getErrorMessage();
        delete res;
        return nullptr;
    }
    return res;
}

} // namespace

bool KritterExporter::exportFolderToGltf(const QString &folderPath, const QString &gltfPath, QString *error)
{
    QDir dir(folderPath);
    if (!dir.exists()) {
        if (error) *error = "Folder not found";
        return false;
    }

    const QString baseName = QFileInfo(folderPath).fileName();
    const QString baseResPath = dir.filePath(baseName + ".res");

    Resource *base = loadResource(baseResPath, nullptr, error);
    if (!base)
        return false;

    Vbuf2Layer *vbuf = nullptr;
    SkelLayer *skel = nullptr;
    QList<MeshLayer*> meshes;
    QMap<int, TexLayer*> texById;
    QMap<int, Mat2Layer*> matById;

    for (Layer *layer : base->layers()) {
        if (layer->type() == "vbuf" || layer->type() == "vbuf2")
            vbuf = dynamic_cast<Vbuf2Layer*>(layer);
        else if (layer->type() == "skel")
            skel = dynamic_cast<SkelLayer*>(layer);
        else if (layer->type() == "mesh") {
            MeshLayer *m = dynamic_cast<MeshLayer*>(layer);
            if (m) meshes << m;
        } else if (layer->type() == "tex") {
            TexLayer *t = dynamic_cast<TexLayer*>(layer);
            if (t) texById.insert(t->id(), t);
        } else if (layer->type() == "mat2") {
            Mat2Layer *m = dynamic_cast<Mat2Layer*>(layer);
            if (m) matById.insert(m->id(), m);
        }
    }

    if (!vbuf || meshes.isEmpty() || !skel) {
        if (error) *error = "Base resource is missing vbuf/mesh/skel data.";
        delete base;
        return false;
    }

    if (vbuf->sublayers().isEmpty())
        vbuf->parse();

    const QList<float> pos = vbuf->sublayer("pos");
    if (pos.isEmpty()) {
        if (error) *error = "No position data found in vbuf.";
        delete base;
        return false;
    }

    const int vertexCount = pos.length() / 3;
    const QList<float> nrm = vbuf->sublayer("nrm");
    const QList<float> tex = vbuf->sublayer("tex");

    // Build bone mapping
    const QList<QPair<QString, SkelLayer::Bone>> skelBones = skel->bones();
    QMap<QString, int> boneIndex;
    QMap<QString, int> boneIndexLower;
    for (int i = 0; i < skelBones.length(); ++i) {
        const QString name = skelBones.at(i).first;
        boneIndex.insert(name, i);
        boneIndexLower.insert(name.toLower(), i);
    }

    // Compute weights/joints
    QVector<QVector<QPair<int, float>>> weightsPerVertex;
    weightsPerVertex.resize(vertexCount);
    const Vbuf2Layer::Bones &boneData = vbuf->bones();
    for (const auto &bonePair : boneData.data) {
        const QString boneName = bonePair.first;
        int bIndex = -1;
        if (boneIndex.contains(boneName))
            bIndex = boneIndex.value(boneName);
        else if (boneIndexLower.contains(boneName.toLower()))
            bIndex = boneIndexLower.value(boneName.toLower());
        if (bIndex < 0)
            continue;
        const BoneData &segments = bonePair.second;
        for (const auto &seg : segments) {
            const int vn = seg.first;
            const QList<float> &vals = seg.second;
            if (vals.isEmpty())
                break;
            for (int i = 0; i < vals.size(); ++i) {
                const int vi = vn + i;
                if (vi < 0 || vi >= vertexCount)
                    continue;
                const float w = vals.at(i);
                if (w <= 0.0f)
                    continue;
                weightsPerVertex[vi].append(qMakePair(bIndex, w));
            }
        }
    }

    QVector<quint16> joints;
    QVector<float> weights;
    joints.resize(vertexCount * 4);
    weights.resize(vertexCount * 4);

    for (int i = 0; i < vertexCount; ++i) {
        auto &list = weightsPerVertex[i];
        std::sort(list.begin(), list.end(), [](const QPair<int, float> &a, const QPair<int, float> &b) {
            return a.second > b.second;
        });
        float sum = 0.0f;
        int count = qMin(4, list.size());
        for (int j = 0; j < count; ++j)
            sum += list[j].second;
        if (sum <= 0.0f) {
            joints[i * 4 + 0] = 0;
            joints[i * 4 + 1] = 0;
            joints[i * 4 + 2] = 0;
            joints[i * 4 + 3] = 0;
            weights[i * 4 + 0] = 1.0f;
            weights[i * 4 + 1] = 0.0f;
            weights[i * 4 + 2] = 0.0f;
            weights[i * 4 + 3] = 0.0f;
            continue;
        }
        for (int j = 0; j < 4; ++j) {
            if (j < count) {
                joints[i * 4 + j] = (quint16)list[j].first;
                weights[i * 4 + j] = list[j].second / sum;
            } else {
                joints[i * 4 + j] = 0;
                weights[i * 4 + j] = 0.0f;
            }
        }
    }

    // Build node list: root + bones + mesh node
    QJsonArray nodes;
    QJsonArray rootChildren;

    QJsonObject rootNode;
    rootNode["name"] = "kritter_root";
    nodes.append(rootNode);

    // Bone nodes
    QMap<QString, QString> parentOf;
    for (const auto &pair : skelBones)
        parentOf.insert(pair.first, pair.second.bp);

    for (int i = 0; i < skelBones.length(); ++i) {
        const auto &pair = skelBones.at(i);
        const SkelLayer::Bone &b = pair.second;
        float qx, qy, qz, qw;
        axisAngleToQuat(b.rax.x(), b.rax.y(), b.rax.z(), b.rang, qx, qy, qz, qw);
        QJsonObject n;
        n["name"] = pair.first;
        QJsonArray t;
        t.append(b.pos.x());
        t.append(b.pos.y());
        t.append(b.pos.z());
        n["translation"] = t;
        QJsonArray r;
        r.append(qx);
        r.append(qy);
        r.append(qz);
        r.append(qw);
        n["rotation"] = r;
        nodes.append(n);
    }

    // Mesh node
    QJsonObject meshNode;
    meshNode["name"] = "kritter_mesh";
    meshNode["mesh"] = 0;
    meshNode["skin"] = 0;
    nodes.append(meshNode);
    const int meshNodeIndex = skelBones.length() + 1;

    // Build bone hierarchy children
    QMap<int, QJsonArray> childMap;
    for (int i = 0; i < skelBones.length(); ++i) {
        const QString name = skelBones.at(i).first;
        const QString parentName = parentOf.value(name);
        if (parentName.isEmpty() || !boneIndex.contains(parentName)) {
            rootChildren.append(i + 1);
        } else {
            const int parentIdx = boneIndex.value(parentName);
            childMap[parentIdx].append(i + 1);
        }
    }
    for (auto it = childMap.constBegin(); it != childMap.constEnd(); ++it) {
        QJsonObject obj = nodes.at(it.key() + 1).toObject();
        obj["children"] = it.value();
        nodes[it.key() + 1] = obj;
    }

    rootChildren.append(meshNodeIndex);
    {
        QJsonObject obj = nodes.at(0).toObject();
        obj["children"] = rootChildren;
        nodes[0] = obj;
    }

    // Build inverse bind matrices
    QVector<QMatrix4x4> globalMats;
    QVector<bool> globalReady;
    globalMats.resize(skelBones.length());
    globalReady.resize(skelBones.length());
    std::function<QMatrix4x4(int)> computeGlobal = [&](int idx) -> QMatrix4x4 {
        if (globalReady[idx]) {
            return globalMats[idx];
        }
        const auto &pair = skelBones.at(idx);
        const SkelLayer::Bone &b = pair.second;
        float qx, qy, qz, qw;
        axisAngleToQuat(b.rax.x(), b.rax.y(), b.rax.z(), b.rang, qx, qy, qz, qw);
        QMatrix4x4 local = makeTRS(b.pos.x(), b.pos.y(), b.pos.z(), qx, qy, qz, qw);

        const QString parentName = b.bp;
        if (!parentName.isEmpty() && boneIndex.contains(parentName)) {
            const int pIdx = boneIndex.value(parentName);
            globalMats[idx] = computeGlobal(pIdx) * local;
        } else {
            globalMats[idx] = local;
        }
        globalReady[idx] = true;
        return globalMats[idx];
    };

    QVector<float> invBindFloats;
    invBindFloats.reserve(skelBones.length() * 16);
    for (int i = 0; i < skelBones.length(); ++i) {
        QMatrix4x4 g = computeGlobal(i);
        QMatrix4x4 inv = g.inverted();
        const float *d = inv.constData();
        for (int k = 0; k < 16; ++k)
            invBindFloats.append(d[k]);
    }

    // Prepare glTF containers
    QJsonArray accessors;
    QJsonArray bufferViews;
    QByteArray bin;

    // Positions
    QVector<float> posVals;
    posVals.reserve(pos.length());
    for (float v : pos)
        posVals.append(v);
    QJsonArray minPos;
    QJsonArray maxPos;
    if (vertexCount > 0) {
        float minX = posVals[0], minY = posVals[1], minZ = posVals[2];
        float maxX = minX, maxY = minY, maxZ = minZ;
        for (int i = 0; i < vertexCount; ++i) {
            const float x = posVals[i * 3 + 0];
            const float y = posVals[i * 3 + 1];
            const float z = posVals[i * 3 + 2];
            minX = qMin(minX, x); minY = qMin(minY, y); minZ = qMin(minZ, z);
            maxX = qMax(maxX, x); maxY = qMax(maxY, y); maxZ = qMax(maxZ, z);
        }
        minPos = QJsonArray{minX, minY, minZ};
        maxPos = QJsonArray{maxX, maxY, maxZ};
    }
    const int posAccessor = addAccessor(accessors, bufferViews, bin,
                                        packFloats(posVals), 5126, "VEC3", vertexCount, minPos, maxPos);

    int nrmAccessor = -1;
    if (!nrm.isEmpty()) {
        QVector<float> nrmVals;
        nrmVals.reserve(nrm.length());
        for (float v : nrm)
            nrmVals.append(v);
        nrmAccessor = addAccessor(accessors, bufferViews, bin,
                                  packFloats(nrmVals), 5126, "VEC3", vertexCount);
    }

    int texAccessor = -1;
    if (!tex.isEmpty()) {
        QVector<float> texVals;
        texVals.reserve(tex.length());
        for (float v : tex)
            texVals.append(v);
        texAccessor = addAccessor(accessors, bufferViews, bin,
                                  packFloats(texVals), 5126, "VEC2", vertexCount);
    }

    const int jointsAccessor = addAccessor(accessors, bufferViews, bin,
                                           packU16(joints), 5123, "VEC4", vertexCount);
    const int weightsAccessor = addAccessor(accessors, bufferViews, bin,
                                            packFloats(weights), 5126, "VEC4", vertexCount);

    const int invBindAccessor = addAccessor(accessors, bufferViews, bin,
                                            packFloats(invBindFloats), 5126, "MAT4", skelBones.length());

    // Materials & textures
    QJsonArray materials;
    QJsonArray images;
    QJsonArray textures;
    QJsonArray samplers;
    QMap<int, int> texIdToTexIndex;
    QMap<int, int> texIdToMatIndex;
    int defaultMaterial = -1;

    if (!texById.isEmpty()) {
        QJsonObject samp;
        samp["magFilter"] = 9729;
        samp["minFilter"] = 9729;
        samplers.append(samp);
    }

    auto ensureMaterialForTex = [&](int texId) -> int {
        if (texIdToMatIndex.contains(texId))
            return texIdToMatIndex.value(texId);

        TexLayer *t = texById.value(texId, nullptr);
        if (!t || t->image().isEmpty())
            return -1;

        const QString imgName = QString("tex_%1.png").arg(texId);
        const QString imgPath = QFileInfo(gltfPath).absolutePath() + "/" + imgName;
        QFile imgFile(imgPath);
        if (imgFile.open(QIODevice::WriteOnly | QIODevice::Truncate)) {
            imgFile.write(t->image());
            imgFile.close();
        }

        QJsonObject img;
        img["uri"] = imgName;
        images.append(img);
        const int imgIndex = images.size() - 1;

        QJsonObject texObj;
        if (!samplers.isEmpty())
            texObj["sampler"] = 0;
        texObj["source"] = imgIndex;
        textures.append(texObj);
        const int texIndex = textures.size() - 1;
        texIdToTexIndex.insert(texId, texIndex);

        QJsonObject mat;
        QJsonObject pbr;
        QJsonObject baseTex;
        baseTex["index"] = texIndex;
        pbr["baseColorTexture"] = baseTex;
        pbr["metallicFactor"] = 0.0;
        pbr["roughnessFactor"] = 1.0;
        mat["pbrMetallicRoughness"] = pbr;
        materials.append(mat);
        const int matIndex = materials.size() - 1;
        texIdToMatIndex.insert(texId, matIndex);
        return matIndex;
    };

    auto ensureDefaultMaterial = [&]() -> int {
        if (defaultMaterial >= 0)
            return defaultMaterial;
        QJsonObject mat;
        QJsonObject pbr;
        QJsonArray baseColor;
        baseColor.append(1.0);
        baseColor.append(1.0);
        baseColor.append(1.0);
        baseColor.append(1.0);
        pbr["baseColorFactor"] = baseColor;
        pbr["metallicFactor"] = 0.0;
        pbr["roughnessFactor"] = 1.0;
        mat["pbrMetallicRoughness"] = pbr;
        materials.append(mat);
        defaultMaterial = materials.size() - 1;
        return defaultMaterial;
    };

    // Mesh primitives
    QJsonArray primitives;
    for (MeshLayer *m : meshes) {
        const QList<int> ind = m->ind();
        if (ind.isEmpty())
            continue;
        int maxIndex = 0;
        for (int v : ind)
            if (v > maxIndex) maxIndex = v;

        bool useU32 = maxIndex > 65535;
        int idxAccessor = -1;
        if (useU32) {
            QVector<quint32> idx;
            idx.reserve(ind.size());
            for (int v : ind) idx.append((quint32)v);
            idxAccessor = addAccessor(accessors, bufferViews, bin,
                                      packU32(idx), 5125, "SCALAR", idx.size());
        } else {
            QVector<quint16> idx;
            idx.reserve(ind.size());
            for (int v : ind) idx.append((quint16)v);
            idxAccessor = addAccessor(accessors, bufferViews, bin,
                                      packU16(idx), 5123, "SCALAR", idx.size());
        }

        QJsonObject attrs;
        attrs["POSITION"] = posAccessor;
        if (nrmAccessor >= 0) attrs["NORMAL"] = nrmAccessor;
        if (texAccessor >= 0) attrs["TEXCOORD_0"] = texAccessor;
        attrs["JOINTS_0"] = jointsAccessor;
        attrs["WEIGHTS_0"] = weightsAccessor;

        QJsonObject prim;
        prim["attributes"] = attrs;
        prim["indices"] = idxAccessor;

        int matIndex = -1;
        int matId = m->matid();
        if (matById.contains(matId)) {
            int texId = extractTexIdFromMat2(matById.value(matId));
            if (texId >= 0)
                matIndex = ensureMaterialForTex(texId);
        }
        if (matIndex < 0)
            matIndex = ensureDefaultMaterial();
        prim["material"] = matIndex;
        primitives.append(prim);
    }

    QJsonArray meshesJson;
    QJsonObject meshObj;
    meshObj["name"] = "kritter_mesh";
    meshObj["primitives"] = primitives;
    meshesJson.append(meshObj);

    // Skin
    QJsonArray jointsJson;
    for (int i = 0; i < skelBones.length(); ++i)
        jointsJson.append(i + 1);
    QJsonObject skin;
    skin["joints"] = jointsJson;
    skin["inverseBindMatrices"] = invBindAccessor;
    if (!jointsJson.isEmpty())
        skin["skeleton"] = jointsJson.at(0).toInt();
    QJsonArray skins;
    skins.append(skin);

    // Animations from folder
    QJsonArray animations;
    QMap<QString, QVector3D> restPosByName;
    QMap<QString, QQuaternion> restRotByName;
    for (const auto &pair : skelBones) {
        const QString name = pair.first;
        const SkelLayer::Bone &b = pair.second;
        float qx, qy, qz, qw;
        axisAngleToQuat(b.rax.x(), b.rax.y(), b.rax.z(), b.rang, qx, qy, qz, qw);
        restPosByName.insert(name, QVector3D(b.pos.x(), b.pos.y(), b.pos.z()));
        restRotByName.insert(name, QQuaternion(qw, qx, qy, qz));
    }
    QStringList resFiles = dir.entryList(QStringList() << "*.res", QDir::Files);
    for (const QString &resName : resFiles) {
        const QString path = dir.filePath(resName);
        Resource *r = loadResource(path, nullptr, nullptr);
        if (!r)
            continue;
        SkanLayer *skan = nullptr;
        SkanLayer tempSkan(QByteArray(), nullptr);
        for (Layer *l : r->layers()) {
            if (l->type() == "skan") {
                skan = dynamic_cast<SkanLayer*>(l);
                if (!skan) {
                    tempSkan = SkanLayer(l->rawData(), nullptr);
                    if (tempSkan.parse())
                        skan = &tempSkan;
                }
                break;
            }
        }
        if (!skan) {
            delete r;
            continue;
        }

        QJsonArray samplers;
        QJsonArray channels;

        for (const SkanLayer::Bone &b : skan->bones()) {
            int nodeIndex = -1;
            if (boneIndex.contains(b.name))
                nodeIndex = boneIndex.value(b.name) + 1;
            else if (boneIndexLower.contains(b.name.toLower()))
                nodeIndex = boneIndexLower.value(b.name.toLower()) + 1;
            if (nodeIndex < 0 || b.frames.isEmpty())
                continue;

            QString boneName = b.name;
            if (!restPosByName.contains(boneName)) {
                const QString lower = boneName.toLower();
                for (auto it = restPosByName.constBegin(); it != restPosByName.constEnd(); ++it) {
                    if (it.key().toLower() == lower) {
                        boneName = it.key();
                        break;
                    }
                }
            }

            QVector<float> times;
            QVector<float> translations;
            QVector<float> rotations;
            times.reserve(b.frames.length());
            translations.reserve(b.frames.length() * 3);
            rotations.reserve(b.frames.length() * 4);

            const QVector3D restPos = restPosByName.value(boneName, QVector3D(0,0,0));
            const QQuaternion restRot = restRotByName.value(boneName, QQuaternion(1,0,0,0));

            for (const SkanLayer::Frame &f : b.frames) {
                times.append(f.tm);
                translations.append(restPos.x() + f.tx);
                translations.append(restPos.y() + f.ty);
                translations.append(restPos.z() + f.tz);
                float qx, qy, qz, qw;
                axisAngleToQuat(f.rax, f.ray, f.raz, f.rang, qx, qy, qz, qw);
                QQuaternion delta(qw, qx, qy, qz);
                QQuaternion combined = restRot * delta;
                rotations.append(combined.x());
                rotations.append(combined.y());
                rotations.append(combined.z());
                rotations.append(combined.scalar());
            }

            QJsonArray minTime, maxTime;
            if (!times.isEmpty()) {
                float tmin = times[0], tmax = times[0];
                for (float t : times) { tmin = qMin(tmin, t); tmax = qMax(tmax, t); }
                minTime = QJsonArray{tmin};
                maxTime = QJsonArray{tmax};
            }

            const int timeAccessor = addAccessor(accessors, bufferViews, bin,
                                                 packFloats(times), 5126, "SCALAR", times.size(), minTime, maxTime);
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
            targetTrans["node"] = nodeIndex;
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
            targetRot["node"] = nodeIndex;
            targetRot["path"] = "rotation";
            chanRot["target"] = targetRot;
            channels.append(chanRot);
        }

        if (!channels.isEmpty()) {
            QJsonObject anim;
            anim["name"] = QFileInfo(resName).completeBaseName();
            anim["samplers"] = samplers;
            anim["channels"] = channels;
            animations.append(anim);
        }

        delete r;
    }

    QJsonArray buffers;
    QJsonObject buf;
    buf["byteLength"] = bin.size();
    buf["uri"] = QFileInfo(gltfPath).completeBaseName() + ".bin";
    buffers.append(buf);

    QJsonArray scenes;
    QJsonObject scene;
    QJsonArray sceneNodes;
    sceneNodes.append(0);
    scene["nodes"] = sceneNodes;
    scenes.append(scene);

    QJsonObject root;
    root["asset"] = QJsonObject{{"version", "2.0"}, {"generator", "HafenResTool Kritter Getter"}};
    root["scene"] = 0;
    root["scenes"] = scenes;
    root["nodes"] = nodes;
    root["meshes"] = meshesJson;
    root["skins"] = skins;
    root["animations"] = animations;
    root["buffers"] = buffers;
    root["bufferViews"] = bufferViews;
    root["accessors"] = accessors;
    if (!materials.isEmpty())
        root["materials"] = materials;
    if (!textures.isEmpty())
        root["textures"] = textures;
    if (!images.isEmpty())
        root["images"] = images;
    if (!samplers.isEmpty())
        root["samplers"] = samplers;

    QJsonDocument doc(root);
    QFile gltfFile(gltfPath);
    if (!gltfFile.open(QIODevice::WriteOnly | QIODevice::Truncate)) {
        if (error) *error = "Failed to write glTF file";
        delete base;
        return false;
    }
    gltfFile.write(doc.toJson(QJsonDocument::Indented));
    gltfFile.close();

    const QString binPath = QFileInfo(gltfPath).absolutePath() + "/" + QFileInfo(gltfPath).completeBaseName() + ".bin";
    QFile binFile(binPath);
    if (!binFile.open(QIODevice::WriteOnly | QIODevice::Truncate)) {
        if (error) *error = "Failed to write glTF .bin file";
        delete base;
        return false;
    }
    binFile.write(bin);
    binFile.close();

    delete base;
    return true;
}
