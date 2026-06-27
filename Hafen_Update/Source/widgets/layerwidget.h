#ifndef LAYERWIDGET_H
#define LAYERWIDGET_H

#include "resources/resource.h"

#include "ui_layerwidget.h"

class LayerWidget : public QWidget, private Ui::LayerWidget
{
    Q_OBJECT

public:
    explicit LayerWidget(QWidget *parent = nullptr);
    void setResource(Resource *res);
    void redrawList();
    Resource *m_resource;
    int currentRow() const;

signals:
    void itemActivated(int index);
    void resourceChanged();

    void onCurrentRowChanged(int row);

private slots:
    void on_layersList_activated(const QModelIndex &index);
    void on_layersList_clicked(const QModelIndex &index);

private:
    //Resource *m_resource;

    int mCurrentRow = -1;

};

#endif // LAYERWIDGET_H
