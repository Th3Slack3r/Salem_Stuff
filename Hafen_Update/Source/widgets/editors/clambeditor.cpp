#include "clambeditor.h"

#include "../../resources/layers/clamblayer.h"

#include "../fields/floatfield.h"
#include "../fields/intfield.h"
#include "../fields/pairlistfield.h"
#include "../fields/stringfield.h"

ClambEditor::ClambEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    ClambLayer *nl = dynamic_cast<ClambLayer *>(l);

    // m_ver
    IntField *f_ver = new IntField("ver", nl->ver(), this);
    m_layout->addWidget(f_ver);
    f_ver->setReadOnly(true);

    // pnm and pid
    QString s = nl->pnm();
    if (nl->ver() == 2 && s.length()) {
        // pnm (string)
        StringField *f_pnm = new StringField("pnm", s, this);
        m_layout->addWidget(f_pnm);
        // pid (unsigned short)
        IntField *f_pid = new IntField("pid", nl->pid(), this, 0, 65535);
        m_layout->addWidget(f_pid);
        // Signals for them
        connect(f_pnm, &StringField::fieldChanged, [nl, f_pnm, this](){nl->setPnm(f_pnm->getValue()); this->onFieldChanged();});
        connect(f_pid, &IntField::fieldChanged, [nl, f_pid, this](){nl->setPid(f_pid->getValue()); this->onFieldChanged();});
    }

    // minc (unsigned char)
    IntField *f_minc = new IntField("minc", nl->minc(), this, 0, 255);
    m_layout->addWidget(f_minc);
    // maxc (unsigned char)
    IntField *f_maxc = new IntField("maxc", nl->maxc(), this, 0, 255);
    m_layout->addWidget(f_maxc);
    // maxi (unsigned short)
    IntField *f_maxi = new IntField("maxi", nl->maxi(), this, 0, 65535);
    m_layout->addWidget(f_maxi);
    // bvol (float)
    FloatField *f_bvol = new FloatField("bvol", nl->bvol(), this, 0, 1.0);
    f_bvol->setSingleStep(0.05);
    m_layout->addWidget(f_bvol);

    // cnms & ieps (pair<string, double>
    QList<QPair<QString, QString> > tl;
    for (int i = 0; i < nl->cnms().length(); ++i)
        tl << QPair<QString, QString>(nl->cnms().at(i), QString("%1").arg(nl->ieps().at(i)));
    PairlistField *f_pairs = new PairlistField("cnms/ieps", tl,
                                               PairlistField::String, PairlistField::Float, this);
    m_layout->addWidget(f_pairs);
    connect(f_pairs, &PairlistField::fieldChanged, [nl, f_pairs, this](){
        QList<QPair<QString, QString> > rl = f_pairs->getValue();
        QList<QString> sl;
        QList<double> dl;
        for (int i = 0; i < rl.length(); ++i) {
            sl << rl.at(i).first;
            dl << rl.at(i).second.toDouble();
        }
        nl->setCnms(sl);
        nl->setIeps(dl);
        this->onFieldChanged();
    });

    // Add connections for fields
    connect(f_minc, &IntField::fieldChanged, [nl, f_minc, this](){nl->setMinc(f_minc->getValue()); this->onFieldChanged();});
    connect(f_maxc, &IntField::fieldChanged, [nl, f_maxc, this](){nl->setMaxc(f_maxc->getValue()); this->onFieldChanged();});
    connect(f_maxi, &IntField::fieldChanged, [nl, f_maxi, this](){nl->setMaxi(f_maxi->getValue()); this->onFieldChanged();});
    connect(f_bvol, &FloatField::fieldChanged, [nl, f_bvol, this](){nl->setBvol(f_bvol->getValue()); this->onFieldChanged();});
}
