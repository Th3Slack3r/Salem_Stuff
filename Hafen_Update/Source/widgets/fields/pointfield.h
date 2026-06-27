#ifndef POINTFIELD_H
#define POINTFIELD_H

#include "ui_pointfield.h"
#include "../field.h"

class PointField : public Field, private Ui::PointField
{
    Q_OBJECT

public:
    explicit PointField(const QString &title, const QPoint &point, bool shrt = false, QWidget *parent = 0);

    const QPoint getValue() const;
    void setValue(const QPoint &v);
    void setReadOnly(bool ro);
};

#endif // POINTFIELD_H
