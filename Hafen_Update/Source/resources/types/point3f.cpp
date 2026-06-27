#include "point3f.h"

Point3F::Point3F(float x, float y, float z)
{
    m_x = x;
    m_y = y;
    m_z = z;
}

const QString Point3F::toString() const
{
    return QString("Coord3f(%1, %2, %3)").arg(m_x).arg(m_y).arg(m_z);
}
