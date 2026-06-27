#include "tileseteditor.h"

#include "../../resources/layers/tilesetlayer.h"

#include "../fields/flavobjfield.h"
#include "../fields/intfield.h"

TilesetEditor::TilesetEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    TilesetLayer *nl = dynamic_cast<TilesetLayer *>(l);

    IntField *f_fl = new IntField("fl", nl->fl(), this, 0, 255);
    m_layout->addWidget(f_fl);

    IntField *f_flavprob = new IntField("flavprob", nl->flavprob(), this, 0, 65535);
    m_layout->addWidget(f_flavprob);

    FlavobjField *f_flavobjs = new FlavobjField("flavobjs", nl->flavobjs(), this);
    m_layout->addWidget(f_flavobjs);

    connect(f_fl, &IntField::fieldChanged, [nl, f_fl, this](){nl->setFl(f_fl->getValue()); this->onFieldChanged();});
    connect(f_flavprob, &IntField::fieldChanged, [nl, f_flavprob, this](){nl->setFlavprob(f_flavprob->getValue()); this->onFieldChanged();});
    connect(f_flavobjs, &FlavobjField::fieldChanged, [nl, f_flavobjs, this](){nl->setFlavobjs(f_flavobjs->getValue()); this->onFieldChanged();});
}
