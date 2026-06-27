#ifndef AUDIOEDITOR_H
#define AUDIOEDITOR_H

#include "../editor.h"
#include <QScopedPointer>
#include <QTemporaryDir>

QT_BEGIN_NAMESPACE
class QMediaPlayer;
class QAudioOutput;
QT_END_NAMESPACE

class AudioEditor : public Editor
{
    Q_OBJECT

public:
    AudioEditor(Layer *l, QWidget *parent = 0);
    ~AudioEditor();

private slots:
    void exportClicked();
    void importClicked();
    void playClicked();
    void stopClicked();

private:
    QScopedPointer<QTemporaryDir> m_tempDir;
    QScopedPointer<QMediaPlayer> m_player;
    QScopedPointer<QAudioOutput> m_audioOut;
    QString ensureTempOgg();
};

#endif // AUDIOEDITOR_H
