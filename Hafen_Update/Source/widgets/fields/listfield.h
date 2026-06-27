#ifndef LISTFIELD_H
#define LISTFIELD_H

#include "ui_listfield.h"
#include "../field.h"

class ListField : public Field, private Ui::ListField
{
    Q_OBJECT

public:
    explicit ListField(const QString &title, const QList<QString> &value, QWidget *parent = 0);

    const QList<QString> getValue() const;
    void setValue(const QList<QString> &v);
    void setReadOnly(bool ro);

private slots:
    void on_listWidget_clicked(const QModelIndex &index);
    void on_pushRemove_clicked();
    void on_pushAdd_clicked();
    void on_pushUp_clicked();
    void on_pushDown_clicked();

private:
    QList<QString> m_value;

    void redrawList();
    void moveItem(int row, int direction);
};

#endif // LISTFIELD_H
