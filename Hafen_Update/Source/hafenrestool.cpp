#include "hafenrestool.h"
#include <QFileDialog>
#include <QInputDialog>
#include <QMessageBox>
#include <QStatusBar>
#include "widgets/helpers/remoteresourcedialog.h"

#include "resources/layers/unknownlayer.h"
#include "resources/layers/vbuf2layer.h"
#include "resources/layers/meshlayer.h"
#include "resources/layers/texlayer.h"
#include "resources/layers/mat2layer.h"
#include "widgets/layerwidget.h"
#include "tools/objexporter.h"
#include "tools/kritterexporter.h"
#include "widgets/editors/audioeditor.h"
#include <QUrl>
#include <QTextStream>
#include <QDesktopServices>
#include <QTcpServer>
#include <QProcess>
#include <QFileInfo>
#include <QDir>

static int extractTexIdFromMat2(const Mat2Layer* mat)
{
    if (!mat) return -1;
    const QMap<QString, VariantList> keyargs = mat->keyargs();

    auto getNumericFromList = [](const VariantList& list, bool fromEnd) -> int {
        if (fromEnd) {
            for (int i = list.size() - 1; i >= 0; --i) {
                const QVariant v = list.value(i);
                if (v.canConvert<int>())
                    return v.toInt();
            }
        } else {
            for (int i = 0; i < list.size(); ++i) {
                const QVariant v = list.value(i);
                if (v.canConvert<int>())
                    return v.toInt();
            }
        }
        return -1;
    };

    // Try exact keys first.
    if (keyargs.contains("tex")) {
        int id = getNumericFromList(keyargs.value("tex"), false);
        if (id >= 0) return id;
    }
    if (keyargs.contains("texlink")) {
        int id = getNumericFromList(keyargs.value("texlink"), true);
        if (id >= 0) return id;
    }

    // Case-insensitive fallback.
    for (auto it = keyargs.constBegin(); it != keyargs.constEnd(); ++it) {
        const QString key = it.key().toLower();
        if (key == "tex") {
            int id = getNumericFromList(it.value(), false);
            if (id >= 0) return id;
        } else if (key == "texlink") {
            int id = getNumericFromList(it.value(), true);
            if (id >= 0) return id;
        }
    }

    return -1;
}


#include "widgets/editors/actioneditor.h"
#include "widgets/editors/audio2editor.h"
#include "widgets/editors/clambeditor.h"
#include "widgets/editors/codeeditor.h"
#include "widgets/editors/codeentryeditor.h"
#include "widgets/editors/defaulteditor.h"
#include "widgets/editors/foodeveditor.h"
#include "widgets/editors/imageeditor.h"
#include "widgets/editors/mat2editor.h"
#include "widgets/editors/mesheditor.h"
#include "widgets/editors/negeditor.h"
#include "widgets/editors/paginaeditor.h"
#include "widgets/editors/rlinkeditor.h"
#include "widgets/editors/skaneditor.h"
#include "widgets/editors/skeleditor.h"
#include "widgets/editors/tileeditor.h"
#include "widgets/editors/tileseteditor.h"
#include "widgets/editors/tileset2editor.h"
#include "widgets/editors/texeditor.h"
#include "widgets/editors/tooltipeditor.h"
#include "widgets/editors/vbuf2editor.h"
#include "widgets/editors/srceditor.h"


HafenResTool::HafenResTool(QWidget *parent) :
    QMainWindow(parent)
{
    m_currentResource = nullptr;
    m_currentEditor = nullptr;
    m_progress = nullptr;
    m_dictionary = new ResourceDictionary(this);
    setupUi(this);

    connect(actionExport_Obj, &QAction::triggered, this, &HafenResTool::onExportObjClicked);
    connect(actionExport_Obj_With_Texture, &QAction::triggered, this, &HafenResTool::onExportObjWithTextureClicked);
    connect(actionView, &QAction::triggered, this, &HafenResTool::onViewObjClicked);
    connect(actionKritterGetter, &QAction::triggered, this, &HafenResTool::on_actionKritterGetter_triggered);


    connect(layersListWidget, SIGNAL(itemActivated(int)), SLOT(onResourceLayerSelected(int)));
    connect(layersListWidget, SIGNAL(resourceChanged()), SLOT(onResourceChanged()));

    connect(layersListWidget, &LayerWidget::onCurrentRowChanged, this, &HafenResTool::onLayerWidgetCurrentRowChanged);
}

// Helper function: converts raw vbuf Layer into Vbuf2Layer



