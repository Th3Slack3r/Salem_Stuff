#ifndef XMLIMPORTER_H
#define XMLIMPORTER_H

#include <QList>
#include <QString>
#include <QVector3D>

class XmlImporter
{
public:
    XmlImporter();

    bool import(const QString &f);
    const QString getError() const {return m_error;}

    const QList<float> pos() const {return m_pos;}
    const QList<float> nrm() const {return m_nrm;}
    const QList<float> tex() const {return m_tex;}
    const QList<int> faces() const {return m_fac;}

private:
    QString m_error;
    QList<float> m_pos;
    QList<float> m_nrm;
    QList<float> m_tex;
    QList<int> m_fac;

    const QVector3D rotateVector(const QVector3D &v);
};

#endif // XMLIMPORTER_H
