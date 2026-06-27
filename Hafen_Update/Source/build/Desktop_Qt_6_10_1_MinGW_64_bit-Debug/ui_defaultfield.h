/********************************************************************************
** Form generated from reading UI file 'defaultfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_DEFAULTFIELD_H
#define UI_DEFAULTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QLabel>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_DefaultField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;

    void setupUi(QWidget *DefaultField)
    {
        if (DefaultField->objectName().isEmpty())
            DefaultField->setObjectName("DefaultField");
        DefaultField->resize(65, 31);
        verticalLayout = new QVBoxLayout(DefaultField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(DefaultField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);


        retranslateUi(DefaultField);

        QMetaObject::connectSlotsByName(DefaultField);
    } // setupUi

    void retranslateUi(QWidget *DefaultField)
    {
        DefaultField->setWindowTitle(QCoreApplication::translate("DefaultField", "Form", nullptr));
        label->setText(QCoreApplication::translate("DefaultField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class DefaultField: public Ui_DefaultField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_DEFAULTFIELD_H