// The slot itself
void HafenResTool::onExportObjClicked()
{
    // 1. Use the correct variable name from your header: m_currentResource
    Resource* res = m_currentResource;

    if (!res) return;

    Vbuf2Layer* vbuf = nullptr;
    QList<MeshLayer*> meshes;

    for (Layer* layer : res->layers()) {
        // Match by string name first
        if (layer->type() == "vbuf" || layer->type() == "vbuf2") {
            vbuf = dynamic_cast<Vbuf2Layer*>(layer);
        } else if (layer->type() == "mesh") {
            MeshLayer* m = dynamic_cast<MeshLayer*>(layer);
            if (m) meshes << m;
        }
    }

    // If the cast failed but we found a "vbuf" layer,
    // wrap the data manually into a temporary Vbuf2Layer object.
    if (!vbuf) {
        for (Layer* layer : res->layers()) {
            if (layer->type() == "vbuf") {
                vbuf = new Vbuf2Layer(layer->toByteArray(), res);
                vbuf->parse(); // Manually trigger the parse
                break;
            }
        }
    }
    if (!vbuf) {
        QMessageBox::warning(this, "Export Error", "No vbuf/vbuf2 layer found.");
        return;
    }

    if (vbuf->sublayers().isEmpty())
        vbuf->parse();

    if (!vbuf->sublayers().contains("pos")) {
        QMessageBox::warning(this, "Export Error", "VBUF has no position data; this resource format may be unsupported.");
        return;
    }

    if (meshes.isEmpty()) {
        QMessageBox::warning(this, "Export Error", "No mesh layers found.");
        return;
    }

    // 4. File Dialog
    QString defaultName = res->getName() + ".obj";
    QString fileName = QFileDialog::getSaveFileName(this, "Export OBJ", defaultName, "Wavefront OBJ (*.obj)");

    if (fileName.isEmpty()) return;

    // 5. Run Export
    ObjExporter exporter(vbuf);
    if (exporter.saveAs(fileName, meshes)) {
        // Accessing statusBar through the QMainWindow method
        this->statusBar()->showMessage(tr("Exported: %1").arg(fileName), 5000);
    } else {
        QString err = exporter.error().isEmpty() ? "Could not write the OBJ file." : exporter.error();
        QMessageBox::critical(this, "Export Failed", err);
    }
}

