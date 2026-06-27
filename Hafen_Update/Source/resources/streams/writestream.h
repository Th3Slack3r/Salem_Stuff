#ifndef WRITESTREAM_H
#define WRITESTREAM_H

#include <QByteArray>
#include <QString>
#include <QPoint>
#include <QPixmap>
#include <QBuffer>
#include <QColor>
#include <QDebug>
#include "../types/variantlist.h"

class WriteStream
{
public:
    WriteStream(QByteArray *array);
    void writeChar(char c);
    void writeShort(short s);
    void writeInt(int i);
    void writeLong(long l);
    void writeFloat(float f);
    void writeDouble(double d);
    void writeFloatD(double d);
    void writeCoord(const QPoint &p);
    void writeCoord16(const QPoint &p);
    void writeBytes(const QByteArray &ba);
    void writeString(const QString &s);
    void writeImage(const QPixmap &i);
    void writeColor(const QColor &c);
    void writeList(const VariantList &l);
    void writeNulls(int n);

private:
    QByteArray *m_array;

    int floatBitsToInt(const float f);
    long doubleBitsToLong(const double d);
};

#endif // WRITESTREAM_H
