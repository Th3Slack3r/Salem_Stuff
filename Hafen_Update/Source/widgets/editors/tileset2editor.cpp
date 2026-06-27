#include "tileset2editor.h"

#include "../../resources/layers/tileset2layer.h"

#include "../fields/flavobjfield.h"
#include "../fields/intfield.h"
#include "../fields/listfield.h"
#include "../fields/stringfield.h"
#include "../fields/variantlistfield.h"

Tileset2Editor::Tileset2Editor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    Tileset2Layer *nl = dynamic_cast<Tileset2Layer *>(l);

    // Set up fields widgets and add them to layout
    // pr
    int p = nl->p();

    if (!p) {
        // tn (string)
        StringField *f_tn = new StringField("tn", nl->tn(), this);
        m_layout->addWidget(f_tn);
        connect(f_tn, &StringField::fieldChanged, [nl, f_tn, this](){nl->setTn(f_tn->getValue()); this->onFieldChanged();});

        // ta (variantlist)
        QMap<QString, VariantList> map;
        map.insert("ta", nl->ta());
        VariantListField *f_ta = new VariantListField("ta", map, this);
        m_layout->addWidget(f_ta);
        connect(f_ta, &VariantListField::fieldChanged, [nl, f_ta, this](){nl->setTa(f_ta->getValue().value("ta")); this->onFieldChanged();});
    } else if (p == 1) {
        // flavprob (unsigned short)
        IntField *f_flavprob = new IntField("flavprob", nl->flavprob(), this,  0, 65535);
        m_layout->addWidget(f_flavprob);
        connect(f_flavprob, &IntField::fieldChanged, [nl, f_flavprob, this](){nl->setFlavprob(f_flavprob->getValue()); this->onFieldChanged();});

        // flavobjs (flavobjs list
        FlavobjField *f_flavobjs = new FlavobjField("flavobjs", nl->flavobjs(), this);
        m_layout->addWidget(f_flavobjs);
        connect(f_flavobjs, &FlavobjField::fieldChanged, [nl, f_flavobjs, this](){nl->setFlavobjs(f_flavobjs->getValue()); this->onFieldChanged();});
    } else if (p == 2) {
        ListField *f_tags = new ListField("tags", nl->tags(), this);
        m_layout->addWidget(f_tags);
        connect(f_tags, &ListField::fieldChanged, [nl, f_tags, this](){nl->setTags(f_tags->getValue()); this->onFieldChanged();});
    }
}
