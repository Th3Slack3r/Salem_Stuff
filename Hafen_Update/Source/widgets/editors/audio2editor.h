#ifndef AUDIO2EDITOR_H
#define AUDIO2EDITOR_H

#include "../editor.h"
#include <QScopedPointer>
#include <QTemporaryDir>

QT_BEGIN_NAMESPACE
class QMediaPlayer;
class QAudioOutput;
QT_END_NAMESPACE

class Audio2Editor : public Editor
{
    Q_OBJECT

public:
    Audio2Editor(Layer *l, QWidget *parent = 0);
    ~Audio2Editor();

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

#endif // AUDIO2EDITOR_H
