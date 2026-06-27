#ifndef DROPDOWNFIELD_H
#define DROPDOWNFIELD_H

#include "ui_dropdownfield.h"
#include "../field.h"

class DropdownField : public Field, private Ui::DropdownField
{
    Q_OBJECT

public:
    explicit DropdownField(const QString &title, const QList<QString> &value,
                           int index = 0, QWidget *parent = 0);

    int getIndex() const;
    const QString getValue() const;
    void setIndex(int idx);
    void setValue(int idx, const QString &v);
    void setValues(const QList<QString> &v);
};

#endif // DROPDOWNFIELD_H
