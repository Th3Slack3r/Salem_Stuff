#ifndef PAIRLISTFIELD_H
#define PAIRLISTFIELD_H

#include "ui_pairlistfield.h"
#include "../field.h"

class PairlistField : public Field, private Ui::PairlistField
{
    Q_OBJECT

public:
    enum PairTypes {
        Int = 0,
        Float,
        String
    };

    explicit PairlistField(const QString &title,
                           const QList<QPair<QString, QString> > &value,
                           PairTypes typeOne, PairTypes typeTwo, QWidget *parent = 0);

    const QList<QPair<QString, QString> > getValue() const {return m_value;}
    void setValue(const QList<QPair<QString, QString> > &v) {m_value = v;}

private slots:
    void on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column);

private:
    QList<QPair<QString, QString> > m_value;
    PairTypes m_t1;
    PairTypes m_t2;

    void redrawList();
    const QString editValie(const QString &v, PairTypes t);
    int itemIndex(QTreeWidgetItem *item);
};

#endif // PAIRLISTFIELD_H
