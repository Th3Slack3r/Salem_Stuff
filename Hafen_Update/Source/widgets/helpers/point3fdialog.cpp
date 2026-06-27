#include "point3fdialog.h"

Point3fDialog::Point3fDialog(const Point3F &value, QWidget *parent) :
    QDialog(parent)
{
    setupUi(this);
    spinX->setValue(value.x());
    spinY->setValue(value.y());
    spinZ->setValue(value.z());
}

const Point3F Point3fDialog::getValue() const
{
    return Point3F(spinX->value(), spinY->value(), spinZ->value());
}
