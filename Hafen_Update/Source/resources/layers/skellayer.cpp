#include "skellayer.h"

SkelLayer::SkelLayer(const QByteArray &d, Resource *r)
    : Layer(d, r)
{

}

const QString SkelLayer::type() const
{
    return "skel";
}

const QByteArray SkelLayer::toByteArray()
{
    QByteArray ret;
    WriteStream ws(&ret);
    for (const auto& pair : qAsConst(m_bones)) {
        ws.writeString(pair.first);
        const Bone& b = pair.second;
        // Write pos
        ws.writeFloatD(b.pos.x());
        ws.writeFloatD(b.pos.y());
        ws.writeFloatD(b.pos.z());
        // Write rax
        ws.writeFloatD(b.rax.x());
        ws.writeFloatD(b.rax.y());
        ws.writeFloatD(b.rax.z());
        // Write rang
        ws.writeFloatD(b.rang);
        // Write parent name
        ws.writeString(b.bp);
    }
    return ret;
}

bool SkelLayer::parse()
{
    if (m_rawData.isEmpty()) {
        m_error = "Resource data is empty";
        return false;
    }

    ReadStream s(&m_rawData);
    try {
        while (!s.atEnd()) {
            QString bnm = s.getString();

            // pos
            float x, y, z;
            x = s.getFloatD();
            y = s.getFloatD();
            z = s.getFloatD();
            Point3F pos(x, y, z);

            // rax
            x = s.getFloatD();
            y = s.getFloatD();
            z = s.getFloatD();
            Point3F rax(x, y, z);

            float rang = s.getFloatD();
            QString bp = s.getString(); // bone parent

            if (hasBone(bnm)) {
                m_error = QString("Duplicate bone name: %1").arg(bnm);
                return false;
            }

            // Put in map
            Bone b;
            b.bp = bp;
            b.pos = pos;
            b.rax = rax;
            b.rang = rang;
            m_bones << qMakePair(bnm, b);
        }
    } catch (const runtime_error &e) {
        m_error = QString(e.what());
        return false;
    }

    return true;
}

bool SkelLayer::hasBone(const QString& parent) const
{
    auto it = std::find_if(m_bones.begin(), m_bones.end(), [&parent](const QPair<QString, Bone>& p) -> bool
    {
        return  p.first == parent;
    });
    return it != m_bones.end();
}
