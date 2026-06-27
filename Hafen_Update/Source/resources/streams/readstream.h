#ifndef READSTREAM_H
#define READSTREAM_H

#include <QString>
#include <QByteArray>
#include <QPoint>
#include <QColor>
#include <QVariant>
#include <exception>
#include <QDebug>
#include "../types/variantlist.h"

#include <stdexcept>

using namespace std;


class ReadStream {

public:
    ReadStream(const QByteArray *array);

    bool atEnd();
    void skip(int length);
    char getChar();
    short getShort();
    int getInt();
    long getLong();
    float getFloat();
    double getDouble();
    double getFloatD();
    QPoint getCoord();
    QPoint getCoord16();
    QString getString();
    QByteArray getBytes(int length = -1);
    QColor getColor();
    VariantList getList();

    int size() const;
    int position() const;

private:
    unsigned int changeEndianess(const unsigned int x);
    float intBitsToFloat(const int x);
    double longBitsToDouble(const long x);

protected:
    const QByteArray *m_array;
    int m_pos;

};


#endif
