/********************************************************************************
** Form generated from reading UI file 'stringfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_STRINGFIELD_H
#define UI_STRINGFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_StringField
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *label;
    QLineEdit *lineEdit;

    void setupUi(QWidget *StringField)
    {
        if (StringField->objectName().isEmpty())
            StringField->setObjectName("StringField");
        StringField->resize(127, 40);
        verticalLayout = new QVBoxLayout(StringField);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        label = new QLabel(StringField);
        label->setObjectName("label");

        horizontalLayout->addWidget(label);

        lineEdit = new QLineEdit(StringField);
        lineEdit->setObjectName("lineEdit");

        horizontalLayout->addWidget(lineEdit);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(StringField);

        QMetaObject::connectSlotsByName(StringField);
    } // setupUi

    void retranslateUi(QWidget *StringField)
    {
        StringField->setWindowTitle(QCoreApplication::translate("StringField", "Form", nullptr));
        label->setText(QCoreApplication::translate("StringField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class StringField: public Ui_StringField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_STRINGFIELD_H
