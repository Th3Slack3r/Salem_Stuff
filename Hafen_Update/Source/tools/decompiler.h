#ifndef DECOMPILER_H
#define DECOMPILER_H

#include <QObject>
#include <QProcess>

class Decompiler : public QObject
{
    Q_OBJECT
public:
    explicit Decompiler(const QString &name, const QByteArray &data, QObject *parent = 0);
    void decompile();

private:
    QString m_name;
    QByteArray m_data;

    bool checkDecompiler();
    bool createFile(const QString &name, const QByteArray &data);

signals:
    void decompileError(const QString &e);
    void codeDecompiled(const QString &c);

public slots:

private slots:
    void onDecompilerFinished(int c, QProcess::ExitStatus s);
};

#endif // DECOMPILER_H
