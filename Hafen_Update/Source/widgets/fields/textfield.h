#ifndef TEXTFIELD_H
#define TEXTFIELD_H

#include "ui_textfield.h"
#include "../field.h"

class TextField : public Field, private Ui::TextField
{
    Q_OBJECT

public:
    explicit TextField(const QString &title, const QString &value, QWidget *parent = 0);

    const QString getValue() const;
    void setValue(const QString &v);
    void setReadOnly(bool r);
};

#endif // TEXTFIELD_H
