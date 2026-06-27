#ifndef EDITOR_H
#define EDITOR_H

#include <QWidget>
#include <QVBoxLayout>

#include "resources/layers/layer.h"

class Editor : public QWidget
{
    Q_OBJECT
public:
    explicit Editor(Layer *l, QWidget *parent = 0);

protected:
    QVBoxLayout *m_layout;
    Layer *m_layer;

signals:
    void resourceChanged();

public slots:

protected slots:
    void onFieldChanged();
};

#endif // EDITOR_H
