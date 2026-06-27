/********************************************************************************
** Form generated from reading UI file 'booleanfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_BOOLEANFIELD_H
#define UI_BOOLEANFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QCheckBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_BooleanField
{
public:
    QHBoxLayout *horizontalLayout;
    QCheckBox *checkBox;

    void setupUi(QWidget *BooleanField)
    {
        if (BooleanField->objectName().isEmpty())
            BooleanField->setObjectName("BooleanField");
        BooleanField->resize(88, 35);
        horizontalLayout = new QHBoxLayout(BooleanField);
        horizontalLayout->setObjectName("horizontalLayout");
        checkBox = new QCheckBox(BooleanField);
        checkBox->setObjectName("checkBox");

        horizontalLayout->addWidget(checkBox);


        retranslateUi(BooleanField);

        QMetaObject::connectSlotsByName(BooleanField);
    } // setupUi

    void retranslateUi(QWidget *BooleanField)
    {
        BooleanField->setWindowTitle(QCoreApplication::translate("BooleanField", "Form", nullptr));
        checkBox->setText(QCoreApplication::translate("BooleanField", "CheckBox", nullptr));
    } // retranslateUi

};

namespace Ui {
    class BooleanField: public Ui_BooleanField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_BOOLEANFIELD_H
