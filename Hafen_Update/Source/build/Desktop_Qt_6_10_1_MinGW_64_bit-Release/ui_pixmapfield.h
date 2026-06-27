/********************************************************************************
** Form generated from reading UI file 'pixmapfield.ui'
**
** Created by: Qt User Interface Compiler version 6.10.1
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_PIXMAPFIELD_H
#define UI_PIXMAPFIELD_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QSpacerItem>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_PixmapField
{
public:
    QVBoxLayout *verticalLayout_2;
    QHBoxLayout *horizontalLayout_2;
    QLabel *labelTitle;
    QVBoxLayout *verticalLayout;
    QLabel *labelPixmap;
    QHBoxLayout *horizontalLayout;
    QPushButton *pushSave;
    QPushButton *pushLoad;
    QSpacerItem *horizontalSpacer;

    void setupUi(QWidget *PixmapField)
    {
        if (PixmapField->objectName().isEmpty())
            PixmapField->setObjectName("PixmapField");
        PixmapField->resize(339, 382);
        verticalLayout_2 = new QVBoxLayout(PixmapField);
        verticalLayout_2->setObjectName("verticalLayout_2");
        horizontalLayout_2 = new QHBoxLayout();
        horizontalLayout_2->setObjectName("horizontalLayout_2");
        labelTitle = new QLabel(PixmapField);
        labelTitle->setObjectName("labelTitle");

        horizontalLayout_2->addWidget(labelTitle);

        verticalLayout = new QVBoxLayout();
        verticalLayout->setObjectName("verticalLayout");
        labelPixmap = new QLabel(PixmapField);
        labelPixmap->setObjectName("labelPixmap");

        verticalLayout->addWidget(labelPixmap);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setObjectName("horizontalLayout");
        pushSave = new QPushButton(PixmapField);
        pushSave->setObjectName("pushSave");

        horizontalLayout->addWidget(pushSave);

        pushLoad = new QPushButton(PixmapField);
        pushLoad->setObjectName("pushLoad");

        horizontalLayout->addWidget(pushLoad);

        horizontalSpacer = new QSpacerItem(40, 20, QSizePolicy::Policy::Expanding, QSizePolicy::Policy::Minimum);

        horizontalLayout->addItem(horizontalSpacer);


        verticalLayout->addLayout(horizontalLayout);


        horizontalLayout_2->addLayout(verticalLayout);


        verticalLayout_2->addLayout(horizontalLayout_2);


        retranslateUi(PixmapField);

        QMetaObject::connectSlotsByName(PixmapField);
    } // setupUi

    void retranslateUi(QWidget *PixmapField)
    {
        PixmapField->setWindowTitle(QCoreApplication::translate("PixmapField", "Form", nullptr));
        labelTitle->setText(QCoreApplication::translate("PixmapField", "TextLabel", nullptr));
        labelPixmap->setText(QString());
#if QT_CONFIG(tooltip)
        pushSave->setToolTip(QCoreApplication::translate("PixmapField", "Save current pixmap to disk", nullptr));
#endif // QT_CONFIG(tooltip)
        pushSave->setText(QCoreApplication::translate("PixmapField", "Save", nullptr));
#if QT_CONFIG(tooltip)
        pushLoad->setToolTip(QCoreApplication::translate("PixmapField", "Load pixmap from disk", nullptr));
#endif // QT_CONFIG(tooltip)
        pushLoad->setText(QCoreApplication::translate("PixmapField", "Load", nullptr));
    } // retranslateUi

};

namespace Ui {
    class PixmapField: public Ui_PixmapField {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_PIXMAPFIELD_H
