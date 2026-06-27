/********************************************************************************
** Form generated from reading UI file 'pointdialog.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_POINTDIALOG_H
#define UI_POINTDIALOG_H

#include <QtCore/QVariant>
#include <QtWidgets/QAbstractButton>
#include <QtWidgets/QApplication>
#include <QtWidgets/QDialog>
#include <QtWidgets/QDialogButtonBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QSpinBox>
#include <QtWidgets/QVBoxLayout>

QT_BEGIN_NAMESPACE

class Ui_PointDialog
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *labelX;
    QSpinBox *spinBoxX;
    QHBoxLayout *horizontalLayout_2;
    QLabel *labelY;
    QSpinBox *spinBoxY;
    QDialogButtonBox *buttonBox;

    void setupUi(QDialog *PointDialog)
    {
        if (PointDialog->objectName().isEmpty())
            PointDialog->setObjectName("PointDialog");
        PointDialog->resize(174, 97);
        verticalLayout = new QVBoxLayout(PointDialog);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        labelX = new QLabel(PointDialog);
        labelX->setObjectName("labelX");

        horizontalLayout->addWidget(labelX);

        spinBoxX = new QSpinBox(PointDialog);
        spinBoxX->setObjectName("spinBoxX");
        spinBoxX->setMinimum(-999999);
        spinBoxX->setMaximum(999999);

        horizontalLayout->addWidget(spinBoxX);


        verticalLayout->addLayout(horizontalLayout);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2->setObjectName("horizontalLayout_2");
        labelY = new QLabel(PointDialog);
        labelY->setObjectName("labelY");

        horizontalLayout_2->addWidget(labelY);

        spinBoxY = new QSpinBox(PointDialog);
        spinBoxY->setObjectName("spinBoxY");
        spinBoxY->setMinimum(-999999);
        spinBoxY->setMaximum(999999);

        horizontalLayout_2->addWidget(spinBoxY);


        verticalLayout->addLayout(horizontalLayout_2);

        buttonBox = new QDialogButtonBox(PointDialog);
        buttonBox->setObjectName("buttonBox");
        buttonBox->setOrientation(Qt::Horizontal);
        buttonBox->setStandardButtons(QDialogButtonBox::Cancel|QDialogButtonBox::Ok);

        verticalLayout->addWidget(buttonBox);


        retranslateUi(PointDialog);
        QObject::connect(buttonBox, &QDialogButtonBox::accepted, PointDialog, qOverload<>(&QDialog::accept));
        QObject::connect(buttonBox, &QDialogButtonBox::rejected, PointDialog, qOverload<>(&QDialog::reject));

        QMetaObject::connectSlotsByName(PointDialog);
    } // setupUi

    void retranslateUi(QDialog *PointDialog)
    {
        PointDialog->setWindowTitle(QCoreApplication::translate("PointDialog", "Point editor", nullptr));
        labelX->setText(QCoreApplication::translate("PointDialog", "x:", nullptr));
        labelY->setText(QCoreApplication::translate("PointDialog", "y:", nullptr));
    } // retranslateUi

};

namespace Ui {
    class PointDialog: public Ui_PointDialog {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_POINTDIALOG_H
