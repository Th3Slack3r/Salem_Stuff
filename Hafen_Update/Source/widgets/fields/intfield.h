#ifndef INTFIELD_H
#define INTFIELD_H

#include "ui_intfield.h"
#include "../field.h"

class IntField : public Field, private Ui::IntField
{
    Q_OBJECT

public:
    explicit IntField(const QString &title, int value,
                      QWidget *parent = 0, int min = -9999999, int max = 9999999);

    int getValue() const;
    void setValue(int v);
    void setReadOnly(bool ro);
};

#endif // INTFIELD_H
