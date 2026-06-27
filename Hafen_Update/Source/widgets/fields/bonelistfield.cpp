#include "bonelistfield.h"
#include "../helpers/point3fdialog.h"
#include <QInputDialog>

BoneListField::BoneListField(const QString &title, const QList<QPair<QString, SkelLayer::Bone> >& value, QWidget *parent)
    : Field(title, parent)
    , m_bones(value)
{
    setupUi(this);
    label->setText(QString("%1:").arg(title));
    redrawList();
}

void BoneListField::redrawList()
{
    treeWidget->clear();

    for (const auto& pair : qAsConst(m_bones)) {
        SkelLayer::Bone b = pair.second;
        QTreeWidgetItem *bi = new QTreeWidgetItem(QStringList() << pair.first << QString());
        treeWidget->addTopLevelItem(bi);
        bi->addChild(new QTreeWidgetItem(QStringList() << "pos" << b.pos.toString()));
        bi->addChild(new QTreeWidgetItem(QStringList() << "rax" << b.rax.toString()));
        bi->addChild(new QTreeWidgetItem(QStringList() << "rang" << QString("%1").arg(b.rang)));
        bi->addChild(new QTreeWidgetItem(QStringList() << "parent" << b.bp));
    }
}

bool BoneListField::editPoint3f(const QString &parent, const QString &type)
{
    if (parent.isEmpty() || type.isEmpty() || !hasParent(parent))
        return false;

    auto it = std::find_if(m_bones.begin(), m_bones.end(), [&parent](const QPair<QString, SkelLayer::Bone>& p) -> bool
    {
        return p.first == parent;
    });
    const SkelLayer::Bone& b = (*it).second;
    Point3F p;
    if (type == "pos")
        p = b.pos;
    else if (type == "rax")
        p = b.rax;
    else
        return false;

    Point3fDialog p3fd(p, this);
    if (!p3fd.exec())
        return false;

    p = p3fd.getValue();
    if (type == "pos")
        (*it).second.pos = p;
    else if (type == "rax")
        (*it).second.rax = p;
    return true;
}

bool BoneListField::editParent(const QString &parent, const QString &current)
{
    if (parent.isEmpty() || current.isEmpty() || !hasParent(parent))
        return false;

    QList<QString> k;
    for (const auto& pair : qAsConst(m_bones))
        k << pair.first;
    k.removeOne(parent);
    k.prepend(QString());
    int index = k.indexOf(current);
    bool ok;
    QString result = QInputDialog::getItem(this, "New value", "Parent:", k, index, false, &ok);
    if (!ok || result == current)
        return false;

    auto it = std::find_if(m_bones.begin(), m_bones.end(), [&parent](const QPair<QString, SkelLayer::Bone>& p) -> bool
    {
        return p.first == parent;
    });
    if (it == m_bones.end())
        return false;

    (*it).second.bp = result;
    return true;
}

bool BoneListField::editRang(const QString &parent)
{
    if (parent.isEmpty() || !hasParent(parent))
        return false;

    auto it = std::find_if(m_bones.begin(), m_bones.end(), [&parent](const QPair<QString, SkelLayer::Bone>& p) -> bool
    {
        return p.first == parent;
    });
    float rang = (*it).second.rang;
    bool ok;
    float nr = (float)QInputDialog::getDouble(this, "New value", "Rang:", (double)rang, -9999, 9999, 6, &ok);
    if (!ok)
        return false;

    (*it).second.rang = nr;
    return true;
}

bool BoneListField::hasParent(const QString& parent) const
{
    auto it = std::find_if(m_bones.begin(), m_bones.end(), [&parent](const QPair<QString, SkelLayer::Bone>& p) -> bool
    {
        return  p.first == parent;
    });
    return it != m_bones.end();
}

void BoneListField::on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column)
{
    Q_UNUSED(column)
    if (!item->parent())
        return;

    QString parentName = item->parent()->text(0);
    QString itemName = item->text(0);

    bool r = false;
    if (itemName == "pos" || itemName == "rax")
        r = editPoint3f(parentName, itemName);
    else if (itemName == "parent")
        r = editParent(parentName, itemName);
    else if (itemName == "rang")
        r = editRang(parentName);
    else
        return;

    if (r) {
        onFieldChanged();
        redrawList();
    }
}
