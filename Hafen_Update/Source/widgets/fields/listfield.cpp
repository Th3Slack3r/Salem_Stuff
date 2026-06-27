#include "listfield.h"
#include <QInputDialog>

ListField::ListField(const QString &title, const QList<QString> &value, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    m_value = value;
    redrawList();
}

const QList<QString> ListField::getValue() const
{
    return m_value;
}

void ListField::setValue(const QList<QString> &v)
{
    m_value = v;
    redrawList();
    onFieldChanged();
}

void ListField::setReadOnly(bool ro)
{
    Field::setReadOnly(ro);
    if (ro) {
        pushAdd->setEnabled(false);
    }
}

void ListField::redrawList()
{
    listWidget->clear();

    pushUp->setEnabled(false);
    pushDown->setEnabled(false);
    pushRemove->setEnabled(false);

    foreach (const QString &i, m_value)
        new QListWidgetItem(i, listWidget);
}

void ListField::moveItem(int row, int direction)
{
    if (!direction)
        return;

    bool change = false;
    if (direction < 0 && row > 0) {
        m_value.move(row, row - 1);
        change = true;
    } else if (direction > 0 && row < m_value.length() - 1) {
        m_value.move(row, row + 1);
        change = true;
    }

    if (change) {
        redrawList();
        onFieldChanged();
    }
}

void ListField::on_listWidget_clicked(const QModelIndex &index)
{
    Q_UNUSED(index)

    if (!m_readOnly) {
        pushUp->setEnabled(true);
        pushDown->setEnabled(true);
        pushRemove->setEnabled(true);
    }
}

void ListField::on_pushRemove_clicked()
{
    int r = listWidget->currentRow();
    if (r < 0 || r > m_value.length() - 1)
        return;

    m_value.removeAt(r);
    redrawList();
    onFieldChanged();
}

void ListField::on_pushAdd_clicked()
{
    bool ok = false;
    QString name = QInputDialog::getText(this, "New item",
                                         "Item name:", QLineEdit::Normal, QString(), &ok);
    if (ok && !name.isEmpty()) {
        m_value << name;
        redrawList();
        onFieldChanged();
    }
}

void ListField::on_pushUp_clicked()
{
    moveItem(listWidget->currentRow(), -1);
}

void ListField::on_pushDown_clicked()
{
    moveItem(listWidget->currentRow(), 1);
}
