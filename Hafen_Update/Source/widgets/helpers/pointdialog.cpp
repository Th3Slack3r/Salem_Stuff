#include "pointdialog.h"

PointDialog::PointDialog(const QPoint &value, QWidget *parent) :
    QDialog(parent)
{
    setupUi(this);

    spinBoxX->setValue(value.x());
    spinBoxY->setValue(value.y());
    setFixedHeight(height());
}

const QPoint PointDialog::getCoord() const
{
    return QPoint(spinBoxX->value(), spinBoxY->value());
}