void HafenResTool::onExportObjWithTextureClicked()
{
    Resource* res = m_currentResource;
    if (!res) return;

    Vbuf2Layer* vbuf = nullptr;
    QList<MeshLayer*> meshes;
    TexLayer* tex = nullptr;
    QMap<int, TexLayer*> texById;
    QMap<int, Mat2Layer*> matById;

    for (Layer* layer : res->layers()) {
        if (layer->type() == "vbuf" || layer->type() == "vbuf2") {
            vbuf = dynamic_cast<Vbuf2Layer*>(layer);
        } else if (layer->type() == "mesh") {
            MeshLayer* m = dynamic_cast<MeshLayer*>(layer);
            if (m) meshes << m;
        } else if (layer->type() == "tex") {
            TexLayer* t = dynamic_cast<TexLayer*>(layer);
            if (t) {
                texById.insert(t->id(), t);
                if (!tex && !t->image().isEmpty())
                    tex = t;
            }
        } else if (layer->type() == "mat2") {
            Mat2Layer* m = dynamic_cast<Mat2Layer*>(layer);
            if (m)
                matById.insert(m->id(), m);
        }
    }

    if (!vbuf) {
        for (Layer* layer : res->layers()) {
            if (layer->type() == "vbuf") {
                vbuf = new Vbuf2Layer(layer->toByteArray(), res);
                vbuf->parse();
                break;
            }
        }
    }

    if (!vbuf) {
        QMessageBox::warning(this, "Export Error", "No vbuf/vbuf2 layer found.");
        return;
    }

    if (vbuf->sublayers().isEmpty())
        vbuf->parse();

    if (!vbuf->sublayers().contains("pos")) {
        QMessageBox::warning(this, "Export Error", "VBUF has no position data; this resource format may be unsupported.");
        return;
    }

    if (meshes.isEmpty()) {
        QMessageBox::warning(this, "Export Error", "No mesh layers found.");
        return;
    }

    // Pick texture based on the first mesh's material, fallback to first available.
    if (!meshes.isEmpty()) {
        int matId = meshes.first()->matid();
        if (matById.contains(matId)) {
            int texId = extractTexIdFromMat2(matById.value(matId));
            if (texId >= 0 && texById.contains(texId))
                tex = texById.value(texId);
        }
    }

    if (!tex || tex->image().isEmpty()) {
        QMessageBox::warning(this, "Export Warning",
                             "No texture found for this mesh. Exporting OBJ without texture.");
    }

    QString defaultName = res->getName() + ".obj";
    QString objPath = QFileDialog::getSaveFileName(this, "Export OBJ + Texture", defaultName, "Wavefront OBJ (*.obj)");
    if (objPath.isEmpty()) return;

    QFileInfo objInfo(objPath);
    QString baseName = objInfo.completeBaseName();
    QString dirPath = objInfo.absolutePath();
    QString mtlPath = dirPath + "/" + baseName + ".mtl";
    QString texFileName = baseName + ".png";
    QString texPath = dirPath + "/" + texFileName;

    ObjExporter exporter(vbuf);
    if (!exporter.saveAs(objPath, meshes, mtlPath, "material_0")) {
        QString err = exporter.error().isEmpty() ? "Could not write the OBJ file." : exporter.error();
        QMessageBox::critical(this, "Export Failed", err);
        return;
    }

    if (tex && !tex->image().isEmpty()) {
        QFile texFile(texPath);
        if (!texFile.open(QIODevice::WriteOnly)) {
            QMessageBox::warning(this, "Export Warning", "Failed to write texture image.");
        } else {
            texFile.write(tex->image());
            texFile.close();
        }

        QFile mtlFile(mtlPath);
        if (mtlFile.open(QIODevice::WriteOnly | QIODevice::Text)) {
            QTextStream mtl(&mtlFile);
            mtl << "newmtl material_0\n";
            mtl << "Ka 1.000 1.000 1.000\n";
            mtl << "Kd 1.000 1.000 1.000\n";
            mtl << "Ks 0.000 0.000 0.000\n";
            mtl << "d 1.0\n";
            mtl << "illum 1\n";
            mtl << "map_Kd " << texFileName << "\n";
            mtlFile.close();
        }
    }

    this->statusBar()->showMessage(tr("Exported: %1").arg(objPath), 5000);
}

