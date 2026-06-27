#include "writestream.h"
#include "math.h"
#include <QtCore/QBuffer>

WriteStream::WriteStream(QByteArray *array)
{
    m_array = array;
}

void WriteStream::writeChar(char c)
{
    m_array->append(c);
}

void WriteStream::writeShort(short s)
{
    writeChar((unsigned char) s);
    short t = s >> 8;
    writeChar((unsigned char) t);
}

void WriteStream::writeInt(int i)
{
    int t = i;
    for (int j = 0; j < 4; ++j) {
        t = i >> (8 * j);
        writeChar((unsigned char) t);
    }
}

void WriteStream::writeLong(long l)
{
    long t = l;
    for (int i = 0; i < 8; ++i) {
        t = l >> (8 * i);
        writeChar((unsigned char) t);
    }
}

void WriteStream::writeFloat(float f)
{
    int i = floatBitsToInt(f);
    writeInt(i);
}

void WriteStream::writeDouble(double d)
{
    long l = doubleBitsToLong(d);
    writeLong(l);
}

void WriteStream::writeFloatD(double d)
{
    uint64_t tmp;
    if (sizeof(d) != sizeof(tmp)) {
        qDebug() << "Size mismatch for uint64_t and double.";
        // TODO: throw error
        return;
    }
    if (d == 0.0) {
        writeChar(-128);
        writeInt(0);
    } else {
        memcpy(&tmp, &d, sizeof(double));
        int e = ((tmp >> 52) & 0x7FF) - 1023; // Exp
        int m = (tmp >> 21) & 0x7FFFFFFF; // Mantisa
        if (d < 0)
            m |= 0x80000000;
        writeChar((char)e);
        writeInt(m);
    }
}

void WriteStream::writeCoord(const QPoint &p)
{
    writeInt(p.x());
    writeInt(p.y());
}

void WriteStream::writeCoord16(const QPoint &p)
{
    writeShort(p.x());
    writeShort(p.y());
}

void WriteStream::writeBytes(const QByteArray &ba)
{
    if (!ba.size())
        return;
    m_array->append(ba);
}

void WriteStream::writeString(const QString &s)
{
    if (!s.isEmpty())
        writeBytes(s.toLocal8Bit());
    writeChar(0);
}

void WriteStream::writeImage(const QPixmap &i)
{
    if (i.isNull())
        return;

    QBuffer buffer(m_array);
    if (buffer.open(QIODevice::WriteOnly | QIODevice::Append)) {
        i.save(&buffer, "PNG");
        buffer.close();
    }
}

void WriteStream::writeColor(const QColor &c)
{
    writeChar((unsigned char)c.red());
    writeChar((unsigned char)c.green());
    writeChar((unsigned char)c.blue());
    writeChar((unsigned char)c.alpha());
}

void WriteStream::writeList(const VariantList &l)
{
    for (int i = 0; i < l.length(); ++i) {
        char t = l.type(i);
        writeChar((unsigned char) t);
        switch (t) {
        case TYPE_END: return;
        case TYPE_INT: writeInt(l.value(i).toInt(0)); break;
        case TYPE_STR: writeString(l.value(i).toString()); break;
        case TYPE_COORD: writeCoord(l.value(i).toPoint()); break;
        case TYPE_UINT8: writeChar((unsigned char)l.value(i).toInt(0)); break;
        case TYPE_UINT16: writeShort((unsigned short)l.value(i).toInt(0)); break;
        case TYPE_INT8: writeChar((char)l.value(i).toInt(0)); break;
        case TYPE_INT16: writeShort((short)l.value(i).toInt(0)); break;
        case TYPE_COLOR: writeColor(qvariant_cast<QColor>(l.value(i))); break;
        case TYPE_NIL: break;
        case TYPE_BYTES: {
            QByteArray b = l.value(i).toByteArray();
            writeChar((unsigned char) b.length());
            if (b.length() & 128)
                writeInt(b.length());
            writeBytes(b);
            break;
        }
        case TYPE_FLOAT32: writeFloat(l.value(i).toFloat(0)); break;
        case TYPE_FLOAT64: writeDouble(l.value(i).toDouble(0)); break;
            //TODO: write list list list list
            //case TYPE_TTOL:
        }
    }
    writeChar(0); //list end, even if empty
}

void WriteStream::writeNulls(int n)
{
    if (n < 1)
        return;

    for (int i = 0; i < n; ++i)
        writeChar(0);
}

int WriteStream::floatBitsToInt(const float f)
{
    union {
        int i;
        float x;
    } u;

    u.x = f;
    return u.i;
}

long WriteStream::doubleBitsToLong(const double d)
{
    union {
        long l;
        double x;
    } u;

    u.x = d;
    return u.l;
}

