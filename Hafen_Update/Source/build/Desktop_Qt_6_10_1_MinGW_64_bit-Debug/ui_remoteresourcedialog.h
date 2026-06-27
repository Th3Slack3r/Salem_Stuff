/********************************************************************************
** Form generated from reading UI file 'remoteresourcedialog.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_REMOTERESOURCEDIALOG_H
#define UI_REMOTERESOURCEDIALOG_H

#include <QtCore/QVariant>
#include <QtWidgets/QAbstractButton>
#include <QtWidgets/QApplication>
#include <QtWidgets/QDialog>
#include <QtWidgets/QDialogButtonBox>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QVBoxLayout>

QT_BEGIN_NAMESPACE

class Ui_RemoteResourceDialog
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QLineEdit *lineEdit;
    QDialogButtonBox *buttonBox;

    void setupUi(QDialog *RemoteResourceDialog)
    {
        if (RemoteResourceDialog->objectName().isEmpty())
            RemoteResourceDialog->setObjectName("RemoteResourceDialog");
        RemoteResourceDialog->resize(430, 86);
        verticalLayout = new QVBoxLayout(RemoteResourceDialog);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(RemoteResourceDialog);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        lineEdit = new QLineEdit(RemoteResourceDialog);
        lineEdit->setObjectName("lineEdit");

        verticalLayout->addWidget(lineEdit);

        buttonBox = new QDialogButtonBox(RemoteResourceDialog);
        buttonBox->setObjectName("buttonBox");
        buttonBox->setOrientation(Qt::Horizontal);
        buttonBox->setStandardButtons(QDialogButtonBox::Cancel|QDialogButtonBox::Ok);

        verticalLayout->addWidget(buttonBox);


        retranslateUi(RemoteResourceDialog);
        QObject::connect(buttonBox, &QDialogButtonBox::accepted, RemoteResourceDialog, qOverload<>(&QDialog::accept));
        QObject::connect(buttonBox, &QDialogButtonBox::rejected, RemoteResourceDialog, qOverload<>(&QDialog::reject));

        QMetaObject::connectSlotsByName(RemoteResourceDialog);
    } // setupUi

    void retranslateUi(QDialog *RemoteResourceDialog)
    {
        RemoteResourceDialog->setWindowTitle(QCoreApplication::translate("RemoteResourceDialog", "Remote resource", nullptr));
        label->setText(QCoreApplication::translate("RemoteResourceDialog", "Enter resource name:", nullptr));
    } // retranslateUi

};

namespace Ui {
    class RemoteResourceDialog: public Ui_RemoteResourceDialog {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_REMOTERESOURCEDIALOG_H