void HafenResTool::onViewObjClicked()
{
    Resource* res = m_currentResource;
    if (!res) return;

    Vbuf2Layer* vbuf = nullptr;
    QList<MeshLayer*> meshes;
    TexLayer* tex = nullptr;
    QMap<int, TexLayer*> texById;
    QMap<int, Mat2Layer*> matById;

    for (Layer* layer : res->layers()) {
        if (layer->type() == "vbuf" || layer->type() == "vbuf2") {
            vbuf = dynamic_cast<Vbuf2Layer*>(layer);
        } else if (layer->type() == "mesh") {
            MeshLayer* m = dynamic_cast<MeshLayer*>(layer);
            if (m) meshes << m;
        } else if (layer->type() == "tex") {
            TexLayer* t = dynamic_cast<TexLayer*>(layer);
            if (t) {
                texById.insert(t->id(), t);
                if (!tex && !t->image().isEmpty())
                    tex = t;
            }
        } else if (layer->type() == "mat2") {
            Mat2Layer* m = dynamic_cast<Mat2Layer*>(layer);
            if (m)
                matById.insert(m->id(), m);
        }
    }

    if (!vbuf) {
        for (Layer* layer : res->layers()) {
            if (layer->type() == "vbuf") {
                vbuf = new Vbuf2Layer(layer->toByteArray(), res);
                vbuf->parse();
                break;
            }
        }
    }

    if (!vbuf) {
        QMessageBox::warning(this, "Preview Error", "No vbuf/vbuf2 layer found.");
        return;
    }

    if (vbuf->sublayers().isEmpty())
        vbuf->parse();

    if (!vbuf->sublayers().contains("pos")) {
        QMessageBox::warning(this, "Preview Error", "VBUF has no position data; this resource format may be unsupported.");
        return;
    }

    if (meshes.isEmpty()) {
        QMessageBox::warning(this, "Preview Error", "No mesh layers found.");
        return;
    }

    m_previewTemp.reset(new QTemporaryDir());
    if (!m_previewTemp->isValid()) {
        QMessageBox::critical(this, "Preview Error", "Failed to create temp folder.");
        return;
    }

    QString objPath = m_previewTemp->path() + "/preview.obj";
    ObjExporter exporter(vbuf);
    if (!exporter.saveAs(objPath, meshes)) {
        QString err = exporter.error().isEmpty() ? "Could not write the OBJ file." : exporter.error();
        QMessageBox::critical(this, "Preview Error", err);
        return;
    }
    if (!QFileInfo::exists(objPath)) {
        QMessageBox::critical(this, "Preview Error", "OBJ file was not created.");
        return;
    }

    // Pick texture based on the first mesh's material, fallback to first available.
    if (!meshes.isEmpty()) {
        int matId = meshes.first()->matid();
        if (matById.contains(matId)) {
            int texId = extractTexIdFromMat2(matById.value(matId));
            if (texId >= 0 && texById.contains(texId))
                tex = texById.value(texId);
        }
    }

    QString texFileName;
    if (tex && !tex->image().isEmpty()) {
        texFileName = "preview.png";
        QString texPath = m_previewTemp->path() + "/" + texFileName;
        QFile texFile(texPath);
        if (texFile.open(QIODevice::WriteOnly)) {
            texFile.write(tex->image());
            texFile.close();
        } else {
            texFileName.clear();
        }
    }

    QString htmlPath = m_previewTemp->path() + "/preview.html";
    QFile htmlFile(htmlPath);
    if (!htmlFile.open(QIODevice::WriteOnly | QIODevice::Text)) {
        QMessageBox::critical(this, "Preview Error", "Failed to write preview HTML.");
        return;
    }

    QTextStream html(&htmlFile);
    html << "<!doctype html>\n";
    html << "<html><head><meta charset=\"utf-8\"/>\n";
    html << "<style>html,body{margin:0;height:100%;overflow:hidden;background:#111;color:#ccc;font-family:sans-serif;}#c{width:100%;height:100%;display:block;}#status{position:fixed;left:12px;top:10px;font-size:12px;background:rgba(0,0,0,0.5);padding:6px 8px;border-radius:4px;}#refresh{position:fixed;left:12px;top:44px;font-size:12px;background:#2b2b2b;color:#ddd;border:1px solid #444;padding:6px 10px;border-radius:4px;cursor:pointer;}#refresh:hover{background:#333;}</style>\n";
    html << "</head><body>\n";
    html << "<canvas id=\"c\"></canvas>\n";
    html << "<div id=\"status\">Loading preview…</div>\n";
    html << "<button id=\"refresh\">Reload Texture</button>\n";
    html << "<script type=\"importmap\">\n";
    html << "{\n";
    html << "  \"imports\": {\n";
    html << "    \"three\": \"./three.module.js\"\n";
    html << "  }\n";
    html << "}\n";
    html << "</script>\n";
    html << "<script type=\"module\">\n";
    html << "import * as THREE from 'three';\n";
    html << "import { OBJLoader } from './OBJLoader.js';\n";
    html << "const statusEl=document.getElementById('status');\n";
    html << "const refreshBtn=document.getElementById('refresh');\n";
    html << "statusEl.textContent='Initializing…';\n";
    html << "window.addEventListener('error', (e)=>{statusEl.textContent='Error: '+e.message;});\n";
    html << "window.addEventListener('unhandledrejection', (e)=>{statusEl.textContent='Error: '+e.reason;});\n";
    html << "const canvas=document.getElementById('c');\n";
    html << "const renderer=new THREE.WebGLRenderer({canvas,antialias:true});\n";
    html << "const scene=new THREE.Scene();scene.background=new THREE.Color(0x111111);\n";
    html << "const camera=new THREE.PerspectiveCamera(45,1,0.1,1000);\n";
    html << "camera.position.set(3,2,3);\n";
    html << "const light=new THREE.DirectionalLight(0xffffff,1.0);light.position.set(3,5,2);scene.add(light);\n";
    html << "const amb=new THREE.AmbientLight(0x666666);scene.add(amb);\n";
    html << "const controls={yaw:0,pitch:0,dist:3};\n";
    html << "let isDown=false,lastX=0,lastY=0;\n";
    html << "canvas.addEventListener('mousedown',e=>{isDown=true;lastX=e.clientX;lastY=e.clientY;});\n";
    html << "window.addEventListener('mouseup',()=>{isDown=false;});\n";
    html << "window.addEventListener('mousemove',e=>{if(!isDown) return;controls.yaw+=(e.clientX-lastX)*0.005;controls.pitch+=(e.clientY-lastY)*0.005;controls.pitch=Math.max(-1.5,Math.min(1.5,controls.pitch));lastX=e.clientX;lastY=e.clientY;});\n";
    html << "window.addEventListener('wheel',e=>{const step=Math.min(0.2,Math.max(0.02,Math.abs(e.deltaY)*0.001));controls.dist=Math.max(0.5,Math.min(50,controls.dist+ (e.deltaY>0?step:-step)));});\n";
    html << "function resize(){const w=window.innerWidth,h=window.innerHeight;renderer.setSize(w,h,false);camera.aspect=w/h;camera.updateProjectionMatrix();}\n";
    html << "window.addEventListener('resize',resize);resize();\n";
    html << "const loader=new OBJLoader();\n";
    html << "const objUrl='preview.obj';\n";
    html << "const texUrl=" << (texFileName.isEmpty() ? "null" : ("'" + texFileName + "'")) << ";\n";
    html << "let current=null;\n";
    html << "let loading=false;\n";
    html << "function loadScene(){\n";
    html << "  if(loading) return;\n";
    html << "  loading=true;\n";
    html << "  statusEl.textContent='Loading preview…';\n";
    html << "  if(current){scene.remove(current);}\n";
    html << "  const bust='?ts='+Date.now();\n";
    html << "  const matPromise = new Promise(resolve=>{\n";
    html << "    if(texUrl){const tex=new THREE.TextureLoader().load(texUrl + bust,()=>{tex.flipY=true;const m=new THREE.MeshStandardMaterial({map:tex});m.side=THREE.DoubleSide;resolve(m);});}\n";
    html << "    else {const m=new THREE.MeshStandardMaterial({color:0xcccccc});m.side=THREE.DoubleSide;resolve(m);} \n";
    html << "  });\n";
    html << "  matPromise.then(mat=>{\n";
    html << "    loader.load(objUrl + bust,(obj)=>{\n";
    html << "      obj.traverse(c=>{if(c.isMesh){c.material=mat;c.geometry.computeVertexNormals();}});\n";
    html << "      const box=new THREE.Box3().setFromObject(obj);\n";
    html << "      const size=new THREE.Vector3();box.getSize(size);\n";
    html << "      const center=new THREE.Vector3();box.getCenter(center);\n";
    html << "      obj.position.sub(center);\n";
    html << "      const maxDim=Math.max(size.x,size.y,size.z) || 1;\n";
    html << "      const fov=THREE.MathUtils.degToRad(camera.fov);\n";
    html << "      let dist=(maxDim/2)/Math.tan(fov/2);\n";
    html << "      dist*=1.5;\n";
    html << "      controls.dist=dist;\n";
    html << "      current=obj;scene.add(obj);\n";
    html << "      statusEl.textContent='Preview loaded';\n";
    html << "      loading=false;\n";
    html << "    }, undefined, (err)=>{statusEl.textContent='OBJ load failed: '+err;loading=false;});\n";
    html << "  });\n";
    html << "}\n";
    html << "loadScene();\n";
    html << "refreshBtn.addEventListener('click', ()=>{loadScene();});\n";
    html << "function animate(){requestAnimationFrame(animate);const x=controls.dist*Math.cos(controls.pitch)*Math.cos(controls.yaw);const y=controls.dist*Math.sin(controls.pitch);const z=controls.dist*Math.cos(controls.pitch)*Math.sin(controls.yaw);camera.position.set(x,y,z);camera.lookAt(0,0,0);renderer.render(scene,camera);}animate();\n";
    html << "</script>\n";
    html << "</body></html>\n";
    htmlFile.close();

    QString base = m_previewTemp->path();
    QString threeSrc = "C:/Users/MikeM/Desktop/HafenResourceTool-master/HafenResourceTool-master/webpreview/three.module.js";
    QString objSrc = "C:/Users/MikeM/Desktop/HafenResourceTool-master/HafenResourceTool-master/webpreview/OBJLoader.js";
    if (!QFileInfo::exists(threeSrc) || !QFileInfo::exists(objSrc)) {
        QMessageBox::critical(this, "Preview Error", "Missing Three.js files in /webpreview. Please re-run setup.");
        return;
    }
    QFile::remove(base + "/three.module.js");
    QFile::remove(base + "/OBJLoader.js");
    if (!QFile::copy(threeSrc, base + "/three.module.js") || !QFile::copy(objSrc, base + "/OBJLoader.js")) {
        QMessageBox::critical(this, "Preview Error", "Failed to copy Three.js files into temp preview folder.");
        return;
    }

    if (m_previewServer) {
        m_previewServer->terminate();
        if (!m_previewServer->waitForFinished(1000))
            m_previewServer->kill();
    }

    QTcpServer portProbe;
    portProbe.listen(QHostAddress::LocalHost, 0);
    m_previewPort = portProbe.serverPort();
    portProbe.close();

    m_previewServer.reset(new QProcess(this));
    m_previewServer->setWorkingDirectory(base);
    m_previewServer->start("python", QStringList() << "-m" << "http.server"
                                                  << QString::number(m_previewPort)
                                                  << "--bind" << "127.0.0.1");
    if (!m_previewServer->waitForStarted(1500)) {
        m_previewServer->start("py", QStringList() << "-m" << "http.server"
                                                  << QString::number(m_previewPort)
                                                  << "--bind" << "127.0.0.1");
        if (!m_previewServer->waitForStarted(1500)) {
            QString err = m_previewServer->errorString();
            QMessageBox::critical(this, "Preview Error",
                                  "Failed to start local preview server. " + err);
            return;
        }
    }

    QDesktopServices::openUrl(QUrl(QString("http://127.0.0.1:%1/preview.html").arg(m_previewPort)));
}




