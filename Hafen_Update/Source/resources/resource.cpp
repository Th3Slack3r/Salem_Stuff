#include "resource.h"

#include <QFile>
#include <QNetworkReply>
#include <QNetworkRequest>

#include "streams/readstream.h"
#include "streams/writestream.h"

#include "layers/actionlayer.h"
#include "layers/audio2layer.h"
#include "layers/audiolayer.h"
#include "layers/clamblayer.h"
#include "layers/codeentrylayer.h"
#include "layers/codelayer.h"
#include "layers/foodevlayer.h"
#include "layers/imagelayer.h"
#include "layers/mat2layer.h"
#include "layers/meshlayer.h"
#include "layers/neglayer.h"
#include "layers/paginalayer.h"
#include "layers/rlinklayer.h"
#include "layers/skanlayer.h"
#include "layers/skellayer.h"
#include "layers/tilelayer.h"
#include "layers/tilesetlayer.h"
#include "layers/tileset2layer.h"
#include "layers/texlayer.h"
#include "layers/tooltiplayer.h"
#include "layers/unknownlayer.h"
#include "layers/vbuf2layer.h"
#include "layers/srclayer.h"

Resource::Resource(const QString &path, QObject *parent) : QObject(parent)
{   
    m_path = m_name = path;
    m_networkManager = nullptr;

    // Make name
    m_local = !m_path.startsWith("http://game.havenandhearth.com/res/");
    if (!m_local) {
        m_name = m_name.replace("http://game.havenandhearth.com/res/", "").replace(".res", "");
    } else {
        m_name = (m_name.mid(m_name.lastIndexOf("/") + 1)).split(".")[0];
    }
}

Resource::~Resource()
{
    // Remove all layers from resource before destroy
    foreach (Layer *l, m_layers)
        delete l;
}

void Resource::init()
{
    if (m_path.isNull() || m_path.isEmpty()) {
        setErrorMessage("Invalid resource path");
        //TODO: emit error
        return;
    }

    if (m_local) {
        loadLocalResource(m_path);
        parseResource();
    } else {
        loadRemoteResource(m_path);
    }
}

const QList<Layer *> Resource::layers() const
{
    return m_layers;
}

const QByteArray Resource::toByteArray() const
{
    QByteArray ret;
    WriteStream stream(&ret);
    stream.writeBytes("Haven Resource 1");
    stream.writeShort(m_version);
    // Writing layers
    foreach (Layer *l, m_layers) {
        // Skip removed layer
        if (l->isRemoved())
            continue;
        // Writing type
        stream.writeString(l->type());
        // Writing layer size
        QByteArray ld = l->toByteArray();
        stream.writeInt(ld.size());
        // Sriting layer data
        if (ld.size())
            stream.writeBytes(ld);
    }

    return ret;
}

const QByteArray Resource::exportLayer(int index) const
{
    if (index < 0 || index >= m_layers.size())
        return QByteArray{};

    QByteArray ret;
    ret = m_layers.at(index)->toByteArray();
    if (ret.isEmpty())
        return ret;
    const char eos = 0;
    ret.prepend(eos);
    ret.prepend(m_layers.at(index)->type().toLatin1());
    return ret;
}

bool Resource::replaceLayer(int index, const QByteArray& ba)
{
    if (ba.isEmpty() || index < 0 || index >= m_layers.size())
        return false;

    ReadStream r(&ba);
    const QString type = r.getString();
    if (type.isEmpty())
        return false;
    if (type != m_layers.at(index)->type())
        return false;
    auto layer = r.getBytes();
    bool result = m_layers[index]->replace(layer);
    m_layers[index]->setEdited(true);
    return result;
}

void Resource::appendLayer(const QByteArray& ba)
{
    if (ba.isEmpty())
        return;
    ReadStream r(&ba);
    const QString type = r.getString();
    if (type.isEmpty())
        return;
    auto layer = r.getBytes();
    if (layer.isEmpty())
        return;

    auto l = createLayer(type, layer);
    if (l && l->parse()) {
        l->init();
        m_layers.append(l);
    }
}

void Resource::makeUnmodified()
{
    QList<Layer *> tmp;
    foreach (Layer *l, m_layers) {
        if (l->isRemoved()) {
            delete l;
            continue;
        }
        if (l->isEdited())
            l->setEdited(false);
        tmp << l;
    }
    m_layers = tmp;
}

bool Resource::isModified() const
{
    foreach (Layer *l, m_layers)
        if (l->isEdited())
            return true;

    return false;
}

