#include "rlinkeditor.h"

#include "../../resources/layers/rlinklayer.h"

#include "../fields/intfield.h"
#include "../fields/stringfield.h"

RlinkEditor::RlinkEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    RlinkLayer *nl = dynamic_cast<RlinkLayer *>(l);

    // Set up fields widgets and add them to layout
    // t (read only)
    IntField *f_t = new IntField("t", nl->t(), this);
    f_t->setReadOnly(true);
    m_layout->addWidget(f_t);

    if (nl->t() < 3) {
        IntField *f_id = new IntField("id", nl->id(), this);
        f_id->setReadOnly(true);
        m_layout->addWidget(f_id);
    } else {
        // id (short)
        IntField *f_id = new IntField("id", nl->id(), this, -32768, 32767);
        m_layout->addWidget(f_id);
        connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    }

    if (nl->t() == 0) {
        // meshnm (string)
        StringField *f_meshnm = new StringField("meshnm", nl->meshnm(), this);
        m_layout->addWidget(f_meshnm);
        connect(f_meshnm, &StringField::fieldChanged, [nl, f_meshnm, this](){nl->setMeshnm(f_meshnm->getValue()); this->onFieldChanged();});

        // meshver (unsigned short)
        IntField *f_meshver = new IntField("meshver", nl->meshver(), this, 0, 65535);
        m_layout->addWidget(f_meshver);
        connect(f_meshver, &IntField::fieldChanged, [nl, f_meshver, this](){nl->setMeshver(f_meshver->getValue()); this->onFieldChanged();});

        // meshid (short)
        IntField *f_meshid = new IntField("meshid", nl->meshid(), this, -32768, 32767);
        m_layout->addWidget(f_meshid);
        connect(f_meshid, &IntField::fieldChanged, [nl, f_meshid, this](){nl->setMeshid(f_meshid->getValue()); this->onFieldChanged();});

        // matnm (string)
        StringField *f_matnm = new StringField("matnm", nl->matnm(), this);
        m_layout->addWidget(f_matnm);
        connect(f_matnm, &StringField::fieldChanged, [nl, f_matnm, this](){nl->setMatnm(f_matnm->getValue()); this->onFieldChanged();});

        // matver (unsigned short)
        IntField *f_matver = new IntField("matver", nl->matver(), this, 0, 65535);
        m_layout->addWidget(f_matver);
        connect(f_matver, &IntField::fieldChanged, [nl, f_matver, this](){nl->setMathver(f_matver->getValue()); this->onFieldChanged();});

        // matid (short)
        IntField *f_matid = new IntField("matid", nl->matid(), this, -32768, 32767);
        m_layout->addWidget(f_matid);
        connect(f_matid, &IntField::fieldChanged, [nl, f_matid, this](){nl->setMathid(f_matid->getValue()); this->onFieldChanged();});
    } else if (nl->t() == 1 || nl->t() == 2) {
        // nm (string)
        StringField *f_nm = new StringField("nm", nl->nm(), this);
        m_layout->addWidget(f_nm);
        connect(f_nm, &StringField::fieldChanged, [nl, f_nm, this](){nl->setNm(f_nm->getValue()); this->onFieldChanged();});

        // ver (unsigned short)
        IntField *f_ver = new IntField("ver", nl->ver(), this, 0, 65535);
        m_layout->addWidget(f_ver);
        connect(f_ver, &IntField::fieldChanged, [nl, f_ver, this](){nl->setVer(f_ver->getValue()); this->onFieldChanged();});

        if (nl->t() == 2) {
            // matid (short)
            IntField *f_matid = new IntField("matid", nl->matid(), this, -32768, 32767);
            m_layout->addWidget(f_matid);
            connect(f_matid, &IntField::fieldChanged, [nl, f_matid, this](){nl->setMathid(f_matid->getValue()); this->onFieldChanged();});
        }
    }
}
