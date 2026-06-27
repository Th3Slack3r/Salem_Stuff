#include "layerwidget.h"

#include "resources/layers/layer.h"

LayerWidget::LayerWidget(QWidget *parent) :
    QWidget(parent)
{
    m_resource = nullptr;
    setupUi(this);
}

void LayerWidget::setResource(Resource *res)
{
    m_resource = res;
    mCurrentRow = -1;
    redrawList();
}

void LayerWidget::redrawList()
{
    layersList->clear();

    if (m_resource) {
        QList<Layer *> layers = m_resource->layers();
        int row = 0;
        foreach (Layer *l, layers) {
            QColor color;
            QString itemText = "";
            if (l->isEdited()) {
                color = QColor(46, 125, 50);
                itemText = "*";
            } else if (l->isRemoved()) {
                color = QColor(198, 40, 40);
                itemText = "-";
            } else {
                color = Qt::black;
            }
            QListWidgetItem *item = new QListWidgetItem(itemText + l->type(), layersList);
            item->setForeground(color);
            if (mCurrentRow == row)
                item->setSelected(true);
            ++row;
        }
    }
}

int LayerWidget::currentRow() const
{
    return mCurrentRow;
}

void LayerWidget::on_layersList_activated(const QModelIndex &index)
{
    int row = index.row();
    emit itemActivated(row);
}

void LayerWidget::on_layersList_clicked(const QModelIndex &index)
{
    int row = index.row();
    if (m_resource && m_resource->layers().length() - 1 >= row) {
        if (row != mCurrentRow) {
            mCurrentRow = row;
            emit onCurrentRowChanged(row);
        }
    }
}
