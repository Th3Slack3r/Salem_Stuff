/********************************************************************************
** Form generated from reading UI file 'hafenrestool.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_HAFENRESTOOL_H
#define UI_HAFENRESTOOL_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QMainWindow>
#include <QtWidgets/QMenu>
#include <QtWidgets/QMenuBar>
#include <QtWidgets/QSpacerItem>
#include <QtWidgets/QToolBar>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>
#include "widgets/layerwidget.h"

QT_BEGIN_NAMESPACE

class Ui_HafenResTool
{
public:
    QAction *actionLocalResource;
    QAction *actionRemoteResource;
    QAction *actionQuit;
    QAction *actionSaveResource;
    QAction *actionRemove_layer;
    QAction *actionRestore_layer;
    QAction *actionExport_layer_to_raw_format;
    QAction *actionReplace_layer_from_raw_format;
    QAction *actionAdd_layer_from_raw_format;
    QAction *actionExport_Obj;
    QAction *actionExport_Obj_With_Texture;
    QAction *actionView;
    QAction *actionKritterGetter;
    QWidget *centralWidget;
    QHBoxLayout *horizontalLayout;
    LayerWidget *layersListWidget;
    QVBoxLayout *verticalLayout;
    QLabel *labelResourceName;
    QLabel *labelResourceVersion;
    QHBoxLayout *editorLayoutHolder;
    QSpacerItem *horizontalSpacer;
    QSpacerItem *verticalSpacer;
    QMenuBar *menuBar;
    QMenu *menuFile;
    QMenu *menuLayers;
    QMenu *menuExport_Obj;
    QToolBar *mainToolBar;

    void setupUi(QMainWindow *HafenResTool)
    {
        if (HafenResTool->objectName().isEmpty())
            HafenResTool->setObjectName("HafenResTool");
        HafenResTool->resize(624, 491);
        actionLocalResource = new QAction(HafenResTool);
        actionLocalResource->setObjectName("actionLocalResource");
        QIcon icon;
        icon.addFile(QString::fromUtf8(":/root/icons/local.png"), QSize(), QIcon::Mode::Normal, QIcon::State::Off);
        actionLocalResource->setIcon(icon);
        actionRemoteResource = new QAction(HafenResTool);
        actionRemoteResource->setObjectName("actionRemoteResource");
        QIcon icon1;
        icon1.addFile(QString::fromUtf8(":/root/icons/remote.png"), QSize(), QIcon::Mode::Normal, QIcon::State::Off);
        actionRemoteResource->setIcon(icon1);
        actionQuit = new QAction(HafenResTool);
        actionQuit->setObjectName("actionQuit");
        QIcon icon2;
        icon2.addFile(QString::fromUtf8(":/root/icons/exit.png"), QSize(), QIcon::Mode::Normal, QIcon::State::Off);
        actionQuit->setIcon(icon2);
        actionSaveResource = new QAction(HafenResTool);
        actionSaveResource->setObjectName("actionSaveResource");
        actionSaveResource->setEnabled(false);
        actionRemove_layer = new QAction(HafenResTool);
        actionRemove_layer->setObjectName("actionRemove_layer");
        actionRemove_layer->setEnabled(false);
        actionRemove_layer->setMenuRole(QAction::MenuRole::TextHeuristicRole);
        actionRemove_layer->setIconVisibleInMenu(false);
        actionRestore_layer = new QAction(HafenResTool);
        actionRestore_layer->setObjectName("actionRestore_layer");
        actionRestore_layer->setEnabled(false);
        actionRestore_layer->setIconVisibleInMenu(false);
        actionExport_layer_to_raw_format = new QAction(HafenResTool);
        actionExport_layer_to_raw_format->setObjectName("actionExport_layer_to_raw_format");
        actionExport_layer_to_raw_format->setEnabled(false);
        actionReplace_layer_from_raw_format = new QAction(HafenResTool);
        actionReplace_layer_from_raw_format->setObjectName("actionReplace_layer_from_raw_format");
        actionReplace_layer_from_raw_format->setEnabled(false);
        actionAdd_layer_from_raw_format = new QAction(HafenResTool);
        actionAdd_layer_from_raw_format->setObjectName("actionAdd_layer_from_raw_format");
        actionExport_Obj = new QAction(HafenResTool);
        actionExport_Obj->setObjectName("actionExport_Obj");
        actionExport_Obj_With_Texture = new QAction(HafenResTool);
        actionExport_Obj_With_Texture->setObjectName("actionExport_Obj_With_Texture");
        actionView = new QAction(HafenResTool);
        actionView->setObjectName("actionView");
        actionKritterGetter = new QAction(HafenResTool);
        actionKritterGetter->setObjectName("actionKritterGetter");
        centralWidget = new QWidget(HafenResTool);
        centralWidget->setObjectName("centralWidget");
        horizontalLayout = new QHBoxLayout(centralWidget);
        horizontalLayout->setSpacing(6);
        horizontalLayout->setContentsMargins(11, 11, 11, 11);
        horizontalLayout->setObjectName("horizontalLayout");
        layersListWidget = new LayerWidget(centralWidget);
        layersListWidget->setObjectName("layersListWidget");
        layersListWidget->setMinimumSize(QSize(200, 0));
        layersListWidget->setMaximumSize(QSize(200, 16777215));

        horizontalLayout->addWidget(layersListWidget);

        verticalLayout = new QVBoxLayout();
        verticalLayout->setSpacing(6);
        verticalLayout->setObjectName("verticalLayout");
        labelResourceName = new QLabel(centralWidget);
        labelResourceName->setObjectName("labelResourceName");
        QFont font;
        font.setPointSize(12);
        font.setBold(true);
        labelResourceName->setFont(font);

        verticalLayout->addWidget(labelResourceName);

        labelResourceVersion = new QLabel(centralWidget);
        labelResourceVersion->setObjectName("labelResourceVersion");
        labelResourceVersion->setFont(font);

        verticalLayout->addWidget(labelResourceVersion);

        editorLayoutHolder = new QHBoxLayout();
        editorLayoutHolder->setSpacing(6);
        editorLayoutHolder->setObjectName("editorLayoutHolder");
        horizontalSpacer = new QSpacerItem(40, 20, QSizePolicy::Policy::Expanding, QSizePolicy::Policy::Minimum);

        editorLayoutHolder->addItem(horizontalSpacer);


        verticalLayout->addLayout(editorLayoutHolder);

        verticalSpacer = new QSpacerItem(20, 40, QSizePolicy::Policy::Minimum, QSizePolicy::Policy::Expanding);

        verticalLayout->addItem(verticalSpacer);


        horizontalLayout->addLayout(verticalLayout);

        HafenResTool->setCentralWidget(centralWidget);
        menuBar = new QMenuBar(HafenResTool);
        menuBar->setObjectName("menuBar");
        menuBar->setGeometry(QRect(0, 0, 624, 21));
        menuFile = new QMenu(menuBar);
        menuFile->setObjectName("menuFile");
        menuLayers = new QMenu(menuBar);
        menuLayers->setObjectName("menuLayers");
        menuLayers->setEnabled(false);
        menuExport_Obj = new QMenu(menuBar);
        menuExport_Obj->setObjectName("menuExport_Obj");
        HafenResTool->setMenuBar(menuBar);
        mainToolBar = new QToolBar(HafenResTool);
        mainToolBar->setObjectName("mainToolBar");
        HafenResTool->addToolBar(mainToolBar);

        menuBar->addAction(menuFile->menuAction());
        menuBar->addAction(menuLayers->menuAction());
        menuBar->addAction(menuExport_Obj->menuAction());
        menuFile->addAction(actionLocalResource);
        menuFile->addAction(actionRemoteResource);
        menuFile->addSeparator();
        menuFile->addAction(actionSaveResource);
        menuFile->addSeparator();
        menuFile->addAction(actionQuit);
        menuLayers->addAction(actionRemove_layer);
        menuLayers->addAction(actionRestore_layer);
        menuLayers->addSeparator();
        menuLayers->addAction(actionExport_layer_to_raw_format);
        menuLayers->addAction(actionReplace_layer_from_raw_format);
        menuLayers->addAction(actionAdd_layer_from_raw_format);
        menuExport_Obj->addAction(actionExport_Obj);
        menuExport_Obj->addAction(actionExport_Obj_With_Texture);
        menuExport_Obj->addAction(actionView);
        mainToolBar->addAction(actionLocalResource);
        mainToolBar->addAction(actionRemoteResource);
        mainToolBar->addAction(actionSaveResource);
        mainToolBar->addSeparator();
        mainToolBar->addAction(actionKritterGetter);

        retranslateUi(HafenResTool);

        QMetaObject::connectSlotsByName(HafenResTool);
    } // setupUi

    void retranslateUi(QMainWindow *HafenResTool)
    {
        HafenResTool->setWindowTitle(QCoreApplication::translate("HafenResTool", "HafenResTool", nullptr));
        actionLocalResource->setText(QCoreApplication::translate("HafenResTool", "Open local resource", nullptr));
#if QT_CONFIG(shortcut)
        actionLocalResource->setShortcut(QCoreApplication::translate("HafenResTool", "Ctrl+L", nullptr));
#endif // QT_CONFIG(shortcut)
        actionRemoteResource->setText(QCoreApplication::translate("HafenResTool", "Open remote resource", nullptr));
#if QT_CONFIG(shortcut)
        actionRemoteResource->setShortcut(QCoreApplication::translate("HafenResTool", "Ctrl+R", nullptr));
#endif // QT_CONFIG(shortcut)
        actionQuit->setText(QCoreApplication::translate("HafenResTool", "Quit", nullptr));
        actionSaveResource->setText(QCoreApplication::translate("HafenResTool", "Save resource", nullptr));
#if QT_CONFIG(shortcut)
        actionSaveResource->setShortcut(QCoreApplication::translate("HafenResTool", "Ctrl+S", nullptr));
#endif // QT_CONFIG(shortcut)
        actionRemove_layer->setText(QCoreApplication::translate("HafenResTool", "Remove layer", nullptr));
        actionRestore_layer->setText(QCoreApplication::translate("HafenResTool", "Restore layer", nullptr));
        actionExport_layer_to_raw_format->setText(QCoreApplication::translate("HafenResTool", "Export layer to raw format", nullptr));
        actionReplace_layer_from_raw_format->setText(QCoreApplication::translate("HafenResTool", "Replace layer from raw format", nullptr));
        actionAdd_layer_from_raw_format->setText(QCoreApplication::translate("HafenResTool", "Add layer from raw format", nullptr));
        actionExport_Obj->setText(QCoreApplication::translate("HafenResTool", "Export Obj", nullptr));
        actionExport_Obj_With_Texture->setText(QCoreApplication::translate("HafenResTool", "Export Obj + Texture", nullptr));
        actionView->setText(QCoreApplication::translate("HafenResTool", "View", nullptr));
        actionKritterGetter->setText(QCoreApplication::translate("HafenResTool", "Kritter Getter", nullptr));
        labelResourceName->setText(QString());
        labelResourceVersion->setText(QString());
        menuFile->setTitle(QCoreApplication::translate("HafenResTool", "File", nullptr));
        menuLayers->setTitle(QCoreApplication::translate("HafenResTool", "Layers", nullptr));
        menuExport_Obj->setTitle(QCoreApplication::translate("HafenResTool", "Obj", nullptr));
        mainToolBar->setWindowTitle(QCoreApplication::translate("HafenResTool", "mainToolBar", nullptr));
    } // retranslateUi

};

namespace Ui {
    class HafenResTool: public Ui_HafenResTool {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_HAFENRESTOOL_H
