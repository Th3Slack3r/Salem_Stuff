#ifndef BOOLEANFIELD_H
#define BOOLEANFIELD_H

#include "ui_booleanfield.h"
#include "../field.h"

class BooleanField : public Field, private Ui::BooleanField
{
    Q_OBJECT

public:
    explicit BooleanField(const QString &title, bool value, QWidget *parent = 0);

    bool getValue() const;
    void setValue(bool v);
};

#endif // BOOLEANFIELD_H
