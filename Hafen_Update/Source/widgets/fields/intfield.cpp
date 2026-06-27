#include "intfield.h"

IntField::IntField(const QString &title, int value, QWidget *parent, int min, int max) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    spinBox->setValue(value);
    spinBox->setMinimum(min);
    spinBox->setMaximum(max);

    connect(spinBox, SIGNAL(valueChanged(int)), SLOT(onFieldChanged()));
}

int IntField::getValue() const
{
    return spinBox->value();
}

void IntField::setValue(int v)
{
    spinBox->setValue(v);
}

void IntField::setReadOnly(bool ro)
{
    Field::setReadOnly(ro);
    spinBox->setReadOnly(ro);
}
