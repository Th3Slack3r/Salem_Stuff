#ifndef POINT3F_H
#define POINT3F_H

#include <QString>

class Point3F
{
public:
    Point3F(float x = 0.0, float y = 0.0, float z = 0.0);

    float x() const {return m_x;}
    float y() const {return m_y;}
    float z() const {return m_z;}

    void setX(float x) {m_x = x;}
    void setY(float y) {m_y = y;}
    void setZ(float z) {m_z = z;}

    const QString toString() const;

private:
    float m_x;
    float m_y;
    float m_z;
};

#endif // POINT3F_H
