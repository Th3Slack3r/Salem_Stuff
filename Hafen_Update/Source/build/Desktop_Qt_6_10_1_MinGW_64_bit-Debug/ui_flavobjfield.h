/********************************************************************************
** Form generated from reading UI file 'flavobjfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_FLAVOBJFIELD_H
#define UI_FLAVOBJFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QTreeWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_FlavobjField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QTreeWidget *treeWidget;

    void setupUi(QWidget *FlavobjField)
    {
        if (FlavobjField->objectName().isEmpty())
            FlavobjField->setObjectName("FlavobjField");
        FlavobjField->resize(251, 212);
        verticalLayout = new QVBoxLayout(FlavobjField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(FlavobjField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        treeWidget = new QTreeWidget(FlavobjField);
        treeWidget->setObjectName("treeWidget");
        treeWidget->setRootIsDecorated(false);

        verticalLayout->addWidget(treeWidget);


        retranslateUi(FlavobjField);

        QMetaObject::connectSlotsByName(FlavobjField);
    } // setupUi

    void retranslateUi(QWidget *FlavobjField)
    {
        FlavobjField->setWindowTitle(QCoreApplication::translate("FlavobjField", "Form", nullptr));
        label->setText(QCoreApplication::translate("FlavobjField", "TextLabel", nullptr));
        QTreeWidgetItem *___qtreewidgetitem = treeWidget->headerItem();
        ___qtreewidgetitem->setText(1, QCoreApplication::translate("FlavobjField", "Values", nullptr));
        ___qtreewidgetitem->setText(0, QCoreApplication::translate("FlavobjField", "Keys", nullptr));
    } // retranslateUi

};

namespace Ui {
    class FlavobjField: public Ui_FlavobjField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_FLAVOBJFIELD_H