void HafenResTool::closeEvent(QCloseEvent *event)
{
    if (m_previewServer) {
        m_previewServer->terminate();
        if (!m_previewServer->waitForFinished(1000))
            m_previewServer->kill();
    }
    event->ignore();
    on_actionQuit_triggered();
}

void HafenResTool::destroyCurrentEditor()
{
    if (m_currentEditor) {
        // Disconnect all signals from editor
        m_currentEditor->disconnect();
        delete m_currentEditor;
        m_currentEditor = nullptr;
    }
}



void HafenResTool::setNewResource(const QString &n)
{
    if (m_currentResource) {
        // Labels
        labelResourceName->setText(QString("Resource:"));
        labelResourceVersion->setText(QString("Version:"));
        actionSaveResource->setEnabled(false);
        // Disconnect signals
        m_currentResource->disconnect();
        delete m_currentResource;
        layersListWidget->setResource(nullptr);
        actionRemove_layer->setEnabled(false);
        actionRestore_layer->setEnabled(false);
        actionExport_layer_to_raw_format->setEnabled(true);
        actionReplace_layer_from_raw_format->setEnabled(false);
    }
    menuLayers->setEnabled(true);
    destroyCurrentEditor();

    m_currentResource = new Resource(n, this);
    connect(m_currentResource, SIGNAL(resourceInited()), SLOT(onResourceInited()));
    connect(m_currentResource, SIGNAL(resourceError(QString)), SLOT(onResourceError(QString)));
    connect(m_currentResource, SIGNAL(resourceStartDownload()), SLOT(onResourceStartDownload()));
    connect(m_currentResource, SIGNAL(resourceEndDownload()), SLOT(onResourceEndDownload()));
    m_currentResource->init();
}

