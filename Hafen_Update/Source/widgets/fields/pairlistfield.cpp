#include "pairlistfield.h"
#include <QInputDialog>

PairlistField::PairlistField(const QString &title,
                             const QList<QPair<QString, QString> > &value,
                             PairTypes typeOne, PairTypes typeTwo, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    m_t1 = typeOne;
    m_t2 = typeTwo;
    m_value = value;
    label->setText(QString("%1:").arg(title));
    redrawList();
}

void PairlistField::redrawList()
{
    treeWidget->clear();
    for (int i = 0; i < m_value.length(); ++i) {
        QPair<QString, QString> p = m_value.at(i);
        new QTreeWidgetItem(treeWidget, QStringList() << p.first << p.second);
    }
}

const QString PairlistField::editValie(const QString &v, PairlistField::PairTypes t)
{
    bool ok = false;
    if (t == Int) {
        int nv = QInputDialog::getInt(this, "New value", "Value:", v.toInt(), -999999, 999999, 1, &ok);
        if (ok)
            return QString("%1").arg(nv);
    } else if (t == Float) {
        double nv = QInputDialog::getDouble(this, "New value", "Value:", v.toDouble(), -999999, 999999, 4, &ok);
        if (ok)
            return QString("%1").arg(nv);
    } else if (t == String) {
        QString nv = QInputDialog::getText(this, "New value", "Value:", QLineEdit::Normal, v, &ok);
        if (ok)
            return nv;
    }
    return v;
}

int PairlistField::itemIndex(QTreeWidgetItem *item)
{
    for (int i = 0; i < treeWidget->topLevelItemCount(); ++i)
        if (treeWidget->topLevelItem(i) == item)
            return i;
    return -1;
}

void PairlistField::on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column)
{
    QString v = item->text(column);
    QString nv = editValie(v, column ? m_t2 : m_t1);
    if (v != nv) {
        int index = itemIndex(item);
        if (index >= 0) {
            column ? m_value[index].second = nv : m_value[index].first = nv;
            redrawList();
            emit onFieldChanged();
        }
    }
}
