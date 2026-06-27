/********************************************************************************
** Form generated from reading UI file 'pointfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_POINTFIELD_H
#define UI_POINTFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QSpinBox>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_PointField
{
public:
    QVBoxLayout *verticalLayout;
    QHBoxLayout *horizontalLayout;
    QLabel *labelX;
    QSpinBox *spinBoxX;
    QLabel *labelY;
    QSpinBox *spinBoxY;

    void setupUi(QWidget *PointField)
    {
        if (PointField->objectName().isEmpty())
            PointField->setObjectName("PointField");
        PointField->resize(206, 40);
        verticalLayout = new QVBoxLayout(PointField);
        verticalLayout->setObjectName("verticalLayout");
        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        labelX = new QLabel(PointField);
        labelX->setObjectName("labelX");

        horizontalLayout->addWidget(labelX);

        spinBoxX = new QSpinBox(PointField);
        spinBoxX->setObjectName("spinBoxX");
        spinBoxX->setMinimum(-9999999);
        spinBoxX->setMaximum(9999999);
        spinBoxX->setValue(0);

        horizontalLayout->addWidget(spinBoxX);

        labelY = new QLabel(PointField);
        labelY->setObjectName("labelY");

        horizontalLayout->addWidget(labelY);

        spinBoxY = new QSpinBox(PointField);
        spinBoxY->setObjectName("spinBoxY");
        spinBoxY->setMinimum(-9999999);
        spinBoxY->setMaximum(9999999);
        spinBoxY->setValue(0);

        horizontalLayout->addWidget(spinBoxY);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(PointField);

        QMetaObject::connectSlotsByName(PointField);
    } // setupUi

    void retranslateUi(QWidget *PointField)
    {
        PointField->setWindowTitle(QCoreApplication::translate("PointField", "Form", nullptr));
        labelX->setText(QCoreApplication::translate("PointField", "TextLabel", nullptr));
        labelY->setText(QCoreApplication::translate("PointField", "TextLabel", nullptr));
    } // retranslateUi

};

namespace Ui {
    class PointField: public Ui_PointField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_POINTFIELD_H
