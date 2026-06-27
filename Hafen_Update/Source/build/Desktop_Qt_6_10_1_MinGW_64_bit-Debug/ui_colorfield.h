/********************************************************************************
** Form generated from reading UI file 'colorfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_COLORFIELD_H
#define UI_COLORFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QSpacerItem>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_ColorField
{
public:
    QVBoxLayout *verticalLayout;
    QLabel *label;
    QHBoxLayout *horizontalLayout;
    QWidget *widget;
    QLabel *labelColor;
    QPushButton *pushButton;
    QSpacerItem *horizontalSpacer;

    void setupUi(QWidget *ColorField)
    {
        if (ColorField->objectName().isEmpty())
            ColorField->setObjectName("ColorField");
        ColorField->resize(400, 62);
        verticalLayout = new QVBoxLayout(ColorField);
        verticalLayout->setObjectName("verticalLayout");
        label = new QLabel(ColorField);
        label->setObjectName("label");

        verticalLayout->addWidget(label);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        widget = new QWidget(ColorField);
        widget->setObjectName("widget");
        widget->setMinimumSize(QSize(23, 23));
        widget->setMaximumSize(QSize(23, 23));

        horizontalLayout->addWidget(widget);

        labelColor = new QLabel(ColorField);
        labelColor->setObjectName("labelColor");

        horizontalLayout->addWidget(labelColor);

        pushButton = new QPushButton(ColorField);
        pushButton->setObjectName("pushButton");

        horizontalLayout->addWidget(pushButton);

        horizontalSpacer = new QSpacerItem(40, 20, QSizePolicy::Policy::Expanding, QSizePolicy::Policy::Minimum);

        horizontalLayout->addItem(horizontalSpacer);


        verticalLayout->addLayout(horizontalLayout);


        retranslateUi(ColorField);

        QMetaObject::connectSlotsByName(ColorField);
    } // setupUi

    void retranslateUi(QWidget *ColorField)
    {
        ColorField->setWindowTitle(QCoreApplication::translate("ColorField", "Form", nullptr));
        label->setText(QCoreApplication::translate("ColorField", "TextLabel", nullptr));
        labelColor->setText(QCoreApplication::translate("ColorField", "TextLabel", nullptr));
        pushButton->setText(QCoreApplication::translate("ColorField", "Change", nullptr));
    } // retranslateUi

};

namespace Ui {
    class ColorField: public Ui_ColorField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_COLORFIELD_H
