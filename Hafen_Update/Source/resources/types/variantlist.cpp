#include "variantlist.h"
#include <QDebug>

VariantList::VariantList()
{
}

int VariantList::typeByString(const QString& t)
{
    if (t.isEmpty())
        return -1;

    QString lct = t;
    lct = lct.toLower();
    if (lct == "int")
    {
        return TYPE_INT;
    }
    else if (lct == "string")
    {
        return TYPE_STR;
    }
    else if (lct == "coord")
    {
        return TYPE_COORD;
    }
    else if (lct == "color")
    {
        return TYPE_COLOR;
    }
    else if (lct == "uchar")
    {
        return TYPE_UINT8;
    }
    else if (lct == "ushort")
    {
        return TYPE_UINT16;
    }
    else if (lct == "char")
    {
        return TYPE_INT8;
    }
    else if (lct == "short")
    {
        return TYPE_INT16;
    }
    else if (lct == "float")
    {
        return TYPE_FLOAT32;
    }
    else if (lct == "double")
    {
        return TYPE_FLOAT64;
    }
    else if (lct == "bytes")
    {
        return TYPE_BYTES;
    }

    return -1;
}

bool VariantList::add(int type, const QVariant& value)
{
    if (type < 0 || type == 13 || type > 16)
        return false;

    m_types << type;
    m_values << value;
    return true;
}

bool VariantList::add(const QString& type, const QVariant& value)
{
    return add(VariantList::typeByString(type), value);
}

bool VariantList::update(int index, const QVariant& value)
{
    if (index < 0 || index > m_types.size() - 1)
        return false;

    m_values[index] = value;
    return true;
}

bool VariantList::remove(int index)
{
    if (index < 0 || index > m_types.size() - 1)
        return false;
    m_types.removeAt(index);
    m_values.removeAt(index);
    return true;
}

int VariantList::size() const
{
    return m_types.size();
}

const QList<QVariant> VariantList::values() const
{
    return m_values;
}

const QVariant VariantList::value(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QVariant();
    return m_values.at(index);
}

int VariantList::type(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return -1;
    return m_types.at(index);
}

void VariantList::clear()
{
    m_types.clear();
    m_values.clear();
}

QVariant VariantList::last() const
{
    return m_values.last();
}

const QString VariantList::asNumber(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QString();

    int t = m_types.at(index);
    if (t == TYPE_FLOAT32)
    {
        return QString("%1").arg(m_values.at(index).toFloat(0));
    }
    else if (t == TYPE_FLOAT64)
    {
        return QString("%1").arg(m_values.at(index).toDouble(0));
    }
    else if (t == TYPE_INT || t == TYPE_INT8 || t == TYPE_INT16 || t == TYPE_UINT8 || t == TYPE_UINT16)
    {
        return QString("%1").arg(m_values.at(index).toInt(0));
    }

    return QString();
}

const QString VariantList::asColor(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QString();

    if (m_types.at(index) == TYPE_COLOR)
    {
        QColor c = qvariant_cast<QColor>(m_values.at(index));
        return QString("ARGB(%1, %2, %3, %4)").arg(c.alpha()).arg(c.red()).arg(c.green()).arg(c.blue());
    }

    return QString();
}

const QString VariantList::asCoord(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QString();

    if (m_types.at(index) == TYPE_COORD)
    {
        QPoint p = m_values.at(index).toPoint();
        return QString("(%1, %2)").arg(p.x()).arg(p.y());
    }

    return QString();
}

const QString VariantList::asString(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QString();

    if (m_types.at(index) == TYPE_STR)
        return m_values.at(index).toString();

    return QString();
}

const QString VariantList::valueToString(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QString();

    int type = m_types.at(index);
    if (type == TYPE_FLOAT32)
    {
        return QString("%1").arg(m_values.at(index).toFloat(0));
    }
    else if (type == TYPE_FLOAT64)
    {
        return QString("%1").arg(m_values.at(index).toDouble(0));
    }
    else if (type == TYPE_INT || type == TYPE_INT8 || type == TYPE_INT16 || type == TYPE_UINT8 || type == TYPE_UINT16)
    {
        return QString("%1").arg(m_values.at(index).toInt(0));
    }
    else if (type == TYPE_COLOR)
    {
        QColor c = qvariant_cast<QColor>(m_values.at(index));
        return QString("ARGB(%1, %2, %3, %4)").arg(c.alpha()).arg(c.red()).arg(c.green()).arg(c.blue());
    }
    else if (type == TYPE_COORD)
    {
        QPoint p = m_values.at(index).toPoint();
        return QString("(%1, %2)").arg(p.x()).arg(p.y());
    }
    else if (type == TYPE_STR)
    {
        return m_values.at(index).toString();
    }

    return QString();
}

const QString VariantList::typeString(int index) const
{
    if (index < 0 || index > m_types.size() - 1)
        return QString();

    int t = m_types.at(index);
    switch (t)
    {
        case TYPE_BYTES:
            return "Bytes";
        case TYPE_COLOR:
            return "Color";
        case TYPE_COORD:
            return "Coord";
        case TYPE_FLOAT32:
            return "Float";
        case TYPE_FLOAT64:
            return "Double";
        case TYPE_INT:
            return "Int";
        case TYPE_INT8:
            return "Char";
        case TYPE_INT16:
            return "Short";
        case TYPE_STR:
            return "String";
        case TYPE_UINT8:
            return "Uchar";
        case TYPE_UINT16:
            return "Ushort";
    }

    return "Undefined";
}
