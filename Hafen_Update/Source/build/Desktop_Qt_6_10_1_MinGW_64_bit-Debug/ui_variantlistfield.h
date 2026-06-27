/********************************************************************************
** Form generated from reading UI file 'variantlistfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_VARIANTLISTFIELD_H
#define UI_VARIANTLISTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QSpacerItem>
#include <QtWidgets/QTreeWidget>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_VariantListField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QTreeWidget *treeWidget;
    QHBoxLayout *horizontalLayout;
    QPushButton *pushRemoveNode;
    QSpacerItem *horizontalSpacer;

    void setupUi(QWidget *VariantListField)
    {
        if (VariantListField->objectName().isEmpty())
            VariantListField->setObjectName("VariantListField");
        VariantListField->resize(341, 282);
        verticalLayout = new QVBoxLayout(VariantListField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(VariantListField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        treeWidget = new QTreeWidget(VariantListField);
        treeWidget->setObjectName("treeWidget");
        treeWidget->setRootIsDecorated(false);

        verticalLayout->addWidget(treeWidget);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        pushRemoveNode = new QPushButton(VariantListField);
        pushRemoveNode->setObjectName("pushRemoveNode");
        pushRemoveNode->setEnabled(false);

        horizontalLayout->addWidget(pushRemoveNode);

        horizontalSpacer = new QSpacerItem(40, 20, QSizePolicy::Policy::Expanding, QSizePolicy::Policy::Minimum);

        horizontalLayout->addItem(horizontalSpacer);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(VariantListField);

        QMetaObject::connectSlotsByName(VariantListField);
    } // setupUi

    void retranslateUi(QWidget *VariantListField)
    {
        VariantListField->setWindowTitle(QCoreApplication::translate("VariantListField", "Form", nullptr));
        label->setText(QCoreApplication::translate("VariantListField", "TextLabel", nullptr));
        QTreeWidgetItem *___qtreewidgetitem = treeWidget->headerItem();
        ___qtreewidgetitem->setText(1, QCoreApplication::translate("VariantListField", "Values", nullptr));
        ___qtreewidgetitem->setText(0, QCoreApplication::translate("VariantListField", "Keys", nullptr));
#if QT_CONFIG(tooltip)
        pushRemoveNode->setToolTip(QCoreApplication::translate("VariantListField", "Remove selected node", nullptr));
#endif // QT_CONFIG(tooltip)
        pushRemoveNode->setText(QCoreApplication::translate("VariantListField", "Remove node", nullptr));
    } // retranslateUi

};

namespace Ui {
    class VariantListField: public Ui_VariantListField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_VARIANTLISTFIELD_H
