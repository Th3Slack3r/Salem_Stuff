#ifndef MESHCHECKLISTFIELD_H
#define MESHCHECKLISTFIELD_H

#include "../../resources/layers/meshlayer.h"
#include "../field.h"
#include "ui_meshchecklistfield.h"

class MeshChecklistField : public Field, private Ui::MeshChecklistField
{
    Q_OBJECT

public:
    explicit MeshChecklistField(const QString& title, const QList<MeshLayer*>& meshes, QWidget* parent = nullptr);
    const QList<MeshLayer*> checkedMeshes() const;
    const QList<MeshLayer*> meshes() const;

signals:
    void checkChanged();

private slots:
    void on_treeWidget_itemChanged(QTreeWidgetItem* item, int column);

private:
    QList<MeshLayer*> m_mesehs;

    void redrawList();
};

#endif // MESHCHECKLISTFIELD_H
