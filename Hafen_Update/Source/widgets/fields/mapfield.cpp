#include "mapfield.h"
#include <QInputDialog>

MapField::MapField(const QString &title, const QMap<QString, QString> &value, MapType mt, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    m_value = value;
    m_mt = mt;
    redrawList();
}

void MapField::setValue(const QMap<QString, QString> &v)
{
    m_value = v;
    redrawList();
}

void MapField::redrawList()
{
    treeWidget->clear();

    QMap<QString, QString>::iterator it;
    for (it = m_value.begin(); it != m_value.end(); ++it)
        treeWidget->addTopLevelItem(new QTreeWidgetItem(QStringList() << it.key() << it.value()));
}

void MapField::on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column)
{
    Q_UNUSED(column)
    if (getReadOnly())
        return;

    bool ok = false;
    if (m_mt == MapType::Int) {
        int v = QInputDialog::getInt(this, "New value", "Value:", item->text(1).toInt(), -9999, 9999, 1, &ok);
        if (ok)
            m_value[item->text(0)] = QString("%1").arg(v);
    } else if (m_mt == MapType::Float) {
        double v = QInputDialog::getDouble(this, "New value", "Value:", item->text(1).toDouble(), -9999, 9999, 3, &ok);
        if (ok)
            m_value[item->text(0)] = QString("%1").arg(v);
    } else {
        QString v = QInputDialog::getText(this, "New value", "Value:", QLineEdit::Normal, item->text(1), &ok);
        if (ok)
            m_value[item->text(0)] = v;
    }

    if (ok) {
        onFieldChanged();
        redrawList();
    }
}
