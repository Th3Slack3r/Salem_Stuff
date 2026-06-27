#include "xmlimporter.h"
#include <QDebug>
#include <QDomDocument>
#include <QDomElement>
#include <QDomNode>
#include <QFile>

#define M_PI 3.14159

XmlImporter::XmlImporter()
{
    //
}

bool XmlImporter::import(const QString& f)
{
    if (f.isEmpty())
    {
        m_error = "Invalid file name";
        return false;
    }

    QDomDocument doc("mesh");
    QFile xf(f);
    if (!xf.open(QIODevice::ReadOnly))
    {
        m_error = "Failed to open import file";
        return false;
    }

    if (!doc.setContent(&xf))
    {
        xf.close();
        m_error = "Failed to parse file contents";
        return false;
    }

    QDomElement mesh = doc.firstChild().nextSibling().toElement();
    if (mesh.isNull() || mesh.tagName() != "mesh")
    {
        m_error = "Cannot find 'mesh' tag at document";
        return false;
    }

    QDomElement sharedgeometry = mesh.firstChild().toElement();
    if (sharedgeometry.isNull() || sharedgeometry.tagName() != "sharedgeometry")
    {
        m_error = "Cannot find 'sharedgeometry' tag at 'mesh'";
        return false;
    }

    int vertexcount = sharedgeometry.attribute("vertexcount", "0").toInt(nullptr);
    if (vertexcount < 1)
    {
        m_error = "Invalid vertexcount for tag 'sharedgeometry'";
        return false;
    }

    QDomElement vertexbuffer = sharedgeometry.firstChild().toElement();
    if (vertexbuffer.isNull() || vertexbuffer.tagName() != "vertexbuffer")
    {
        m_error = "Cannot find 'vertexbuffer' tag at 'sharedgeometry'";
        return false;
    }

    QList<float> pos, nrm, tex;
    // Reading vertexes: pos, nrm, tex
    for (QDomNode node = vertexbuffer.firstChild(); !node.isNull(); node = node.nextSibling())
    {
        for (QDomNode verVals = node.firstChild(); !verVals.isNull(); verVals = verVals.nextSibling())
        {
            QDomElement el = verVals.toElement();
            if (el.isNull())
                continue;
            if (el.tagName() == "position")
            {
                QVector3D v(el.attribute("x").toFloat(), el.attribute("y").toFloat(), el.attribute("z").toFloat());
                v = rotateVector(v);
                pos << v.x() << v.y() << v.z();
            }
            else if (el.tagName() == "normal")
            {
                QVector3D v(el.attribute("x").toFloat(), el.attribute("y").toFloat(), el.attribute("z").toFloat());
                v = rotateVector(v);
                nrm << v.x() << v.y() << v.z();
            }
            else if (el.tagName() == "texcoord")
            {
                tex << el.attribute("u").toFloat() << el.attribute("v").toFloat();
            }
        }
    }

    bool check = (pos.length() / 3 + nrm.length() / 3 + tex.length() / 2) / 3 == vertexcount;
    if (!check)
    {
        m_error = "Array size mismatch from vertex count";
        return false;
    }

    // Code below should be fixed later

    QDomElement submeshes = sharedgeometry.nextSibling().toElement();
    if (submeshes.isNull() || submeshes.tagName() != "submeshes")
    {
        m_error = "Cannot find 'submeshes' tag at document";
        return false;
    }

    QDomElement faces = submeshes.firstChild().firstChild().toElement();
    if (faces.isNull() || faces.tagName() != "faces")
    {
        m_error = "Cannot find 'faces' tag at submeshes";
        return false;
    }

    int facesCount = faces.attribute("count").toInt(nullptr);
    QList<int> fcs;
    for (QDomNode face = faces.firstChild(); !face.isNull(); face = face.nextSibling())
    {
        QDomElement el = face.toElement();
        if (el.isNull())
            continue;
        fcs << el.attribute("v1").toInt() << el.attribute("v2").toInt() << el.attribute("v3").toInt();
    }

    if (fcs.length() / 3 != facesCount)
    {
        m_error = "Faces count mismatch";
        return false;
    }

    m_pos = pos;
    m_nrm = nrm;
    m_tex = tex;
    m_fac = fcs;

    xf.close();
    return true;
}

const QVector3D XmlImporter::rotateVector(const QVector3D& v)
{
    QVector3D ret = v;
    ret.setY(v.y() * cos(M_PI) - v.z() * sin(M_PI));
    ret.setZ(v.y() * sin(M_PI) + v.z() * cos(M_PI));
    return ret;
}
