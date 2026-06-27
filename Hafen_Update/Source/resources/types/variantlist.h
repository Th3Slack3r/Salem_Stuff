#ifndef VARIANTLIST_H
#define VARIANTLIST_H

#define TYPE_END 0
#define TYPE_INT 1
#define TYPE_STR 2
#define TYPE_COORD 3
#define TYPE_UINT8 4
#define TYPE_UINT16 5
#define TYPE_COLOR 6
#define TYPE_TTOL 8
#define TYPE_INT8 9
#define TYPE_INT16 10
#define TYPE_NIL 12
#define TYPE_BYTES 14
#define TYPE_FLOAT32 15
#define TYPE_FLOAT64 16

#include <QColor>
#include <QList>
#include <QPoint>
#include <QVariant>

class VariantList
{
public:
    VariantList();

    static int typeByString(const QString& t);

    bool add(int type, const QVariant& value);
    bool add(const QString& type, const QVariant& value);
    bool update(int index, const QVariant& value);
    bool remove(int index);
    int size() const;
    int length() const
    {
        return size();
    }
    const QList<QVariant> values() const;
    const QVariant value(int index) const;
    int type(int index) const;
    void clear();
    QVariant last() const;

    const QString asNumber(int index) const;
    const QString asColor(int index) const;
    const QString asCoord(int index) const;
    const QString asString(int index) const;
    const QString valueToString(int index) const;
    const QString typeString(int index) const;

private:
    QList<int> m_types;
    QList<QVariant> m_values;
};

#endif // VARIANTLIST_H
