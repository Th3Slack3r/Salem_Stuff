#include "floatfield.h"

FloatField::FloatField(const QString &title, double value, QWidget *parent, double min, double max) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    doubleSpinBox->setValue(value);
    doubleSpinBox->setMinimum(min);
    doubleSpinBox->setMaximum(max);

    connect(doubleSpinBox, SIGNAL(valueChanged(double)), SLOT(onFieldChanged()));
}

double FloatField::getValue() const
{
    return doubleSpinBox->value();
}

void FloatField::setValue(double d)
{
    doubleSpinBox->setValue(d);
}

void FloatField::setDecimals(int d)
{
    doubleSpinBox->setDecimals(d);
}

void FloatField::setSingleStep(double s)
{
    doubleSpinBox->setSingleStep(s);
}

void FloatField::setReadOnly(bool r)
{
    Field::setReadOnly(r);
    doubleSpinBox->setReadOnly(r);
}
