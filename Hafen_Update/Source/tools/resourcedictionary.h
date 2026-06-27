#ifndef RESOURCEDICTIONARY_H
#define RESOURCEDICTIONARY_H

#include <QObject>
#include <QtSql>
//resources (id, path)

class ResourceDictionary : public QObject
{
    Q_OBJECT
public:
    explicit ResourceDictionary(QObject *parent = 0);

    const QStringList getList() const;
    bool addPath(const QString &path);

private:
    QSqlDatabase m_database;

signals:

public slots:
};

#endif // RESOURCEDICTIONARY_H
