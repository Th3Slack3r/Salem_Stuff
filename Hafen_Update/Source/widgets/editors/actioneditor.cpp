#include "actioneditor.h"

#include "../../resources/layers/actionlayer.h"

#include "../fields/intfield.h"
#include "../fields/listfield.h"
#include "../fields/stringfield.h"

ActionEditor::ActionEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    ActionLayer *nl = dynamic_cast<ActionLayer *>(l);

    // Set up fields widgets and add them to layout
    // pr
    StringField *f_pr = new StringField("pr", nl->pr(), this);
    m_layout->addWidget(f_pr);
    // name
    StringField *f_name = new StringField("name", nl->name(), this);
    m_layout->addWidget(f_name);
    // pver (short)
    IntField *f_pver = new IntField("pver", nl->pver(), this, -32768, 32767);
    m_layout->addWidget(f_pver);
    // hk (char)
    IntField *f_hk = new IntField("hk", nl->hk(), this, -128, 127);
    m_layout->addWidget(f_hk);
    // ad
    ListField *f_ad = new ListField("ad", nl->ad(), this);
    m_layout->addWidget(f_ad);

    // Add connections for fields
    connect(f_pr, &StringField::fieldChanged, [nl, f_pr, this](){nl->setPr(f_pr->getValue()); this->onFieldChanged();});
    connect(f_name, &StringField::fieldChanged, [nl, f_name, this](){nl->setName(f_name->getValue()); this->onFieldChanged();});
    connect(f_pver, &IntField::fieldChanged, [nl, f_pver, this](){nl->setPver(f_pver->getValue()); this->onFieldChanged();});
    connect(f_hk, &IntField::fieldChanged, [nl, f_hk, this](){nl->setHk(f_hk->getValue()); this->onFieldChanged();});
    connect(f_ad, &ListField::fieldChanged, [nl, f_ad, this](){nl->setAd(f_ad->getValue()); this->onFieldChanged();});
}
