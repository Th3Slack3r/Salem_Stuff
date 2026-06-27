#include "booleanfield.h"

BooleanField::BooleanField(const QString &title, bool value, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    checkBox->setText(QString(title));
    checkBox->setChecked(value);

    connect(checkBox, SIGNAL(toggled(bool)), SLOT(onFieldChanged()));
}

bool BooleanField::getValue() const
{
    return checkBox->isChecked();
}

void BooleanField::setValue(bool v)
{
    checkBox->setChecked(v);
}
