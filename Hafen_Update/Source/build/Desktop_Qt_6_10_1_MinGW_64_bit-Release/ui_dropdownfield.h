/********************************************************************************
** Form generated from reading UI file 'dropdownfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_DROPDOWNFIELD_H
#define UI_DROPDOWNFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QComboBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_DropdownField
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *label;
    QComboBox *comboBox;

    void setupUi(QWidget *DropdownField)
    {
        if (DropdownField->objectName().isEmpty())
            DropdownField->setObjectName("DropdownField");
        DropdownField->resize(142, 40);
        verticalLayout = new QVBoxLayout(DropdownField);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        label = new QLabel(DropdownField);
        label->setObjectName("label");

        horizontalLayout->addWidget(label);

        comboBox = new QComboBox(DropdownField);
        comboBox->setObjectName("comboBox");

        horizontalLayout->addWidget(comboBox);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(DropdownField);

        QMetaObject::connectSlotsByName(DropdownField);
    } // setupUi

    void retranslateUi(QWidget *DropdownField)
    {
        DropdownField->setWindowTitle(QCoreApplication::translate("DropdownField", "Form", nullptr));
        label->setText(QCoreApplication::translate("DropdownField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class DropdownField: public Ui_DropdownField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_DROPDOWNFIELD_H
