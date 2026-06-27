#pragma once

#include <QList>
#include <QString>

class ObjImporter
{
public:
    ObjImporter();

    bool import(const QString& f);
    const QString getError() const
    {
        return m_error;
    }
    const QList<float>& pos() const
    {
        return m_pos;
    }
    const QList<float>& nrm() const
    {
        return m_nrm;
    }
    const QList<float>& tex() const
    {
        return m_tex;
    }
    const QList<int>& faces() const
    {
        return m_fac;
    }

protected:
    bool processFaces(const QStringList& faces);
    int faceToInt(const QString& face);

private:
    QString m_error;
    QList<float> m_pos;
    QList<float> m_nrm;
    QList<float> m_tex;
    QList<int> m_fac;
};
