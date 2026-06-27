#ifndef VBUF2EDITOR_H
#define VBUF2EDITOR_H

#include "../editor.h"
#include "../fields/meshchecklistfield.h"
#include <QTextStream>
#include <QScopedPointer>
#include <QTemporaryDir>
#include <QProcess>

class Vbuf2Editor : public Editor
{
    Q_OBJECT

public:
    Vbuf2Editor(Layer* l, QWidget* parent = nullptr);
    ~Vbuf2Editor();

private slots:
    void vawefrontClicked();
    void threeClicked();
    void viewModelClicked();
    void importFromObjClicked();

private:
    MeshChecklistField* f_meshes;
    QString m_resName;
    QScopedPointer<QTemporaryDir> m_previewTemp;
    QScopedPointer<QProcess> m_previewServer;
    int m_previewPort = -1;

    bool exportObj();
    bool exportThree();
    void openPreview(const QList<MeshLayer*>& meshes);
};

#endif // VBUF2EDITOR_H
