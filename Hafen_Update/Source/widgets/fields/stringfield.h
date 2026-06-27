#ifndef STRINGFIELD_H
#define STRINGFIELD_H

#include "ui_stringfield.h"
#include "../field.h"

class StringField : public Field, private Ui::StringField
{
    Q_OBJECT

public:
    explicit StringField(const QString &title, const QString &value, QWidget *parent = 0);

    const QString getValue() const;
    void setValue(const QString &v);
    void setReadOnly(bool r);
};

#endif // STRINGFIELD_H
