#include "variantlistfield.h"
#include <QInputDialog>
#include <QColorDialog>
#include <QDebug>
#include "../helpers/pointdialog.h"

VariantListField::VariantListField(const QString &title,
                                   const QMap<QString, VariantList> &value, QWidget *parent) :
    Field(title, parent)
{
    m_value = value;
    setupUi(this);
    label->setText(QString("%1:").arg(title));

    redrawList();
}

const QMap<QString, VariantList> VariantListField::getValue() const
{
    return m_value;
}

void VariantListField::setValue(const QMap<QString, VariantList> &value)
{
    m_value = value;
}

void VariantListField::redrawList()
{
    treeWidget->clear();

    QMap<QString, VariantList>::iterator it;
    for (it = m_value.begin(); it != m_value.end(); ++it) {
        // Top level item
        VariantList list = it.value();
        QString col2 = list.length() ? "" : "Empty";
        QTreeWidgetItem *tli = new QTreeWidgetItem(treeWidget, QStringList() << it.key() << col2);

        // Child items if any
        if (list.size()) {
            for (int i = 0; i < list.size(); ++i)
                tli->addChild(new QTreeWidgetItem(
                                  QStringList() << list.typeString(i) << list.valueToString(i)));
        }
    }
}

int VariantListField::getInt(int value, int min, int max)
{
    bool ok = false;
    int result = QInputDialog::getInt(this, "New value", "Value:", value, min, max, 1, &ok);
    if (ok)
        return result;
    return value;
}

double VariantListField::getDouble(double value, double min, double max)
{
    bool ok = false;
    double result = QInputDialog::getDouble(this, "New value", "Value:", value, min, max, 1, &ok);
    if (ok)
        return result;
    return value;
}

const QString VariantListField::getString(const QString &value)
{
    bool ok = false;
    QString result = QInputDialog::getText(this, "New value", "Value:", QLineEdit::Normal, value, &ok);
    if (ok)
        return result;
    return value;
}

const QString VariantListField::getTypeFromList(const QString &defaultValue, const QStringList &values)
{
    if (values.isEmpty() || !values.contains(defaultValue))
        return defaultValue;

    bool ok = false;
    QString result = QInputDialog::getItem(this, "New value", "Value:", values, values.indexOf(defaultValue), false, &ok);
    if (ok)
        return result;
    return defaultValue;
}

const QColor VariantListField::getColor(const QColor &value)
{
    QColor result = QColorDialog::getColor(value, this, "New value", QColorDialog::ShowAlphaChannel);
    if (result.isValid())
        return result;
    return value;
}

const QPoint VariantListField::getPoint(const QPoint &value)
{
    PointDialog pd(value, this);
    int result = pd.exec();
    if (result)
        return pd.getCoord();
    return value;
}

void VariantListField::on_treeWidget_itemDoubleClicked(QTreeWidgetItem *item, int column)
{
    Q_UNUSED(column)
    QTreeWidgetItem *parent = item->parent();
    if (!parent)
        return;

    QString type = item->text(0);
    QString parentType = parent->text(0);
    if (type == "Undefined")
        return;

    int index = parent->indexOfChild(item);
    QVariant oldValue = m_value.value(parentType).value(index);
    QVariant newValue;
    if (type == "Int")
        newValue = QVariant(getInt(oldValue.toInt()));
    else if (type == "Char")
        newValue = QVariant(getInt(oldValue.toInt(), -128, 127));
    else if (type == "Uchar")
        newValue = QVariant(getInt(oldValue.toInt(), 0, 255));
    else if (type == "Short")
        newValue = QVariant(getInt(oldValue.toInt(), -32768, 32767));
    else if (type == "Ushort")
        newValue = QVariant(getInt(oldValue.toInt(), 0, 65535));
    else if (type == "Float")
        newValue = QVariant((float)getDouble(oldValue.toDouble()));
    else if (type == "Double")
        newValue = QVariant(getDouble(oldValue.toDouble()));
    else if (type == "String") {
        if (parentType == "light") {
            QStringList l = QStringList() << "def" << "pv" << "pp" << "n";
            newValue = QVariant(getTypeFromList(item->text(1), l));
        } else if (parentType == "order") {
            QStringList l = QStringList() << "first" << "last" << "pfx" << "eye" << "earlyeye" << "premap" << "postmap";
            newValue = QVariant(getTypeFromList(item->text(1), l));
        } else {
            newValue = QVariant(getString(item->text(1)));
        }
    }
    else if (type == "Color")
        newValue = QVariant(getColor(qvariant_cast<QColor>(oldValue)));
    else if (type == "Coord")
        newValue = QVariant(getPoint(oldValue.toPoint()));

    if (oldValue != newValue) {
        bool updated = m_value[parentType].update(index, newValue);
        if (updated) {
            redrawList();
            onFieldChanged();
        }
    }
}

void VariantListField::on_treeWidget_itemClicked(QTreeWidgetItem *item, int column)
{
    Q_UNUSED(column)
    if (!item->parent()) {
        pushRemoveNode->setEnabled(true);
    } else {
        pushRemoveNode->setEnabled(false);
    }
}

void VariantListField::on_pushRemoveNode_clicked()
{
    QTreeWidgetItem *item = treeWidget->currentItem();
    if (item && !item->parent()) {
        m_value.remove(item->text(0).trimmed());
        redrawList();
        onFieldChanged();
        pushRemoveNode->setEnabled(false);
    }
}
