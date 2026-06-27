/********************************************************************************
** Form generated from reading UI file 'bonelistfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_BONELISTFIELD_H
#define UI_BONELISTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QTreeWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_BoneListField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QTreeWidget *treeWidget;

    void setupUi(QWidget *BoneListField)
    {
        if (BoneListField->objectName().isEmpty())
            BoneListField->setObjectName("BoneListField");
        BoneListField->resize(400, 300);
        verticalLayout = new QVBoxLayout(BoneListField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(BoneListField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        treeWidget = new QTreeWidget(BoneListField);
        treeWidget->setObjectName("treeWidget");
        treeWidget->setRootIsDecorated(false);

        verticalLayout->addWidget(treeWidget);


        retranslateUi(BoneListField);

        QMetaObject::connectSlotsByName(BoneListField);
    } // setupUi

    void retranslateUi(QWidget *BoneListField)
    {
        BoneListField->setWindowTitle(QCoreApplication::translate("BoneListField", "Form", nullptr));
        label->setText(QCoreApplication::translate("BoneListField", "TextLabel", nullptr));
        QTreeWidgetItem *___qtreewidgetitem = treeWidget->headerItem();
        ___qtreewidgetitem->setText(1, QCoreApplication::translate("BoneListField", "Values", nullptr));
        ___qtreewidgetitem->setText(0, QCoreApplication::translate("BoneListField", "Bones", nullptr));
    } // retranslateUi

};

namespace Ui {
    class BoneListField: public Ui_BoneListField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_BONELISTFIELD_H
