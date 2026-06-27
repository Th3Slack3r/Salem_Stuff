#ifndef COLORFIELD_H
#define COLORFIELD_H

#include "ui_colorfield.h"
#include "../field.h"

class ColorField : public Field, private Ui::ColorField
{
    Q_OBJECT

public:
    explicit ColorField(const QString &title, const QColor &value, QWidget *parent = 0);

    const QColor getValue() const {return m_value;}

private slots:
    void on_pushButton_clicked();

private:
    QColor m_value;

    void setColorLabelText(const QColor &value);
    void setWidgetBgColor(const QColor &value);
};

#endif // COLORFIELD_H
