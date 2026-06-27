#ifndef POINT3FDIALOG_H
#define POINT3FDIALOG_H

#include "ui_point3fdialog.h"
#include "../../resources/types/point3f.h"

class Point3fDialog : public QDialog, private Ui::Point3fDialog
{
    Q_OBJECT

public:
    explicit Point3fDialog(const Point3F &value, QWidget *parent = 0);

    const Point3F getValue() const;
};

#endif // POINT3FDIALOG_H
