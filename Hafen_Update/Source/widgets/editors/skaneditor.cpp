#include "skaneditor.h"

#include "../../resources/layers/skanlayer.h"

#include "../fields/intfield.h"
#include "../fields/floatfield.h"
#include "../fields/textfield.h"

#include "../../tools/skanexporter.h"

#include <QPushButton>
#include <QFileDialog>
#include <QMessageBox>

static QString buildSkanSummary(const SkanLayer *nl)
{
    QStringList lines;
    lines << QString("Bones: %1").arg(nl->bones().length());
    int boneIndex = 0;
    for (const SkanLayer::Bone &b : nl->bones()) {
        lines << QString("");
        lines << QString("Bone %1: %2").arg(boneIndex + 1).arg(b.name);
        lines << QString("Frames: %1").arg(b.frames.length());
        int previewCount = qMin(5, b.frames.length());
        for (int i = 0; i < previewCount; ++i) {
            const SkanLayer::Frame &f = b.frames.at(i);
            lines << QString("  [%1] tm=%2 trans=(%3,%4,%5) rang=%6 rax=(%7,%8,%9)")
                        .arg(i)
                        .arg(f.tm, 0, 'f', 4)
                        .arg(f.tx, 0, 'f', 4)
                        .arg(f.ty, 0, 'f', 4)
                        .arg(f.tz, 0, 'f', 4)
                        .arg(f.rang, 0, 'f', 4)
                        .arg(f.rax, 0, 'f', 4)
                        .arg(f.ray, 0, 'f', 4)
                        .arg(f.raz, 0, 'f', 4);
        }
        if (b.frames.length() > previewCount)
            lines << QString("  ... %1 more frames").arg(b.frames.length() - previewCount);
        boneIndex++;
    }
    return lines.join("\n");
}

SkanEditor::SkanEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    SkanLayer *nl = dynamic_cast<SkanLayer *>(l);

    QPushButton *exportBtn = new QPushButton("Export glTF", this);
    exportBtn->setToolTip("Export skan animation to glTF for Blender");
    m_layout->addWidget(exportBtn);
    connect(exportBtn, &QPushButton::clicked, [this, nl]() {
        QString path = QFileDialog::getSaveFileName(this, "Export glTF", "./saved", "glTF (*.gltf)");
        if (path.isEmpty())
            return;
        QString err;
        if (!SkanExporter::exportGltf(nl, path, &err)) {
            QMessageBox::critical(this, "Export Failed", err.isEmpty() ? "Failed to export glTF." : err);
            return;
        }
        QMessageBox::information(this, "Export", "glTF exported successfully.");
    });

    IntField *f_id = new IntField("id", nl->id(), this, 0, 65535);
    m_layout->addWidget(f_id);

    IntField *f_fl = new IntField("fl", nl->fl(), this, 0, 255);
    m_layout->addWidget(f_fl);

    IntField *f_mode = new IntField("mode", nl->mode(), this, 0, 3);
    m_layout->addWidget(f_mode);

    FloatField *f_len = new FloatField("len", nl->len(), this, -999999, 999999);
    f_len->setDecimals(4);
    f_len->setSingleStep(0.1);
    m_layout->addWidget(f_len);

    FloatField *f_nspeed = new FloatField("nspeed", nl->nspeed(), this, -999999, 999999);
    f_nspeed->setDecimals(4);
    f_nspeed->setSingleStep(0.1);
    m_layout->addWidget(f_nspeed);

    TextField *f_summary = new TextField("frames", buildSkanSummary(nl), this);
    f_summary->setReadOnly(true);
    m_layout->addWidget(f_summary);

    connect(f_id, &IntField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    connect(f_fl, &IntField::fieldChanged, [nl, f_fl, this](){nl->setFl(f_fl->getValue()); this->onFieldChanged();});
    connect(f_mode, &IntField::fieldChanged, [nl, f_mode, this](){nl->setMode(f_mode->getValue()); this->onFieldChanged();});
    connect(f_len, &FloatField::fieldChanged, [nl, f_len, this](){nl->setLen((float)f_len->getValue()); this->onFieldChanged();});
    connect(f_nspeed, &FloatField::fieldChanged, [nl, f_nspeed, this](){nl->setNspeed((float)f_nspeed->getValue()); this->onFieldChanged();});
}
