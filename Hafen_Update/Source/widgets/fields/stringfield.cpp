#include "stringfield.h"

StringField::StringField(const QString &title, const QString &value, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    lineEdit->setText(value);

    connect(lineEdit, SIGNAL(textChanged(QString)), SLOT(onFieldChanged()));
}

const QString StringField::getValue() const
{
    return lineEdit->text();
}

void StringField::setValue(const QString &v)
{
    lineEdit->setText(v);
}

void StringField::setReadOnly(bool r)
{
    Field::setReadOnly(r);
    lineEdit->setReadOnly(r);
}