void HafenResTool::saveResource()
{
    QString name = QFileDialog::getSaveFileName(this, "Save resource", "./saved", "Hafen resources (*.res)");
    if (!name.isEmpty() && m_currentResource) {
        QFile tmpFile(name);
        if (tmpFile.open(QIODevice::WriteOnly)) {
            QByteArray resData = m_currentResource->toByteArray();
            tmpFile.write(resData);
            tmpFile.close();

            if (m_currentResource->isModified()) {
                m_currentResource->makeUnmodified();
                layersListWidget->redrawList();
                labelResourceName->setText(QString("Resource: %1").arg(m_currentResource->getName()));
            }
        }
    }
}

void HafenResTool::onResourceChanged()
{
    if (m_currentResource && !m_currentResource->isModified()) {
        // TODO: change title
    }
    // Update layers list widget
    layersListWidget->redrawList();
}

void HafenResTool::onResourceError(const QString &e)
{
    QMessageBox::warning(this, windowTitle(),
                         QString("An error occured while working with resource.\n%1").arg(e));
}

void HafenResTool::onResourceInited()
{
    labelResourceName->setText(QString("Resource: %1").arg(m_currentResource->getName()));
    labelResourceVersion->setText(QString("Version: %1").arg(m_currentResource->getVersion()));
    layersListWidget->setResource(m_currentResource);
    actionSaveResource->setEnabled(true);
    destroyCurrentEditor();
    // Add to dictionary if remote
    if (!m_currentResource->isLocal())
        m_dictionary->addPath(m_currentResource->getName());
}

