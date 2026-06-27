#ifndef VARIANTLISTFIELD_H
#define VARIANTLISTFIELD_H

#include "ui_variantlistfield.h"
#include "../field.h"
#include "../../resources/types/variantlist.h"
#include <QMap>

class VariantListField : public Field, private Ui::VariantListField
{
    Q_OBJECT

public:
    explicit VariantListField(const QString &title,
                              const QMap<QString, VariantList> &value, QWidget *parent = 0);

    const QMap<QString, VariantList> getValue() const;
    void setValue(const QMap<QString, VariantList> &value);

private slots:
    void on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column);
    void on_treeWidget_itemClicked(QTreeWidgetItem *item, int column);
    void on_pushRemoveNode_clicked();

private:
    QMap<QString, VariantList> m_value;

    void redrawList();
    int getInt(int value = 0, int min = -2147483647, int max = -2147483647);
    double getDouble(double value = 0, double min = -2147483647, double max = 2147483647);
    const QString getString(const QString &value);
    const QString getTypeFromList(const QString &defaultValue, const QStringList &values);
    const QColor getColor(const QColor &value);
    const QPoint getPoint(const QPoint &value);
};

#endif // VARIANTLISTFIELD_H
