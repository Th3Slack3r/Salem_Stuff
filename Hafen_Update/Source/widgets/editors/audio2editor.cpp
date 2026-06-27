#include "audio2editor.h"
#include <QHBoxLayout>
#include <QPushButton>
#include <QDir>
#include <QFileDialog>
#include <QMessageBox>
#include <QMediaPlayer>
#include <QAudioOutput>
#include <QUrl>

#include "../../resources/layers/audio2layer.h"

#include "../fields/floatfield.h"
#include "../fields/intfield.h"
#include "../fields/stringfield.h"

Audio2Editor::Audio2Editor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    Audio2Layer *nl = dynamic_cast<Audio2Layer *>(l);

    // Set up fields widgets and add them to layout
    // ver (unsigned char)
    IntField *f_ver = new IntField("ver", nl->ver(), this, 0, 255);
    f_ver->setReadOnly(true);
    m_layout->addWidget(f_ver);
    // id (string)
    StringField *f_id = new StringField("id", nl->id(), this);
    m_layout->addWidget(f_id);
    // bvol (float)
    FloatField *f_bvol = new FloatField("bvol", nl->bvol(), this, 0, 1);
    f_bvol->setSingleStep(0.05);
    m_layout->addWidget(f_bvol);
    if (nl->ver() != 2)
        f_bvol->setReadOnly(true);

    // Temp layout
    QHBoxLayout *buttonLayout = new QHBoxLayout(this);
    // Export sound
    QPushButton *exportSound = new QPushButton("Export audio", this);
    exportSound->setToolTip("Export current audio to file");
    connect(exportSound, SIGNAL(clicked(bool)), SLOT(exportClicked()));
    buttonLayout->addWidget(exportSound);
    // Import sound
    QPushButton *importSound = new QPushButton("Import audio", this);
    importSound->setToolTip("Import audio from file");
    connect(importSound, SIGNAL(clicked(bool)), SLOT(importClicked()));
    buttonLayout->addWidget(importSound);
    // Play
    QPushButton *playSound = new QPushButton("Play", this);
    playSound->setToolTip("Play audio");
    connect(playSound, SIGNAL(clicked(bool)), SLOT(playClicked()));
    buttonLayout->addWidget(playSound);
    // Stop
    QPushButton *stopSound = new QPushButton("Stop", this);
    stopSound->setToolTip("Stop audio");
    connect(stopSound, SIGNAL(clicked(bool)), SLOT(stopClicked()));
    buttonLayout->addWidget(stopSound);

    // Layout tricks
    buttonLayout->insertStretch(-1, 1);
    m_layout->addLayout(buttonLayout);

    // Add connections for fields
    connect(f_id, &StringField::fieldChanged, [nl, f_id, this](){nl->setId(f_id->getValue()); this->onFieldChanged();});
    connect(f_bvol, &FloatField::fieldChanged, [nl, f_bvol, this](){nl->setBvol(f_bvol->getValue()); this->onFieldChanged();});
}

Audio2Editor::~Audio2Editor()
{
    if (m_player)
        m_player->stop();
}

QString Audio2Editor::ensureTempOgg()
{
    Audio2Layer *nl = dynamic_cast<Audio2Layer *>(m_layer);
    if (!nl)
        return QString();
    if (nl->audio().isEmpty())
        return QString();

    if (!m_tempDir)
        m_tempDir.reset(new QTemporaryDir());
    if (!m_tempDir->isValid())
        return QString();

    QString path = m_tempDir->path() + "/preview.ogg";
    QFile f(path);
    if (!f.open(QIODevice::WriteOnly))
        return QString();
    f.write(nl->audio());
    f.close();
    return path;
}

void Audio2Editor::exportClicked()
{
    QString sf = QFileDialog::getSaveFileName(this, "Export audio", "./saved", "Vorbis OGG (*.ogg)");
    if (sf.isEmpty())
        return;

    QFile of(sf);
    if (!of.open(QIODevice::WriteOnly)) {
        QMessageBox::critical(this, "Error", "Failed to save file");
        return;
    }
    Audio2Layer *nl = dynamic_cast<Audio2Layer *>(m_layer);
    of.write(nl->audio());
    of.close();
    QMessageBox::information(this, "Exported", "Audio successfully exported");
}

void Audio2Editor::importClicked()
{
    QString sf = QFileDialog::getOpenFileName(this, "Import audio", QDir::currentPath(), "Vorbis OGG (*.ogg)");
    if (sf.isEmpty())
        return;

    QFile of(sf);
    if (!of.open(QIODevice::ReadOnly)) {
        QMessageBox::critical(this, "Error", "Failed to load file");
        return;
    }
    Audio2Layer *nl = dynamic_cast<Audio2Layer *>(m_layer);
    nl->setAudio(of.readAll());
    of.close();
    onFieldChanged();
}

void Audio2Editor::playClicked()
{
    Audio2Layer *nl = dynamic_cast<Audio2Layer *>(m_layer);
    if (!nl || nl->audio().isEmpty()) {
        QMessageBox::warning(this, "Play", "No audio data to play.");
        return;
    }

    QString path = ensureTempOgg();
    if (path.isEmpty()) {
        QMessageBox::critical(this, "Play", "Failed to prepare audio for playback.");
        return;
    }

    if (!m_player) {
        m_player.reset(new QMediaPlayer(this));
        m_audioOut.reset(new QAudioOutput(this));
        m_player->setAudioOutput(m_audioOut.data());
    }

    m_player->setSource(QUrl::fromLocalFile(path));
    m_player->play();
}

void Audio2Editor::stopClicked()
{
    if (m_player)
        m_player->stop();
}
