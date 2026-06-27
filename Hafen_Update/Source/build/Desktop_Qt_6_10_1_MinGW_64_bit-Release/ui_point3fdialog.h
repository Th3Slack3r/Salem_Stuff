/********************************************************************************
** Form generated from reading UI file 'point3fdialog.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_POINT3FDIALOG_H
#define UI_POINT3FDIALOG_H

#include <QtCore/QVariant>
#include <QtWidgets/QAbstractButton>
#include <QtWidgets/QApplication>
#include <QtWidgets/QDialog>
#include <QtWidgets/QDialogButtonBox>
#include <QtWidgets/QDoubleSpinBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QVBoxLayout>

QT_BEGIN_NAMESPACE

class Ui_Point3fDialog
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *labelX;
    QDoubleSpinBox *spinX;
    QHBoxLayout *horizontalLayout_2;
    QLabel *labelY;
    QDoubleSpinBox *spinY;
    QHBoxLayout *horizontalLayout_3;
    QLabel *labelZ;
    QDoubleSpinBox *spinZ;
    QDialogButtonBox *buttonBox;

    void setupUi(QDialog *Point3fDialog)
    {
        if (Point3fDialog->objectName().isEmpty())
            Point3fDialog->setObjectName("Point3fDialog");
        Point3fDialog->resize(260, 125);
        Point3fDialog->setMinimumSize(QSize(260, 125));
        Point3fDialog->setMaximumSize(QSize(16777215, 125));
        verticalLayout = new QVBoxLayout(Point3fDialog);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        labelX = new QLabel(Point3fDialog);
        labelX->setObjectName("labelX");

        horizontalLayout->addWidget(labelX);

        spinX = new QDoubleSpinBox(Point3fDialog);
        spinX->setObjectName("spinX");
        spinX->setDecimals(6);
        spinX->setMinimum(-9999.000000000000000);
        spinX->setMaximum(9999.989999999999782);

        horizontalLayout->addWidget(spinX);


        verticalLayout->addLayout(horizontalLayout);

        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2->setObjectName("horizontalLayout_2");
        labelY = new QLabel(Point3fDialog);
        labelY->setObjectName("labelY");

        horizontalLayout_2->addWidget(labelY);

        spinY = new QDoubleSpinBox(Point3fDialog);
        spinY->setObjectName("spinY");
        spinY->setDecimals(6);
        spinY->setMinimum(-9999.000000000000000);
        spinY->setMaximum(9999.989999999999782);

        horizontalLayout_2->addWidget(spinY);


        verticalLayout->addLayout(horizontalLayout_2);

        horizontalLayout_3 = new QHBoxLayout();
        horizontalLayout_3->setObjectName("horizontalLayout_3");
        labelZ = new QLabel(Point3fDialog);
        labelZ->setObjectName("labelZ");

        horizontalLayout_3->addWidget(labelZ);

        spinZ = new QDoubleSpinBox(Point3fDialog);
        spinZ->setObjectName("spinZ");
        spinZ->setDecimals(6);
        spinZ->setMinimum(-9999.000000000000000);
        spinZ->setMaximum(9999.989999999999782);

        horizontalLayout_3->addWidget(spinZ);


        verticalLayout->addLayout(horizontalLayout_3);

        buttonBox = new QDialogButtonBox(Point3fDialog);
        buttonBox->setObjectName("buttonBox");
        buttonBox->setOrientation(Qt::Horizontal);
        buttonBox->setStandardButtons(QDialogButtonBox::Cancel|QDialogButtonBox::Ok);

        verticalLayout->addWidget(buttonBox);


        retranslateUi(Point3fDialog);
        QObject::connect(buttonBox, &QDialogButtonBox::accepted, Point3fDialog, qOverload<>(&QDialog::accept));
        QObject::connect(buttonBox, &QDialogButtonBox::rejected, Point3fDialog, qOverload<>(&QDialog::reject));

        QMetaObject::connectSlotsByName(Point3fDialog);
    } // setupUi

    void retranslateUi(QDialog *Point3fDialog)
    {
        Point3fDialog->setWindowTitle(QCoreApplication::translate("Point3fDialog", "Point3f editor", nullptr));
        labelX->setText(QCoreApplication::translate("Point3fDialog", "x:", nullptr));
        labelY->setText(QCoreApplication::translate("Point3fDialog", "y:", nullptr));
        labelZ->setText(QCoreApplication::translate("Point3fDialog", "z:", nullptr));
    } // retranslateUi

};

namespace Ui {
    class Point3fDialog: public Ui_Point3fDialog {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_POINT3FDIALOG_H
