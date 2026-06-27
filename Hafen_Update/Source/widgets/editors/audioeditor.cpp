#include "audioeditor.h"
#include <QHBoxLayout>
#include <QPushButton>
#include <QDir>
#include <QFileDialog>
#include <QMessageBox>
#include <QMediaPlayer>
#include <QAudioOutput>
#include <QUrl>

#include "../../resources/layers/audiolayer.h"

AudioEditor::AudioEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Temp layout
    QHBoxLayout *buttonLayout = new QHBoxLayout(this);
    // Export audio
    QPushButton *exportSound = new QPushButton("Export audio", this);
    exportSound->setToolTip("Export current audio to file");
    connect(exportSound, SIGNAL(clicked(bool)), SLOT(exportClicked()));
    buttonLayout->addWidget(exportSound);
    // Import audio
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

    buttonLayout->insertStretch(-1, 1);
    m_layout->addLayout(buttonLayout);
}

AudioEditor::~AudioEditor()
{
    if (m_player)
        m_player->stop();
}

QString AudioEditor::ensureTempOgg()
{
    AudioLayer *nl = dynamic_cast<AudioLayer *>(m_layer);
    if (!nl || nl->audio().isEmpty())
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

void AudioEditor::exportClicked()
{
    QString sf = QFileDialog::getSaveFileName(this, "Export audio", "./saved", "Vorbis OGG (*.ogg)");
    if (sf.isEmpty())
        return;

    QFile of(sf);
    if (!of.open(QIODevice::WriteOnly)) {
        QMessageBox::critical(this, "Error", "Failed to save file");
        return;
    }
    AudioLayer *nl = dynamic_cast<AudioLayer *>(m_layer);
    of.write(nl->audio());
    of.close();
    QMessageBox::information(this, "Exported", "Audio successfully exported");
}

void AudioEditor::importClicked()
{
    QString sf = QFileDialog::getOpenFileName(this, "Import audio", QDir::currentPath(), "Vorbis OGG (*.ogg)");
    if (sf.isEmpty())
        return;

    QFile of(sf);
    if (!of.open(QIODevice::ReadOnly)) {
        QMessageBox::critical(this, "Error", "Failed to load file");
        return;
    }
    AudioLayer *nl = dynamic_cast<AudioLayer *>(m_layer);
    nl->setAudio(of.readAll());
    of.close();
    onFieldChanged();
}

void AudioEditor::playClicked()
{
    AudioLayer *nl = dynamic_cast<AudioLayer *>(m_layer);
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

void AudioEditor::stopClicked()
{
    if (m_player)
        m_player->stop();
}
