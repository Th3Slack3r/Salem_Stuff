#include "vbuf2layer.h"
#include "../resource.h"
#include <QtEndian>

Vbuf2Layer::Vbuf2Layer(const QByteArray &d, Resource *r)
    : Layer(d, r)
    , m_id(0)
{
    // Fill dimensions
    m_dimensions["pos"] = 3; // x y z
    m_dimensions["nrm"] = 3; // x y z
    m_dimensions["col"] = 4; // r g b a
    m_dimensions["tex"] = 2; // x y (texture skinning?)
    m_dimensions["tan"] = 3; // tangets
    m_dimensions["bit"] = 3; // bitangets
    m_dimensions["otex"] = 2; // overtex (sorta grayscaling magic?)

    // Init bones as empty
    m_bones.size = 0;
}

const QString Vbuf2Layer::type() const
{
    return "vbuf2";
}

const QByteArray Vbuf2Layer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    ws.writeChar(static_cast<unsigned char>(m_fl));
    char ver = m_fl &0xf;
    if (ver > 0)
        ws.writeShort(static_cast<unsigned short>(m_id));
    ws.writeShort(static_cast<unsigned short>(m_num));
    QList<QString> dims = m_dimensions.keys();
    for (const auto &dim : qAsConst(dims)) {
        if (m_sublayers.contains(dim)) {
            // Write sublayer name
            ws.writeString(dim);
            if (ver > 0) {
                if (mSubSizes.contains(dim))
                    ws.writeInt(mSubSizes.value(dim));
                else
                    return QByteArray();
            }
            // Write dots
            for (int i = 0; i < m_sublayers[dim].length(); ++i)
                ws.writeFloat(m_sublayers[dim][i]);
        }
    }
    // Special behaviour
    if (m_bones.size) {
        writeBones(ws);
    }

    return ret;
}
bool Vbuf2Layer::parse()
{
    if (m_rawData.isEmpty()) return false;
    ReadStream s(&m_rawData);

    try {
        // Read flags + vertex count (little-endian short)
        m_fl = (unsigned char)s.getChar();
        m_num = (unsigned short)s.getShort();

        // Safety: if it looks corrupted, swap bytes once
        if (m_num > 10000) {
            m_num = (unsigned short)((m_num >> 8) | (m_num << 8));
        }

        qDebug() << "VBUF vertex count =" << m_num;

        while (!s.atEnd()) {
            int id = (unsigned char)s.getChar();
            if (id < 0 || id > 3)
                break; // Unknown data format, stop here

            if (id >= 0 && id <= 2) {
                int dim = (id == 2) ? 2 : 3;
                QList<float> dots;
                dots.reserve(m_num * dim);
                for (int i = 0; i < m_num * dim; ++i) {
                    if (s.atEnd()) break;
                    dots << static_cast<float>(s.getFloatD());
                }

                if (id == 0) m_sublayers.insert("pos", dots);
                else if (id == 1) m_sublayers.insert("nrm", dots);
                else if (id == 2) m_sublayers.insert("tex", dots);
            } else if (id == 3) {
                // Bone data; parse and continue
                readBones(s);
            }
        }

        return m_sublayers.contains("pos");
    } catch (...) {
        return !m_sublayers.isEmpty();
    }
}

bool Vbuf2Layer::init()
{
    if (!m_resource) {
        m_error = "Missing resource reference for layer vbuf2";
        return false;
    }

    foreach (Layer *l, m_resource->layers()) {
        if (l->type() == "mesh")
            m_meshes << dynamic_cast<MeshLayer *>(l);
        else if (l->type() == "tex")
            m_textures << dynamic_cast<TexLayer *>(l);
    }

    return true;
}

const QList<float> Vbuf2Layer::sublayer(const QString &s) const
{
    if (s.isEmpty() || !m_sublayers.contains(s))
        return QList<float>();

    return m_sublayers.value(s, QList<float>());
}

int Vbuf2Layer::dimension(const QString &d) const
{
    if (d.isEmpty() || !m_dimensions.contains(d))
        return -1;

    return m_dimensions.value(d, -1);
}

const QList<QString> Vbuf2Layer::sublayers() const
{
    return m_sublayers.keys();
}

const QList<MeshLayer *> Vbuf2Layer::meshes() const
{
    return m_meshes;
}

const QList<TexLayer *> Vbuf2Layer::textures() const
{
    return m_textures;
}

const QString Vbuf2Layer::resName() const
{
    return m_resource->getName();
}

void Vbuf2Layer::setSublayerList(const QString &s, const QList<float> &l)
{
    if (s.isEmpty() || !m_sublayers.contains(s) || l.length() % m_dimensions.value(s))
        return;

    m_sublayers[s] = l;
}

void Vbuf2Layer::readBones(ReadStream &s)
{
    m_bones.unk1 = s.getInt();
    m_bones.size = (unsigned char)s.getChar();
    while (true) {
        const QString bone = s.getString();
        if (bone.isEmpty())
            break;

        BoneData data;
        while (true) {
           auto run = (unsigned short)s.getShort();
           int vn = (unsigned short)s.getShort();
           if (run == 0) {
               data << qMakePair(vn, QList<float>());
               break;
           }
           QList<float> values;
           for (int i = 0; i < run; ++i)
               values << static_cast<float>(s.getFloatD());
           data << qMakePair(vn, values);
        }

        m_bones.data << qMakePair(bone, data);
    }
}

void Vbuf2Layer::writeBones(WriteStream& s)
{
    s.writeString("bones");
    s.writeInt(m_bones.unk1);
    s.writeChar((unsigned char)m_bones.size);
    for (const auto& bone : qAsConst(m_bones.data)) {
        s.writeString(bone.first);
        const auto& bdata = bone.second;
        for (const auto& bd : qAsConst(bdata)) {
            s.writeShort((unsigned short)bd.second.size());
            s.writeShort((unsigned short)bd.first);
            for (const auto& f : bd.second) {
                s.writeFloat(f);
            }
        }
    }
    // Must end with empty string
    s.writeChar(0);
}
