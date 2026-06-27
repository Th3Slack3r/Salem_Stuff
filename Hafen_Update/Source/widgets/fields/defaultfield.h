#ifndef DEFAULTFIELD_H
#define DEFAULTFIELD_H

#include "ui_defaultfield.h"
#include "../field.h"

class DefaultField : public Field, private Ui::DefaultField
{
    Q_OBJECT

public:
    explicit DefaultField(const QString &title, QWidget *parent = 0);
};

#endif // DEFAULTFIELD_H
