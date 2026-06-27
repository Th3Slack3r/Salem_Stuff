#include "field.h"

Field::Field(const QString &title, QWidget *parent) : QWidget(parent)
{
    m_title = title;
    m_readOnly = false;
}

void Field::setReadOnly(bool ro)
{
    m_readOnly = ro;
}

bool Field::getReadOnly() const
{
    return m_readOnly;
}

void Field::onFieldChanged()
{
    if (!m_readOnly)
        emit fieldChanged();
}
