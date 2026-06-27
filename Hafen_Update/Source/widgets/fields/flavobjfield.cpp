#include "flavobjfield.h"
#include <QInputDialog>

FlavobjField::FlavobjField(const QString &title, const QList<Tileset2Layer::Flavobj> &value, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    m_value = value;
    redrawList();
}

void FlavobjField::redrawList()
{
    treeWidget->clear();

    for (int  i = 0; i < m_value.length(); ++i) {
        QTreeWidgetItem *ri = new QTreeWidgetItem(QStringList() << QString("Flavobject%1").arg(i+1) << "");
        treeWidget->addTopLevelItem(ri);
        Tileset2Layer::Flavobj fo = m_value.at(i);
        ri->addChild(new QTreeWidgetItem(QStringList() << "fln" << fo.fln));
        ri->addChild(new QTreeWidgetItem(QStringList() << "flv" << QString("%1").arg(fo.flv)));
        ri->addChild(new QTreeWidgetItem(QStringList() << "flw" << QString("%1").arg(fo.flw)));
    }
}

void FlavobjField::on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column)
{
    Q_UNUSED(column)
    if (!item->parent())
        return;

    QString type = item->text(0);
    int index = -1;
    for (int i = 0; i < item->parent()->treeWidget()->topLevelItemCount(); ++i) {
        if (item->parent()->treeWidget()->topLevelItem(i) == item->parent()) {
            index = i;
            break;
        }
    }
    if (index < 0) return;
    bool ok = false;
    if (type == "fln") {
        QString v = QInputDialog::getText(this, "New value", "Value:", QLineEdit::Normal, item->text(1), &ok);
        if (ok)
            m_value[index].fln = v;
    } else if (type == "flv") {
        int v = QInputDialog::getInt(this, "New value", "Value:", item->text(1).toInt(), 0, 65535, 1, &ok);
        if (ok)
            m_value[index].flv = v;
    } else if (type == "flw") {
        int v = QInputDialog::getInt(this, "New value", "Value:", item->text(1).toInt(), 0, 255, 1, &ok);
        if (ok)
            m_value[index].flw = v;
    }

    if (ok) {
        onFieldChanged();
        redrawList();
    }
}
