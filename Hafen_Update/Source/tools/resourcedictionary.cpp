#include "resourcedictionary.h"

ResourceDictionary::ResourceDictionary(QObject *parent) : QObject(parent)
{
    // Connect to database
    m_database = QSqlDatabase::addDatabase("QSQLITE");
    m_database.setDatabaseName("./tools/dictionary");
    if (!m_database.open()) {
        qDebug() << "Failed to open dictionary database:" << m_database.lastError().text();
    }
}

const QStringList ResourceDictionary::getList() const
{
    if (!m_database.isOpen())
        return QStringList();

    QSqlQuery query("SELECT path FROM resources ORDER BY path ASC");
    if (!query.exec())
        return QStringList();

    QStringList ret;
    while (query.next())
        ret << query.value(0).toString();
    return ret;
}

bool ResourceDictionary::addPath(const QString &path)
{
    if (!m_database.isOpen() || path.isEmpty())
        return false;

    QString _path = path.trimmed().toLower();

    QSqlQuery query;

    // Check if not exist
    if (!query.prepare("SELECT COUNT(*) FROM resources WHERE path = ?"))
        return false;
    query.bindValue(0, _path);
    if (!query.exec())
        return false;
    query.first();
    int c = query.value(0).toInt(0);
    if (c > 0)
        return false;

    // Insert new path
    query.clear();
    if (!query.prepare("INSERT INTO resources(path) VALUES(?)"))
        return false;

    query.bindValue(0, _path);
    if (!query.exec())
        return false;

    return true;
}
