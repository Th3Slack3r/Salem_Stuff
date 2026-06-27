#ifndef HAFENRESTOOL_H
#define HAFENRESTOOL_H

#include "ui_hafenrestool.h"
#include "resources/resource.h"
#include "tools/resourcedictionary.h"
#include "widgets/editor.h"
#include "widgets/layerwidget.h"
#include <QCloseEvent>
#include <QProgressDialog>
#include <QScopedPointer>
#include <QTemporaryDir>
#include <QProcess>
#include <QTcpServer>

class HafenResTool : public QMainWindow, private Ui::HafenResTool
{
    Q_OBJECT

public:
    explicit HafenResTool(QWidget *parent = 0);

protected:
    void closeEvent(QCloseEvent *event);

private:
    Resource *m_currentResource;
    ResourceDictionary *m_dictionary;
    Editor *m_currentEditor;
    QProgressDialog *m_progress;
    LayerWidget* m_layerWidget;
    QScopedPointer<QTemporaryDir> m_previewTemp;
    QScopedPointer<QProcess> m_previewServer;
    int m_previewPort = -1;

    void destroyCurrentEditor();
    void setNewResource(const QString &n);
    void saveResource();

private slots:
    void onResourceChanged();
    void onResourceError(const QString &e);
    void onResourceInited();
    void onResourceLayerSelected(int index);
    void onResourceStartDownload();
    void onResourceEndDownload();
    void on_actionRemoteResource_triggered();
    void on_actionLocalResource_triggered();
    void on_actionQuit_triggered();
    void on_actionSaveResource_triggered();
    void onExportObjClicked();
    void onExportObjWithTextureClicked();
    void onViewObjClicked();
    void on_actionKritterGetter_triggered();

    //
    void onLayerWidgetCurrentRowChanged(int index);
    void on_actionRemove_layer_triggered();
    void on_actionRestore_layer_triggered();
    void on_actionExport_layer_to_raw_format_triggered();
    void on_actionReplace_layer_from_raw_format_triggered();
    void on_actionAdd_layer_from_raw_format_triggered();
};

#endif // HAFENRESTOOL_H
