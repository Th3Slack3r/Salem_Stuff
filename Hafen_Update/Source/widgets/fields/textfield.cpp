#include "textfield.h"

TextField::TextField(const QString &title, const QString &value, QWidget *parent) :
    Field(title, parent)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    plainTextEdit->setPlainText(value);

    connect(plainTextEdit, SIGNAL(textChanged()), SLOT(onFieldChanged()));
}

const QString TextField::getValue() const
{
    return plainTextEdit->toPlainText();
}

void TextField::setValue(const QString &v)
{
    plainTextEdit->setPlainText(v);
}

void TextField::setReadOnly(bool r)
{
    Field::setReadOnly(r);
    plainTextEdit->setReadOnly(r);
}
