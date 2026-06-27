#include "mesheditor.h"

#include "../../resources/layers/meshlayer.h"

#include "../fields/intfield.h"
#include "../fields/labelfield.h"
#include "../fields/listfield.h"

MeshEditor::MeshEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    MeshLayer *nl = dynamic_cast<MeshLayer *>(l);

    // Set up fields widgets and add them to layout
    // label
    LabelField *f_label1 = new LabelField("<b>Note:</b> field 'fl' is read only", this);
    m_layout->addWidget(f_label1);
    // fl
    IntField *f_fl = new IntField("fl", nl->fl(), this);
    f_fl->setReadOnly(true);
    m_layout->addWidget(f_fl);
    // matid
    IntField *f_matid = new IntField("matid", nl->matid(), this, -32768, 32767);
    m_layout->addWidget(f_matid);

    if (nl->id() > -1) {
        IntField *f_id = new IntField("id", nl->id(), this, -32768, 32767);
        m_layout->addWidget(f_id);
        connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    }

    if (nl->ref() > -1) {
        IntField *f_ref = new IntField("ref", nl->ref(), this, -32768, 32767);
        m_layout->addWidget(f_ref);
        connect(f_ref, &IntField::fieldChanged, [nl, f_ref, this](){nl->setRef(f_ref->getValue()); this->onFieldChanged();});
    }

    if (nl->rdat().length()) {
        ListField *f_rdat = new ListField("rdat", nl->rdat(), this);
        m_layout->addWidget(f_rdat);
        connect(f_rdat, &ListField::fieldChanged, [nl, f_rdat, this](){nl->setRdat(f_rdat->getValue()); this->onFieldChanged();});
    }

    LabelField *f_label2 = new LabelField(QString("Faces count: %1").arg(nl->ind().length()), this);
    m_layout->addWidget(f_label2);

    // Add connections for fields
    connect(f_matid, &IntField::fieldChanged, [nl, f_matid, this](){nl->setMatid(f_matid->getValue()); this->onFieldChanged();});
}