bool Resource::loadLocalResource(const QString &p)
{
    QFile tmpFile(p);
    if (!tmpFile.open(QIODevice::ReadOnly)) {
        setErrorMessage("Failed to open file");
        return false;
    }

    m_resourceData = tmpFile.readAll();
    tmpFile.close();

    return true;
}

void Resource::loadRemoteResource(const QString &p)
{
    if (!m_networkManager)
        m_networkManager = new QNetworkAccessManager(this);
    QNetworkReply *reply = m_networkManager->get(QNetworkRequest(QUrl(p)));
    connect(reply, SIGNAL(finished()), SLOT(onResourceDownloaded()));
    emit resourceStartDownload();
}

Layer *Resource::createLayer(const QString &name, const QByteArray &data)
{
    Layer *layer = nullptr;

    if (name == "action") {
        layer = new ActionLayer(data, this);
    } else if (name == "audio2") {
        layer = new Audio2Layer(data, this);
    } else if (name == "audio") {
        layer = new AudioLayer(data, this);
    } else if (name == "clamb") {
        layer = new ClambLayer(data, this);
    } else if (name == "codeentry") {
        layer = new CodeentryLayer(data, this);
    } else if (name == "code") {
        layer = new CodeLayer(data, this);
    } else if (name == "foodev") {
        layer = new FoodevLayer(data, this);
    } else if (name == "image") {
        layer = new ImageLayer(data, this);
    } else if (name == "mat2") {
        layer = new Mat2Layer(data, this);
    } else if (name == "mesh") {
        layer = new MeshLayer(data, this);
    } else if (name == "neg") {
        layer = new NegLayer(data, this);
    } else if (name == "pagina") {
        layer = new PaginaLayer(data, this);
    } else if (name == "rlink") {
        layer = new RlinkLayer(data, this);
    } else if (name == "skan") {
        layer = new SkanLayer(data, this);
    } else if (name == "skel") {
        layer = new SkelLayer(data, this);
    } else if (name == "tile") {
        layer = new TileLayer(data, this);
    } else if (name == "tileset") {
        layer = new TilesetLayer(data, this);
    } else if (name == "tileset2") {
        layer = new Tileset2Layer(data, this);
    } else if (name == "tex") {
        layer = new TexLayer(data, this);
    } else if (name == "tooltip") {
        layer = new TooltipLayer(data, this);
    } else if (name == "vbuf" || name == "vbuf2") {
        layer = new Vbuf2Layer(data, this);
    } else if (name == "src") {
        layer = new SrcLayer(data, this);
    } else {
        layer = new UnknownLayer(name, data, this);
    }

    return layer;
}

void Resource::parseResource()
{
    if (m_resourceData.isNull() || m_resourceData.isEmpty()) {
        //TODO: emit error
        return;
    }

    ReadStream stream(&m_resourceData);
    try {
        if (stream.getBytes(16) != "Haven Resource 1") {
            qDebug() << "Not a Hafen resource";
            //TODO: emit error
            return;
        }
        m_version = stream.getShort();
        while (!stream.atEnd()) {
            // Read layer type as string
            QString name = stream.getString();
            // Read layer size
            int size = stream.getInt();
            // Read layer bytes
            QByteArray ld = stream.getBytes(size);
            // Create layer
            Layer *layer = createLayer(name, ld);
            if (!layer)
                continue;
            if (!layer->parse()) {
                QString err = layer->getError();
                qDebug() << "Failed to parse layer" << name << "Adding as unknown; Parse error:" << err;
                //TODO: store errors
                delete layer;
                UnknownLayer *ul = new UnknownLayer(name, ld, this);
                ul->setErrorString(err);
                layer = ul;
            }
            //qDebug() << name << layer;
            m_layers << layer;
        }
    } catch (...) {
        //
        qDebug() << "An error occured while parsing resource";
    }

    // Init layers
    foreach (Layer *l, m_layers) {
        l->init();
        // TODO: check errors
    }

    emit resourceInited();
}

void Resource::setErrorMessage(const QString &e)
{
    m_errorString = e;
    emit resourceError(e);
}

void Resource::onResourceDownloaded()
{
    emit resourceEndDownload();
    QNetworkReply *reply = qobject_cast<QNetworkReply *>(sender());
    if (!reply) {
        setErrorMessage("Failed to download remote resource");
        return;
    }

    reply->deleteLater();
    int replyCode = reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
    qDebug() << "Remote resource downloading reply code:" << replyCode;
    if (replyCode != 200) {
        setErrorMessage(QString("Resource downloading failed, server response with code %1").arg(replyCode));
        return;
    }

    m_resourceData = reply->readAll();
    parseResource();
}
