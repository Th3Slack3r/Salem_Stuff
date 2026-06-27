#ifndef TRANSFORMER_H
#define TRANSFORMER_H

#if 0
#include <QObject>
#include <QMatrix4x4>

namespace Qt3DCore {
class QTransform;
}

class Transformer : public QObject
{
    Q_OBJECT
    Q_PROPERTY(Qt3DCore::QTransform* target READ target WRITE setTarget NOTIFY targetChanged)
    Q_PROPERTY(float radius READ radius WRITE setRadius NOTIFY radiusChanged)
    Q_PROPERTY(float angle READ angle WRITE setAngle NOTIFY angleChanged)

public:
    explicit Transformer(QObject *parent = 0);

    void setTarget(Qt3DCore::QTransform *target);
    Qt3DCore::QTransform *target() const;

    void setRadius(float radius);
    float radius() const;

    void setAngle(float angle);
    float angle() const;

signals:
    void targetChanged();
    void radiusChanged();
    void angleChanged();

protected:
    void updateMatrix();

private:
    Qt3DCore::QTransform *m_target;
    QMatrix4x4 m_matrix;
    float m_radius;
    float m_angle;
};
#endif

#endif // TRANSFORMER_H
