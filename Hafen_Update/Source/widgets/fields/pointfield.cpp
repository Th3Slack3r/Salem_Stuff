#include "pointfield.h"

PointField::PointField(const QString &title, const QPoint &point, bool shrt, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    labelX->setText(QString("%1.x:").arg(title));
    labelY->setText(QString("%1.y:").arg(title));

    spinBoxX->setValue(point.x());
    spinBoxY->setValue(point.y());

    // Set valid short ranges if coord16
    if (shrt) {
        spinBoxX->setMinimum(-32768); spinBoxX->setMaximum(32767);
        spinBoxY->setMinimum(-32768); spinBoxY->setMaximum(32767);
    }


    connect(spinBoxX, SIGNAL(valueChanged(int)), SLOT(onFieldChanged()));
    connect(spinBoxY, SIGNAL(valueChanged(int)), SLOT(onFieldChanged()));
}

const QPoint PointField::getValue() const
{
    return QPoint(spinBoxX->value(), spinBoxY->value());
}

void PointField::setValue(const QPoint &v)
{
    spinBoxX->setValue(v.x());
    spinBoxY->setValue(v.y());
}

void PointField::setReadOnly(bool ro)
{
    Field::setReadOnly(ro);
    spinBoxX->setReadOnly(ro);
    spinBoxY->setReadOnly(ro);
}
