#include "readstream.h"
#include "math.h"
#include <QDebug>

ReadStream::ReadStream(const QByteArray* array)
{
    m_array = array;
    m_pos = 0;
}

bool ReadStream::atEnd()
{
    return m_pos >= m_array->size();
}

void ReadStream::skip(int length)
{
    if (m_pos + length > m_array->size() - 1)
    {
        throw runtime_error(QString("[skip] Out of bound").toLocal8Bit().constData());
    }

    m_pos += length;
}

char ReadStream::getChar()
{
    if (m_pos >= m_array->size())
    {
        throw runtime_error(QString("[getChar] Out of bound").toLocal8Bit().constData());
    }
    return (*m_array)[m_pos++];
}

short ReadStream::getShort()
{
    if (m_pos + 1 >= m_array->size())
    {
        throw runtime_error(QString("[getShort] Out of bound").toLocal8Bit().constData());
    }
    short s = ((unsigned char)((*m_array)[m_pos])) | ((unsigned char)((*m_array)[m_pos + 1])) << 8;
    m_pos += 2;
    return s;
}

QPoint ReadStream::getCoord()
{
    int x = getInt();
    int y = getInt();
    return QPoint(x, y);
}

QPoint ReadStream::getCoord16()
{
    int x = getShort();
    int y = getShort();
    return QPoint(x, y);
}

int ReadStream::getInt()
{
    if (m_pos + 3 >= m_array->size())
    {
        throw runtime_error(QString("[getInt] Out of bound").toLocal8Bit().constData());
    }
    int s = ((unsigned char)(*m_array)[m_pos]) | ((unsigned char)(*m_array)[m_pos + 1]) << 8 |
            ((unsigned char)(*m_array)[m_pos + 2]) << 16 | ((unsigned char)(*m_array)[m_pos + 3]) << 24;
    m_pos += 4;
    return s;
}

long ReadStream::getLong()
{
    if (m_pos + 7 >= m_array->size())
    {
        throw runtime_error(QString("[getLong] Out of bound").toLocal8Bit().constData());
    }
    long l(0);
    for (int i = 0; i < 8; ++i)
        l |= ((long)((unsigned char)(*m_array)[m_pos + i])) << (i * 8);
    return l;
}

float ReadStream::getFloat()
{
    int i = getInt();
    return intBitsToFloat(i);
}

double ReadStream::getDouble()
{
    long l = getLong();
    return longBitsToDouble(l);
}

double ReadStream::getFloatD()
{
    int e = getChar();
    int t = (unsigned int)getInt();
    int m = (int)(t & 0x7fffffffL);
    bool s = (t & 0x80000000L) != 0;
    if (e == -128 && !m)
    {
        return 0.0;
    }
    double v = (((double)m) / 2147483648.0) + 1.0;
    if (s)
        v = -v;
    double ret = pow(2.0, e) * v;
    return ret;
}

QString ReadStream::getString()
{
    QByteArray buffer;
    bool terminated = false;
    while (!atEnd()) {
        auto c = m_array->at(m_pos++);
        if (!c) {
            terminated = true;
            break;
        }
        buffer.append(c);
    }
    if (!terminated)
        throw runtime_error("[getString] Out of bound");
    return QString{buffer};
}

QByteArray ReadStream::getBytes(int length)
{
    if (length < 0)
    {
        if (m_pos == m_array->size())
        {
            throw runtime_error(QString("[getBytes] Stream is already at end").toLocal8Bit().constData());
        }
        QByteArray a = m_array->mid(m_pos);
        m_pos = m_array->size();
        return a;
    }
    if (m_pos + length - 1 >= m_array->size())
    {
        throw runtime_error(QString("[getBytes] Out of bound").toLocal8Bit().constData());
    }
    QByteArray a = m_array->mid(m_pos, length);
    m_pos += length;
    return a;
}

QColor ReadStream::getColor()
{
    /*QColor ret = QColor(
                (unsigned char)getChar(),
                (unsigned char)getChar(),
                (unsigned char)getChar(),
                (unsigned char)getChar()
                );*/
    // i've added this method when worked on mat2 layer
    // then i've noticed that after reading from ReadStream, colors are always random
    // enjoy your ub++
    int r, g, b, a;
    r = (unsigned char)getChar();
    g = (unsigned char)getChar();
    b = (unsigned char)getChar();
    a = (unsigned char)getChar();
    return QColor(r, g, b, a);
}

VariantList ReadStream::getList()
{
    VariantList ret;
    while (1)
    {
        if (atEnd())
            return ret;
        int t = (unsigned char)getChar();
        switch (t)
        {
            case TYPE_END:
                return ret;
            case TYPE_INT:
                ret.add(TYPE_INT, QVariant(getInt()));
                break;
            case TYPE_STR:
                ret.add(TYPE_STR, QVariant(getString()));
                break;
            case TYPE_COORD:
                ret.add(TYPE_COORD, QVariant(getCoord()));
                break;
            case TYPE_UINT8:
                ret.add(TYPE_UINT8, QVariant((unsigned char)getChar()));
                break;
            case TYPE_UINT16:
                ret.add(TYPE_UINT16, QVariant((unsigned short)getShort()));
                break;
            case TYPE_INT8:
                ret.add(TYPE_INT8, QVariant(getChar()));
                break;
            case TYPE_INT16:
                ret.add(TYPE_INT16, QVariant(getShort()));
                break;
            case TYPE_COLOR:
                ret.add(TYPE_COLOR, QVariant(getColor()));
                break;
            case TYPE_NIL:
                ret.add(TYPE_NIL, QVariant(char(0)));
                break;
            case TYPE_BYTES:
            {
                int len = (unsigned char)getChar();
                if (len & 128)
                    len = getInt();
                ret.add(TYPE_BYTES, QVariant(getBytes(len)));
                break;
            }
            case TYPE_FLOAT32:
                ret.add(TYPE_FLOAT32, QVariant(getFloat()));
                break;
            case TYPE_FLOAT64:
                ret.add(TYPE_FLOAT64, QVariant(getDouble()));
                break;
            case TYPE_TTOL:
                getList();
                break; // TODO: register user type
        }
    }

    return ret;
}

int ReadStream::size() const
{
    return m_array->size();
}

int ReadStream::position() const
{
    return m_pos;
}
unsigned int ReadStream::changeEndianess(const unsigned int x)
{
    return (x >> 24) | ((x << 8) & 0x00FF0000) | ((x >> 8) & 0x0000FF00) | (x << 24);
}

float ReadStream::intBitsToFloat(const int x)
{
    union {
        float f;
        int i;
    } u;

    u.i = x;
    return u.f;
}

double ReadStream::longBitsToDouble(const long x)
{
    union {
        double d;
        long l;
    } u;

    u.l = x;
    return u.d;
}
