#include "meshchecklistfield.h"

MeshChecklistField::MeshChecklistField(const QString& title, const QList<MeshLayer*>& meshes, QWidget* parent)
    : Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    m_mesehs = meshes;
    redrawList();
}

const QList<MeshLayer*> MeshChecklistField::checkedMeshes() const
{
    QList<MeshLayer*> ret;
    for (int i = 0; i < treeWidget->topLevelItemCount(); ++i)
    {
        QTreeWidgetItem* item = treeWidget->topLevelItem(i);
        if (item->checkState(0) == Qt::Checked)
            ret << m_mesehs.at(i);
    }
    return ret;
}

const QList<MeshLayer*> MeshChecklistField::meshes() const
{
    return m_mesehs;
}

void MeshChecklistField::redrawList()
{
    treeWidget->clear();

    for (int i = 0; i < m_mesehs.length(); ++i)
    {
        QTreeWidgetItem* item = new QTreeWidgetItem(
            QStringList()
            << QString("%1").arg(m_mesehs[i]->id())
            << QString("dots: %1; faces: %2").arg(m_mesehs[i]->ind().length()).arg(m_mesehs[i]->ind().length() / 3));
        item->setFlags(Qt::ItemIsUserCheckable | item->flags());
        item->setCheckState(0, Qt::Checked);
        treeWidget->addTopLevelItem(item);
    }
}

void MeshChecklistField::on_treeWidget_itemChanged(QTreeWidgetItem* item, int column)
{
    Q_UNUSED(column)
    Q_UNUSED(item)
    emit checkChanged();
}