void HafenResTool::onResourceLayerSelected(int index)
{
    if (!m_currentResource || m_currentResource->layers().length() - 1 < index || index < 0)
        return;

    destroyCurrentEditor();
    Layer *l = m_currentResource->layers().at(index);

    // Create editor by given type
    QString type = l->type();

    UnknownLayer *ul = dynamic_cast<UnknownLayer *>(l);
    if (ul) {
        // Default editor for all unknown layers
        m_currentEditor = new DefaultEditor(l, this);
    } else {
        if (type == "action") {
            m_currentEditor = new ActionEditor(l, this);
        } else if (type == "audio2") {
            m_currentEditor = new Audio2Editor(l, this);
        } else if (type == "audio") {
            m_currentEditor = new AudioEditor(l, this);
        } else if (type == "clamb") {
            m_currentEditor = new ClambEditor(l, this);
        } else if (type == "code") {
            m_currentEditor = new CodeEditor(l, this);
        } else if (type == "codeentry") {
            m_currentEditor = new CodeentryEditor(l, this);
        } else if (type == "foodev") {
            m_currentEditor = new FoodevEditor(l, this);
        } else if (type == "image") {
            m_currentEditor = new ImageEditor(l, this);
        } else if (type == "mat2") {
            m_currentEditor = new Mat2Editor(l, this);
        } else if (type == "mesh") {
            m_currentEditor = new MeshEditor(l, this);
        } else if (type == "neg") {
            m_currentEditor = new NegEditor(l, this);
        } else if (type == "pagina") {
            m_currentEditor = new PaginaEditor(l, this);
        } else if (type == "rlink") {
            m_currentEditor = new RlinkEditor(l, this);
        } else if (type == "skan") {
            m_currentEditor = new SkanEditor(l, this);
        } else if (type == "skel") {
            m_currentEditor = new SkelEditor(l, this);
        } else if (type == "tile") {
            m_currentEditor = new TileEditor(l, this);
        } else if (type == "tileset") {
            m_currentEditor = new TilesetEditor(l, this);
        } else if (type == "tileset2") {
            m_currentEditor = new Tileset2Editor(l, this);
        } else if (type == "tex") {
            m_currentEditor = new TexEditor(l, this);
        } else if (type == "tooltip") {
            m_currentEditor = new TooltipEditor(l, this);
        } else if (type == "vbuf" || type == "vbuf2") {
            m_currentEditor = new Vbuf2Editor(l, this);
        } else if (type == "src") {
            m_currentEditor = new SrcEditor(l, this);
        } else {
            // If editor not implemented
            m_currentEditor = new DefaultEditor(l, this);
        }
    }

    if (!m_currentEditor)
        m_currentEditor = new DefaultEditor(l, this);

    // A bit of layout magic here
    editorLayoutHolder->insertWidget(0, m_currentEditor);
    editorLayoutHolder->setStretch(0, 0);
    editorLayoutHolder->setStretch(1, 1);
    connect(m_currentEditor, SIGNAL(resourceChanged()), SLOT(onResourceChanged()));
}

void HafenResTool::onResourceStartDownload()
{
    if (m_progress) return;
    m_progress = new QProgressDialog(this, Qt::CustomizeWindowHint | Qt::WindowTitleHint);
    m_progress->setCancelButton(nullptr);
    m_progress->setMinimum(0);
    m_progress->setMaximum(0);
    m_progress->setLabel(new QLabel("Downloading resource from server..."));
    m_progress->show();
}

void HafenResTool::onResourceEndDownload()
{
    if (m_progress) {
        delete m_progress;
        m_progress = nullptr;
    }
}

void HafenResTool::on_actionRemoteResource_triggered()
{
    RemoteResourceDialog rrd(m_dictionary->getList(), this);
    if (rrd.exec()) {
        QString name = rrd.getPath();
        if (!name.isEmpty())
            setNewResource(QString("http://game.havenandhearth.com/res/%1.res").arg(name));
    }
}

void HafenResTool::on_actionLocalResource_triggered()
{
    QString name = QFileDialog::getOpenFileName(this, "Local resource", QDir::currentPath(),
                                                "Hafen resources (*.res *.cached)");
    if (!name.isEmpty())
        setNewResource(name);
}

void HafenResTool::on_actionQuit_triggered()
{
    if (m_currentResource && m_currentResource->isModified()) {
        QMessageBox::StandardButton mbr = QMessageBox::question(this, "HafenResTool",
                              "Current resource has been modified.\nDo you want save changes before exit?",
                              QMessageBox::Yes | QMessageBox::No | QMessageBox::Cancel, QMessageBox::Yes);
        if (mbr == QMessageBox::Yes)
            saveResource();
        else if (mbr == QMessageBox::Cancel)
            return;
    }
    qApp->quit();
}

