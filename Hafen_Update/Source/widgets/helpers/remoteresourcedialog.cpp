#include "remoteresourcedialog.h"
#include <QCompleter>

RemoteResourceDialog::RemoteResourceDialog(const QStringList &dict, QWidget *parent) :
    QDialog(parent)
{
    setupUi(this);
    setFixedHeight(90);
    setMinimumWidth(450);

    // Create autocompleter
    if (dict.length()) {
        QCompleter *completer = new QCompleter(dict, this);
        completer->setCaseSensitivity(Qt::CaseInsensitive);
        completer->setFilterMode(Qt::MatchContains);
        lineEdit->setCompleter(completer);
    }
}

const QString RemoteResourceDialog::getPath() const
{
    return lineEdit->text();
}
