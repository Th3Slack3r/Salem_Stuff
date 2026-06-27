#ifndef VBUF2LAYER_H
#define VBUF2LAYER_H

#include "layer.h"
#include "meshlayer.h"
#include "texlayer.h"
#include <QPixmap>

using BoneData = QList<QPair<int, QList<float>>>;

class Vbuf2Layer : public Layer
{
public:
    struct Bones {
        QList<QPair<QString, BoneData>> data = {};
        int unk1 = 0;
        int size = 0;
    };

    Vbuf2Layer(const QByteArray &d, Resource *r = nullptr);
    const QString& error() const { return m_error; }
    virtual const QString type() const;
    virtual const QByteArray toByteArray();
    virtual bool parse();
    virtual bool init();

    const QList<float> sublayer(const QString &s) const;
    int dimension(const QString &d) const;
    const QList<QString> sublayers() const;
    const QList<MeshLayer *> meshes() const;
    const QList<TexLayer *> textures() const;
    const QString resName() const;
    const Bones& bones() const { return m_bones; }
    int vertexCount() const { return m_num > 0 ? m_num : (m_sublayers.value("pos").length() / 3); }

    void setSublayerList(const QString &s, const QList<float> &l);

private:
    int m_fl;
    int m_id;
    int m_num;
    QMap<QString, QList<float> > m_sublayers;
    Bones m_bones;

    //
    QMap<QString, int> m_dimensions;
    QMap<QString, int> mSubSizes;
    QList<MeshLayer *> m_meshes;
    QList<TexLayer *> m_textures;

    void readBones(ReadStream &s);
    void writeBones(WriteStream &s);
};

#endif // VBUF2LAYER_H