void HafenResTool::on_actionSaveResource_triggered()
{
    saveResource();
}

void HafenResTool::on_actionKritterGetter_triggered()
{
    const QString folder = QFileDialog::getExistingDirectory(this, "Select Kritter Folder", QDir::currentPath());
    if (folder.isEmpty())
        return;

    const QString baseName = QFileInfo(folder).fileName();
    const QString defaultPath = QDir(folder).filePath(baseName + ".gltf");
    const QString outPath = QFileDialog::getSaveFileName(this, "Export glTF", defaultPath, "glTF (*.gltf)");
    if (outPath.isEmpty())
        return;

    QString err;
    if (!KritterExporter::exportFolderToGltf(folder, outPath, &err)) {
        QMessageBox::critical(this, "Kritter Getter", err.isEmpty() ? "Failed to export glTF." : err);
        return;
    }
    QMessageBox::information(this, "Kritter Getter", "glTF exported successfully.");
}

void HafenResTool::onLayerWidgetCurrentRowChanged(int index)
{
    if (index > -1 && index < m_currentResource->layers().size()) {
        auto layer = m_currentResource->layers().at(index);
        actionRemove_layer->setEnabled(layer->isRemoved() ? false : true);
        actionRestore_layer->setEnabled(layer->isRemoved() ? true : false);
        actionExport_layer_to_raw_format->setEnabled(true);
        actionReplace_layer_from_raw_format->setEnabled(true);
    }
}

void HafenResTool::on_actionRemove_layer_triggered()
{
    auto index = layersListWidget->currentRow();
    if (index > -1 && index < m_currentResource->layers().size()) {
        auto layer = m_currentResource->layers().at(index);
        if (!layer->isRemoved()) {
            layer->setRemoved(true);
            onLayerWidgetCurrentRowChanged(index);
            layersListWidget->redrawList();
        }
    }
}

void HafenResTool::on_actionRestore_layer_triggered()
{
    auto index = layersListWidget->currentRow();
    if (index > -1 && index < m_currentResource->layers().size()) {
        auto layer = m_currentResource->layers().at(index);
        if (layer->isRemoved()) {
            layer->setRemoved(false);
            onLayerWidgetCurrentRowChanged(index);
            layersListWidget->redrawList();
        }
    }
}

void HafenResTool::on_actionExport_layer_to_raw_format_triggered()
{
    const QString name = QFileDialog::getSaveFileName(this, "Export layer", "./saved");
    if (name.isEmpty())
        return;

    auto index = layersListWidget->currentRow();
    auto raw = m_currentResource->exportLayer(index);
    if (raw.isEmpty()) {
        QMessageBox::warning(this, windowTitle(),
                             QString("Failed to convert current layer to raw format."));
        return;
    }

    QFile lf(name);
    if (!lf.open(QIODevice::WriteOnly)) {
        QMessageBox::warning(this, windowTitle(),
                             QString("Failed to create file."));
        return;
    }
    lf.write(raw);
    lf.close();
}

void HafenResTool::on_actionReplace_layer_from_raw_format_triggered()
{
    const QString name = QFileDialog::getOpenFileName(this, "Import layer", QDir::currentPath());
    if (name.isEmpty())
        return;

    QFile lf(name);
    if (!lf.open(QIODevice::ReadOnly)) {
        QMessageBox::warning(this, windowTitle(),
                             QString("Failed to open file."));
        return;
    }
    QByteArray layer = lf.readAll();
    auto index = layersListWidget->currentRow();
    if (!m_currentResource->replaceLayer(index, layer)) {
        QMessageBox::warning(this, windowTitle(),
                             QString("Layer is invalid or incompatible type"));
        return;
    }
    onResourceLayerSelected(index);
    layersListWidget->redrawList();
}

void HafenResTool::on_actionAdd_layer_from_raw_format_triggered()
{
    const QString name = QFileDialog::getOpenFileName(this, "Import layer", QDir::currentPath());
    if (name.isEmpty())
        return;

    QFile lf(name);
    if (!lf.open(QIODevice::ReadOnly)) {
        QMessageBox::warning(this, windowTitle(),
                             QString("Failed to open file."));
        return;
    }
    QByteArray layer = lf.readAll();
    m_currentResource->appendLayer(layer);
    layersListWidget->redrawList();
}



