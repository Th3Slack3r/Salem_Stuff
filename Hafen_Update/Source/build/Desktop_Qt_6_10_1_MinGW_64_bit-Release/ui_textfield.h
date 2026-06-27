/********************************************************************************
** Form generated from reading UI file 'textfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_TEXTFIELD_H
#define UI_TEXTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QLabel>
#include <QtWidgets/QPlainTextEdit>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_TextField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QPlainTextEdit *plainTextEdit;

    void setupUi(QWidget *TextField)
    {
        if (TextField->objectName().isEmpty())
            TextField->setObjectName("TextField");
        TextField->resize(89, 108);
        verticalLayout = new QVBoxLayout(TextField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(TextField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        plainTextEdit = new QPlainTextEdit(TextField);
        plainTextEdit->setObjectName("plainTextEdit");

        verticalLayout->addWidget(plainTextEdit);


        retranslateUi(TextField);

        QMetaObject::connectSlotsByName(TextField);
    } // setupUi

    void retranslateUi(QWidget *TextField)
    {
        TextField->setWindowTitle(QCoreApplication::translate("TextField", "Form", nullptr));
        label->setText(QCoreApplication::translate("TextField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class TextField: public Ui_TextField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_TEXTFIELD_H
