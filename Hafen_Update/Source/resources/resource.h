#ifndef RESOURCE_H
#define RESOURCE_H

#include <QObject>
#include <QByteArray>
#include <QNetworkAccessManager>
#include <QList>

#include "layers/layer.h"

class Resource : public QObject
{
    Q_OBJECT

public:
    explicit Resource(const QString &path, QObject *parent = nullptr);
    ~Resource();
    void init();

    const QList<Layer *> layers() const;
    const QString getErrorMessage() const {return m_errorString;}
    const QString getName() const {return m_name;}
    short getVersion() {return m_version;}
    const QByteArray toByteArray() const;
    const QByteArray exportLayer(int index) const;
    bool replaceLayer(int index, const QByteArray& ba);
    void appendLayer(const QByteArray& ba);
    void makeUnmodified();
    bool isModified() const;
    bool isLocal() const {return m_local;}

private:
    QString m_errorString;
    QNetworkAccessManager *m_networkManager;
    QString m_path;
    QString m_name;
    QByteArray m_resourceData;
    short m_version;
    QList <Layer *> m_layers;
    bool m_local;

    bool loadLocalResource(const QString &p);
    void loadRemoteResource(const QString &p);
    Layer *createLayer(const QString &name, const QByteArray &data);
    void parseResource();
    void setErrorMessage(const QString &e);

signals:
    void resourceStartDownload();
    void resourceEndDownload();
    void resourceInited();
    void resourceError(const QString &error);

public slots:

private slots:
    void onResourceDownloaded();
};

#endif // RESOURCE_H
