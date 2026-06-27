/********************************************************************************
** Form generated from reading UI file 'listfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_LISTFIELD_H
#define UI_LISTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QListWidget>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QSpacerItem>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_ListField
{
public:
    QVBoxLayout *verticalLayout_2;
    QLabel *label;
    QHBoxLayout *horizontalLayout_2;
    QListWidget *listWidget;
    QVBoxLayout *verticalLayout;
    QPushButton *pushRemove;
    QPushButton *pushAdd;
    QSpacerItem *verticalSpacer;
    QHBoxLayout *horizontalLayout;
    QPushButton *pushUp;
    QPushButton *pushDown;

    void setupUi(QWidget *ListField)
    {
        if (ListField->objectName().isEmpty())
            ListField->setObjectName("ListField");
        ListField->resize(400, 240);
        verticalLayout_2 = new QVBoxLayout(ListField);
        verticalLayout_2->setObjectName("verticalLayout_2");
        label = new QLabel(ListField);
        label->setObjectName("label");

        verticalLayout_2->addWidget(label);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2->setObjectName("horizontalLayout_2");
        listWidget = new QListWidget(ListField);
        listWidget->setObjectName("listWidget");

        horizontalLayout_2->addWidget(listWidget);

        verticalLayout = new QVBoxLayout();
        verticalLayout->setObjectName("verticalLayout");
        pushRemove = new QPushButton(ListField);
        pushRemove->setObjectName("pushRemove");
        pushRemove->setEnabled(false);

        verticalLayout->addWidget(pushRemove);

        pushAdd = new QPushButton(ListField);
        pushAdd->setObjectName("pushAdd");

        verticalLayout->addWidget(pushAdd);

        verticalSpacer = new QSpacerItem(20, 40, QSizePolicy::Policy::Minimum, QSizePolicy::Policy::Expanding);

        verticalLayout->addItem(verticalSpacer);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        pushUp = new QPushButton(ListField);
        pushUp->setObjectName("pushUp");
        pushUp->setEnabled(false);
        pushUp->setMinimumSize(QSize(41, 0));
        pushUp->setMaximumSize(QSize(41, 16777215));

        horizontalLayout->addWidget(pushUp);

        pushDown = new QPushButton(ListField);
        pushDown->setObjectName("pushDown");
        pushDown->setEnabled(false);
        pushDown->setMinimumSize(QSize(41, 0));
        pushDown->setMaximumSize(QSize(41, 16777215));

        horizontalLayout->addWidget(pushDown);


        verticalLayout->addLayout(horizontalLayout);


        horizontalLayout_2->addLayout(verticalLayout);


        verticalLayout_2->addLayout(horizontalLayout_2);


        retranslateUi(ListField);

        QMetaObject::connectSlotsByName(ListField);
    } // setupUi

    void retranslateUi(QWidget *ListField)
    {
        ListField->setWindowTitle(QCoreApplication::translate("ListField", "Form", nullptr));
        label->setText(QCoreApplication::translate("ListField", "TextLabel", nullptr));
#if QT_CONFIG(tooltip)
        pushRemove->setToolTip(QCoreApplication::translate("ListField", "Remove selected item", nullptr));
#endif // QT_CONFIG(tooltip)
        pushRemove->setText(QCoreApplication::translate("ListField", "Remove", nullptr));
#if QT_CONFIG(tooltip)
        pushAdd->setToolTip(QCoreApplication::translate("ListField", "Add new item", nullptr));
#endif // QT_CONFIG(tooltip)
        pushAdd->setText(QCoreApplication::translate("ListField", "Add", nullptr));
        pushUp->setText(QCoreApplication::translate("ListField", "Up", nullptr));
        pushDown->setText(QCoreApplication::translate("ListField", "Down", nullptr));
    } // retranslateUi

};

namespace Ui {
    class ListField: public Ui_ListField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_LISTFIELD_H
