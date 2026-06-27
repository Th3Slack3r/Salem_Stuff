#include "pixmapfield.h"

PixmapField::PixmapField(const QString &title, const QPixmap &pixmap, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    labelTitle->setText(QString("%1:").arg(title));
    if (!pixmap.isNull()) {
        labelPixmap->setPixmap(pixmap);
        m_pixmap = pixmap;
    } else {
        labelPixmap->setText("Ivalid pixmap");
    }
}

const QPixmap PixmapField::getValue() const
{
    return m_pixmap;
}

void PixmapField::setValue(const QPixmap &v)
{
    m_pixmap = v;
    if (!v.isNull()) {
        labelPixmap->setPixmap(v);
        labelPixmap->setMinimumSize(v.size());
        onFieldChanged();
    }
}

void PixmapField::on_pushSave_clicked()
{
    QString save = QFileDialog::getSaveFileName(this, "Save image", "./saved", "Images (*.png *.jpg *.jpeg)");
    if (save.isEmpty())
        return;

    if (!m_pixmap.save(save))
        QMessageBox::warning(this, "Warning", "Failed to save image.");
}

void PixmapField::on_pushLoad_clicked()
{
    QString open = QFileDialog::getOpenFileName(this, "Open image", "./saved", "Images (*.png *.jpg *.jpeg)");
    if (open.isEmpty())
        return;

    QPixmap p;
    if (!p.load(open) || p.isNull()) {
        QMessageBox::warning(this, "Warning", "Failed to load image.");
        return;
    }

    m_pixmap = p;
    labelPixmap->setPixmap(p);
    labelPixmap->setMinimumSize(p.size());
    onFieldChanged();
}
