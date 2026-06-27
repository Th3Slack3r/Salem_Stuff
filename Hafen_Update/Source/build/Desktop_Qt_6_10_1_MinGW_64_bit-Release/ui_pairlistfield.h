/********************************************************************************
** Form generated from reading UI file 'pairlistfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_PAIRLISTFIELD_H
#define UI_PAIRLISTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QTreeWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_PairlistField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QTreeWidget *treeWidget;

    void setupUi(QWidget *PairlistField)
    {
        if (PairlistField->objectName().isEmpty())
            PairlistField->setObjectName("PairlistField");
        PairlistField->resize(291, 300);
        verticalLayout = new QVBoxLayout(PairlistField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(PairlistField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        treeWidget = new QTreeWidget(PairlistField);
        treeWidget->setObjectName("treeWidget");
        treeWidget->setRootIsDecorated(false);

        verticalLayout->addWidget(treeWidget);


        retranslateUi(PairlistField);

        QMetaObject::connectSlotsByName(PairlistField);
    } // setupUi

    void retranslateUi(QWidget *PairlistField)
    {
        PairlistField->setWindowTitle(QCoreApplication::translate("PairlistField", "Form", nullptr));
        label->setText(QCoreApplication::translate("PairlistField", "TextLabel", nullptr));
        QTreeWidgetItem *___qtreewidgetitem = treeWidget->headerItem();
        ___qtreewidgetitem->setText(1, QCoreApplication::translate("PairlistField", "Second", nullptr));
        ___qtreewidgetitem->setText(0, QCoreApplication::translate("PairlistField", "First", nullptr));
    } // retranslateUi

};

namespace Ui {
    class PairlistField: public Ui_PairlistField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_PAIRLISTFIELD_H
