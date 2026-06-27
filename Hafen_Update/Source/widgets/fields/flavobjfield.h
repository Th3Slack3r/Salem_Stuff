#ifndef FLAVOBJFIELD_H
#define FLAVOBJFIELD_H

#include "ui_flavobjfield.h"
#include "../field.h"
#include "../../resources/layers/tileset2layer.h"

class FlavobjField : public Field, private Ui::FlavobjField
{
    Q_OBJECT

public:
    explicit FlavobjField(const QString &title, const QList<Tileset2Layer::Flavobj> &value, QWidget *parent = 0);

    const QList<Tileset2Layer::Flavobj> getValue() const {return m_value;}

private slots:
    void on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column);

private:
    QList<Tileset2Layer::Flavobj> m_value;

    void redrawList();
};

#endif // FLAVOBJFIELD_H
