#include "foodeveditor.h"

#include "../../resources/layers/foodevlayer.h"

#include "../fields/colorfield.h"
#include "../fields/intfield.h"
#include "../fields/stringfield.h"

FoodevEditor::FoodevEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    FoodevLayer *nl = dynamic_cast<FoodevLayer *>(l);

    // Set up fields widgets and add them to layout
    // col (color)
    ColorField *f_col = new ColorField("col", nl->col(), this);
    m_layout->addWidget(f_col);
    // nm (string)
    StringField *f_nm = new StringField("nm", nl->nm(), this);
    m_layout->addWidget(f_nm);
    // sort (short)
    IntField *f_sort = new IntField("sort", nl->sort(), this, -32768, 32767);
    m_layout->addWidget(f_sort);

    // Add connections for fields
    connect(f_col, &ColorField::fieldChanged, [nl, f_col, this](){nl->setCol(f_col->getValue()); this->onFieldChanged();});
    connect(f_nm, &StringField::fieldChanged, [nl, f_nm, this](){nl->setNm(f_nm->getValue()); this->onFieldChanged();});
    connect(f_sort, &IntField::fieldChanged, [nl, f_sort, this](){nl->setSort(f_sort->getValue()); this->onFieldChanged();});
}
