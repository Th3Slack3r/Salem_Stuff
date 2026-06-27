#include "colorfield.h"
#include <QColorDialog>
#include <QPalette>

ColorField::ColorField(const QString &title, const QColor &value, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    m_value = value;
    label->setText(QString("%1:").arg(title));
    setColorLabelText(value);
    setWidgetBgColor(value);
}

void ColorField::on_pushButton_clicked()
{
    QColor c = QColorDialog::getColor(m_value, this, "New color", QColorDialog::ShowAlphaChannel);
    if (!c.isValid())
        return;

    m_value = c;
    setColorLabelText(c);
    setWidgetBgColor(c);
    onFieldChanged();
}

void ColorField::setColorLabelText(const QColor &value)
{
    labelColor->setText(QString("RGBA(%1, %2, %3, %4)")
                        .arg(value.red()).arg(value.green()).arg(value.blue()).arg(value.alpha()));
}

void ColorField::setWidgetBgColor(const QColor &value)
{
    QPalette p = widget->palette();
#if QT_VERSION < QT_VERSION_CHECK(6, 0, 0)
    p.setColor(QPalette::Background, value);
#else
    p.setColor(QPalette::Window, value);
#endif
    widget->setPalette(p);
    widget->setAutoFillBackground(true);
    widget->update();
}
