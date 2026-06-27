#ifndef POINTDIALOG_H
#define POINTDIALOG_H

#include "ui_pointdialog.h"
#include <QPoint>

class PointDialog : public QDialog, private Ui::PointDialog
{
    Q_OBJECT

public:
    explicit PointDialog(const QPoint &value, QWidget *parent = 0);

    const QPoint getCoord() const;
};

#endif // POINTDIALOG_H
