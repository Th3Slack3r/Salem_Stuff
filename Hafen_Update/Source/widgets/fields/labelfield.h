#ifndef LABELFIELD_H
#define LABELFIELD_H

#include "ui_labelfield.h"
#include "../field.h"

class LabelField : public Field, private Ui::LabelField
{
    Q_OBJECT

public:
    explicit LabelField(const QString &title, QWidget *parent = 0);
};

#endif // LABELFIELD_H
