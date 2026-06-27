#ifndef LAYER
#define LAYER

#include <QByteArray>
#include <QString>
#include <QDebug>

#include "../streams/readstream.h"
#include "../streams/writestream.h"

class Resource;

// Generic abstract layer class
class Layer {

public:
    Layer(const QByteArray &data, Resource *res = nullptr) {
        m_isRemoved = false;
        m_edited = false;
        m_rawData = data;
        m_resource = res;
    }
    virtual ~Layer() {}
    virtual bool parse() = 0;
    virtual const QByteArray toByteArray() {return m_rawData;}
    virtual const QString type() const = 0;
    virtual const QString getError() const {return m_error;}
    virtual bool init() {return true;}
    virtual bool isRemoved() {return m_isRemoved;}
    virtual void setRemoved(bool r) {m_isRemoved = r;}
    virtual bool isEdited() {return m_edited;}
    virtual void setEdited(bool e) {m_edited = e;}
    virtual bool replace(const QByteArray& data) {
        if (data.isEmpty())
            return false;
        m_rawData = data;
        return parse();
    }
    const QByteArray& rawData() const { return m_rawData; }
    const QString &error() const { return m_error; }

protected:
    bool m_isRemoved;
    bool m_edited;
    QByteArray m_rawData;
    Resource *m_resource;
    QString m_error;


};

#endif // LAYER
