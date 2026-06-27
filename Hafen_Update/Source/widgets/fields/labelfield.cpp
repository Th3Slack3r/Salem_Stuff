#include "labelfield.h"

LabelField::LabelField(const QString &title, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(title);
}
