#include "editor.h"

Editor::Editor(Layer *l, QWidget *parent) : QWidget(parent)
{
    m_layout = new QVBoxLayout(this);
    m_layer = l;
    setLayout(m_layout);
}

void Editor::onFieldChanged()
{
    m_layer->setEdited(true);
    if (!m_layer->isRemoved())
        emit resourceChanged();
}
