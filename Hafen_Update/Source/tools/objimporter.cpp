#include "objimporter.h"

#include <QTextStream>
#include <QFile>

static const int G_INVALID = -65535;

ObjImporter::ObjImporter()
{
}

bool ObjImporter::import(const QString& f)
{
    if (f.isEmpty())
    {
        m_error = "Invalid file name";
        return false;
    }

    QFile objf(f);
    if (!objf.open(QIODevice::ReadOnly))
    {
        m_error = "Failed to open import file";
        return false;
    }

    QTextStream stream(&objf);
    QString line;
    while (stream.readLineInto(&line))
    {
        QStringList splitted = line.split(' ');
        if (splitted.size() < 3)
            continue;

        const QString& section = splitted.at(0);
        if (section == "v")
        {
            m_pos.append(splitted.at(1).toFloat());
            m_pos.append(splitted.at(2).toFloat());
            m_pos.append(splitted.at(3).toFloat());
        }
        else if (section == "vt")
        {
            m_tex.append(splitted.at(1).toFloat());
            m_tex.append(splitted.at(2).toFloat());
        }
        else if (section == "vn")
        {
            m_nrm.append(splitted.at(1).toFloat());
            m_nrm.append(splitted.at(2).toFloat());
            m_nrm.append(splitted.at(3).toFloat());
        }
        else if (section == "f")
        {
            splitted.removeFirst();
            bool result = processFaces(splitted);
            if (!result)
                return false;
        }
    }

    return true;
}

bool ObjImporter::processFaces(const QStringList& faces)
{
    if (faces.size() == 3)
    {
        for (const auto& face : qAsConst(faces))
        {
            int index = faceToInt(face);
            if (index == G_INVALID)
                return false;
            m_fac.append(index);
        }
    }
    else if (faces.size() > 3)
    {
        int initial = faceToInt(faces.at(0));
        if (initial == G_INVALID)
            return false;
        for (int i = 1; i < faces.size() - 1; ++i)
        {
            int p2 = faceToInt(faces.at(i));
            int p3 = faceToInt(faces.at(i + 1));
            if (p2 == G_INVALID || p3 == G_INVALID)
                return false;
            m_fac.append({initial, p2, p3});
        }
    }

    return true;
}

int ObjImporter::faceToInt(const QString& face)
{
    auto list = face.split('/');
    if (list.size() != 3)
    {
        m_error = "Invalid face: " + face;
        return G_INVALID;
    }
    bool equal = list.at(0) == list.at(1) && list.at(0) == list.at(2);
    if (!equal)
    {
        m_error = "Face elements should be equal: " + face;
        return G_INVALID;
    }

    return list.at(0).toInt() - 1; // Since indexes for faces starts from '1'
}
