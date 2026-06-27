#include "dropdownfield.h"

DropdownField::DropdownField(const QString &title, const QList<QString> &value, int index, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    comboBox->addItems(QStringList(value));
    comboBox->setCurrentIndex(index);
}

int DropdownField::getIndex() const
{
    return comboBox->currentIndex();
}

const QString DropdownField::getValue() const
{
    return comboBox->currentText();
}

void DropdownField::setIndex(int idx)
{
    comboBox->setCurrentIndex(idx);
}

void DropdownField::setValue(int idx, const QString &v)
{
    comboBox->setItemText(idx, v);
}

void DropdownField::setValues(const QList<QString> &v)
{
    comboBox->clear();
    comboBox->addItems(QStringList(v));
}
