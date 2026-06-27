#ifndef FLOATFIELD_H
#define FLOATFIELD_H

#include "../field.h"
#include "ui_floatfield.h"

class FloatField : public Field, private Ui::FloatField
{
    Q_OBJECT

public:
    explicit FloatField(const QString &title, double value, QWidget *parent = 0,
                        double min = -999999, double max = 999999);

    double getValue() const;
    void setValue(double d);
    void setDecimals(int d);
    void setSingleStep(double s);

    void setReadOnly(bool r);
};

#endif // FLOATFIELD_H
