/********************************************************************************
** Form generated from reading UI file 'labelfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_LABELFIELD_H
#define UI_LABELFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QLabel>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_LabelField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;

    void setupUi(QWidget *LabelField)
    {
        if (LabelField->objectName().isEmpty())
            LabelField->setObjectName("LabelField");
        LabelField->resize(65, 31);
        verticalLayout = new QVBoxLayout(LabelField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(LabelField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);


        retranslateUi(LabelField);

        QMetaObject::connectSlotsByName(LabelField);
    } // setupUi

    void retranslateUi(QWidget *LabelField)
    {
        LabelField->setWindowTitle(QCoreApplication::translate("LabelField", "Form", nullptr));
        label->setText(QCoreApplication::translate("LabelField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class LabelField: public Ui_LabelField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_LABELFIELD_H
