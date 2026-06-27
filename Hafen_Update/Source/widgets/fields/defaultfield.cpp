#include "defaultfield.h"

DefaultField::DefaultField(const QString &title, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(title);
}
