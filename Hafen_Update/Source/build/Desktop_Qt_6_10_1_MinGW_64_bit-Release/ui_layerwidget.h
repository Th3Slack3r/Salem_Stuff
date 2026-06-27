/********************************************************************************
** Form generated from reading UI file 'layerwidget.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_LAYERWIDGET_H
#define UI_LAYERWIDGET_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QLabel>
#include <QtWidgets/QListWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_LayerWidget
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QListWidget *layersList;

    void setupUi(QWidget *LayerWidget)
    {
        if (LayerWidget->objectName().isEmpty())
            LayerWidget->setObjectName("LayerWidget");
        LayerWidget->resize(346, 432);
        verticalLayout = new QVBoxLayout(LayerWidget);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(LayerWidget);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        layersList = new QListWidget(LayerWidget);
        layersList->setObjectName("layersList");

        verticalLayout->addWidget(layersList);


        retranslateUi(LayerWidget);

        QMetaObject::connectSlotsByName(LayerWidget);
    } // setupUi

    void retranslateUi(QWidget *LayerWidget)
    {
        LayerWidget->setWindowTitle(QCoreApplication::translate("LayerWidget", "Form", nullptr));
        label->setText(QCoreApplication::translate("LayerWidget", "Resource layers:", nullptr));
    } // retranslateUi

};

namespace Ui {
    class LayerWidget: public Ui_LayerWidget {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_LAYERWIDGET_H
