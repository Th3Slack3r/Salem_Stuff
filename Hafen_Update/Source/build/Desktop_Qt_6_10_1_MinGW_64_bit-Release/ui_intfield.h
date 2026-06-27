/********************************************************************************
** Form generated from reading UI file 'intfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_INTFIELD_H
#define UI_INTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QSpinBox>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_IntField
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *label;
    QSpinBox *spinBox;

    void setupUi(QWidget *IntField)
    {
        if (IntField->objectName().isEmpty())
            IntField->setObjectName("IntField");
        IntField->resize(154, 40);
        verticalLayout = new QVBoxLayout(IntField);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        label = new QLabel(IntField);
        label->setObjectName("label");

        horizontalLayout->addWidget(label);

        spinBox = new QSpinBox(IntField);
        spinBox->setObjectName("spinBox");
        spinBox->setMinimum(-9999999);
        spinBox->setMaximum(9999999);

        horizontalLayout->addWidget(spinBox);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(IntField);

        QMetaObject::connectSlotsByName(IntField);
    } // setupUi

    void retranslateUi(QWidget *IntField)
    {
        IntField->setWindowTitle(QCoreApplication::translate("IntField", "Form", nullptr));
        label->setText(QCoreApplication::translate("IntField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class IntField: public Ui_IntField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_INTFIELD_H
