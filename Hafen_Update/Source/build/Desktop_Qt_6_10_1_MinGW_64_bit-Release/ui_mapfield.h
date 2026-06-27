/********************************************************************************
** Form generated from reading UI file 'mapfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MAPFIELD_H
#define UI_MAPFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QTreeWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MapField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QTreeWidget *treeWidget;

    void setupUi(QWidget *MapField)
    {
        if (MapField->objectName().isEmpty())
            MapField->setObjectName("MapField");
        MapField->resize(334, 271);
        verticalLayout = new QVBoxLayout(MapField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(MapField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        treeWidget = new QTreeWidget(MapField);
        QTreeWidgetItem *__qtreewidgetitem = new QTreeWidgetItem();
        __qtreewidgetitem->setText(1, QString::fromUtf8("Values"));
        __qtreewidgetitem->setText(0, QString::fromUtf8("Keys"));
        treeWidget->setHeaderItem(__qtreewidgetitem);
        treeWidget->setObjectName("treeWidget");
        treeWidget->setRootIsDecorated(false);
        treeWidget->setColumnCount(2);
        treeWidget->header()->setVisible(false);

        verticalLayout->addWidget(treeWidget);


        retranslateUi(MapField);

        QMetaObject::connectSlotsByName(MapField);
    } // setupUi

    void retranslateUi(QWidget *MapField)
    {
        MapField->setWindowTitle(QCoreApplication::translate("MapField", "Form", nullptr));
        label->setText(QCoreApplication::translate("MapField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class MapField: public Ui_MapField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MAPFIELD_H
