#ifndef MAPFIELD_H
#define MAPFIELD_H

#include "ui_mapfield.h"
#include "../field.h"

class MapField : public Field, private Ui::MapField
{
    Q_OBJECT

public:
    enum MapType {
        Int = 0,
        Float,
        String
    };

    explicit MapField(const QString &title, const QMap<QString, QString> &value,
                      MapType mt = MapType::String, QWidget *parent = 0);

    const QMap<QString, QString> getValue() const {return m_value;}
    void setValue(const QMap<QString, QString> &v);

private slots:
    void on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column);

private:
    QMap<QString, QString> m_value;
    MapType m_mt;

    void redrawList();
};

#endif // MAPFIELD_H
