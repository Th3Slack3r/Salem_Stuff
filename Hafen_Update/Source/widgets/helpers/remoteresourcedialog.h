#ifndef REMOTERESOURCEDIALOG_H
#define REMOTERESOURCEDIALOG_H

#include "ui_remoteresourcedialog.h"

class RemoteResourceDialog : public QDialog, private Ui::RemoteResourceDialog
{
    Q_OBJECT

public:
    explicit RemoteResourceDialog(const QStringList &dict = QStringList(), QWidget *parent = 0);

    const QString getPath() const;
};

#endif // REMOTERESOURCEDIALOG_H
