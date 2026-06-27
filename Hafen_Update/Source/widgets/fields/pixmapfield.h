#ifndef PIXMAPFIELD_H
#define PIXMAPFIELD_H

#include "ui_pixmapfield.h"
#include "../field.h"

class PixmapField : public Field, private Ui::PixmapField
{
    Q_OBJECT

public:
    explicit PixmapField(const QString &title, const QPixmap &pixmap, QWidget *parent = 0);

    const QPixmap getValue() const;
    void setValue(const QPixmap &v);

private slots:
    void on_pushSave_clicked();
    void on_pushLoad_clicked();

private:
    QPixmap m_pixmap;
};

#endif // PIXMAPFIELD_H
