/********************************************************************************
** Form generated from reading UI file 'floatfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_FLOATFIELD_H
#define UI_FLOATFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QDoubleSpinBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_FloatField
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *label;
    QDoubleSpinBox *doubleSpinBox;

    void setupUi(QWidget *FloatField)
    {
        if (FloatField->objectName().isEmpty())
            FloatField->setObjectName("FloatField");
        FloatField->resize(126, 40);
        verticalLayout = new QVBoxLayout(FloatField);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        label = new QLabel(FloatField);
        label->setObjectName("label");

        horizontalLayout->addWidget(label);

        doubleSpinBox = new QDoubleSpinBox(FloatField);
        doubleSpinBox->setObjectName("doubleSpinBox");
        doubleSpinBox->setDecimals(4);
        doubleSpinBox->setMinimum(-999999.000000000000000);
        doubleSpinBox->setMaximum(1000000.000000000000000);

        horizontalLayout->addWidget(doubleSpinBox);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(FloatField);

        QMetaObject::connectSlotsByName(FloatField);
    } // setupUi

    void retranslateUi(QWidget *FloatField)
    {
        FloatField->setWindowTitle(QCoreApplication::translate("FloatField", "Form", nullptr));
        label->setText(QCoreApplication::translate("FloatField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class FloatField: public Ui_FloatField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_FLOATFIELD_H
