#pragma once
#include <QString>
#include <QList>

// Any vertex buffer layer must implement this
class IVertexBufferLayer {
public:
    virtual ~IVertexBufferLayer() {}

    // Returns number of floats per element (pos=3, nrm=3, tex=2...)
    virtual int dimension(const QString &name) const = 0;

    // Returns the float data for the sublayer
    virtual const QList<float> sublayer(const QString &name) const = 0;

    // Returns all sublayer names
    virtual const QList<QString> sublayers() const = 0;
};
