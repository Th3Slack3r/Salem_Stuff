#include "decompiler.h"
#include <QFile>
#include <QDir>
#include <QDebug>

Decompiler::Decompiler(const QString &name, const QByteArray &data, QObject *parent) : QObject(parent)
{
    m_name = name;
    m_data = data;
}

void Decompiler::decompile()
{
    if (checkDecompiler() && createFile(m_name, m_data)) {
        // Create new process
        QProcess *p = new QProcess(this);
        connect(p, SIGNAL(finished(int, QProcess::ExitStatus)),
                SLOT(onDecompilerFinished(int, QProcess::ExitStatus)));
        // Start process
        p->start("java", QStringList() << "-jar" << "./tools/decompiler.jar"
                 << QString("./tmp/%1.class").arg(m_name));
    }
}

bool Decompiler::checkDecompiler()
{
    QString path = "./tools/decompiler.jar";
    if (!QFileInfo::exists(path)) {
        emit decompileError("Cannot find decompiler: missing './tools/decompile.jar'.");
        return false;
    }

    return true;
}

bool Decompiler::createFile(const QString &name, const QByteArray &data)
{
    if (name.isEmpty() || data.isEmpty()) {
        emit decompileError("File name or bytecode is invalid.");
        return false;
    }

    QDir d;
    if (!d.exists("./tmp")) {
        if (!d.mkdir("tmp")) {
            emit decompileError("Failed to create temporary folder.");
            return false;
        }
    }

    QFile _f(QString("./tmp/%1.class").arg(name));
    if (!_f.open(QIODevice::WriteOnly)) {
        emit decompileError("Failed to create temporary file.");
        return false;
    }
    _f.write(data);
    _f.close();

    return true;
}

void Decompiler::onDecompilerFinished(int c, QProcess::ExitStatus s)
{
    qDebug() << "Decompiler finished with code" << c << "and status" << s;

    QProcess *p = qobject_cast<QProcess *>(sender());
    if (!p) return;

    emit codeDecompiled(QString::fromUtf8(p->readAllStandardOutput()));
    p->deleteLater();
}
