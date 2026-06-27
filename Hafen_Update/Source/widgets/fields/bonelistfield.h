#ifndef BONELISTFIELD_H
#define BONELISTFIELD_H

#include "ui_bonelistfield.h"
#include "../field.h"
#include "../../resources/layers/skellayer.h"

class BoneListField : public Field, private Ui::BoneListField
{
    Q_OBJECT

public:
    explicit BoneListField(const QString &title, const QList<QPair<QString, SkelLayer::Bone>> &value, QWidget *parent = nullptr);

    const QList<QPair<QString, SkelLayer::Bone>> getValue() const {return m_bones;}

private slots:
    void on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column);

private:
    QList<QPair<QString, SkelLayer::Bone>> m_bones;

    void redrawList();
    bool editPoint3f(const QString &parent, const QString &type);
    bool editParent(const QString &parent, const QString &current);
    bool editRang(const QString &parent);

    bool hasParent(const QString& parent) const;
};

#endif // BONELISTFIELD_H
