/********************************************************************************
** Form generated from reading UI file 'meshchecklistfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MESHCHECKLISTFIELD_H
#define UI_MESHCHECKLISTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QTreeWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MeshChecklistField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QTreeWidget *treeWidget;

    void setupUi(QWidget *MeshChecklistField)
    {
        if (MeshChecklistField->objectName().isEmpty())
            MeshChecklistField->setObjectName("MeshChecklistField");
        MeshChecklistField->resize(311, 209);
        verticalLayout = new QVBoxLayout(MeshChecklistField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(MeshChecklistField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        treeWidget = new QTreeWidget(MeshChecklistField);
        treeWidget->setObjectName("treeWidget");
        treeWidget->setRootIsDecorated(false);

        verticalLayout->addWidget(treeWidget);


        retranslateUi(MeshChecklistField);

        QMetaObject::connectSlotsByName(MeshChecklistField);
    } // setupUi

    void retranslateUi(QWidget *MeshChecklistField)
    {
        MeshChecklistField->setWindowTitle(QCoreApplication::translate("MeshChecklistField", "Form", nullptr));
        label->setText(QCoreApplication::translate("MeshChecklistField", "TextLabel", nullptr));
        QTreeWidgetItem *___qtreewidgetitem = treeWidget->headerItem();
        ___qtreewidgetitem->setText(1, QCoreApplication::translate("MeshChecklistField", "Size", nullptr));
        ___qtreewidgetitem->setText(0, QCoreApplication::translate("MeshChecklistField", "Mesh id", nullptr));
    } // retranslateUi

};

namespace Ui {
    class MeshChecklistField: public Ui_MeshChecklistField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MESHCHECKLISTFIELD_H
