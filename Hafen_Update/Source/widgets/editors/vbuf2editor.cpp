#include "vbuf2editor.h"
#include <QDir>
#include <QFileDialog>
#include <QHBoxLayout>
#include <QMessageBox>
#include <QPixmap>
#include <QPushButton>
#include <QDesktopServices>
#include <QTemporaryDir>
#include <QTcpServer>
#include <QProcess>
#include <QFileInfo>

#include "../../resources/layers/meshlayer.h"
#include "../../resources/layers/texlayer.h"
#include "../../resources/layers/vbuf2layer.h"

#include "../fields/listfield.h"

#include "../../tools/objexporter.h"
#include "../../tools/objimporter.h"
#include "../../tools/threeexporter.h"

Vbuf2Editor::Vbuf2Editor(Layer* l, QWidget* parent)
    : Editor(l, parent)
{
    // Cast layer
    Vbuf2Layer* nl = dynamic_cast<Vbuf2Layer*>(l);
    m_resName = nl->resName();
    m_resName.replace("/", "_");

    // Set up fields widgets and add them to layout
    // sublayers
    QList<QString> sublayers = nl->sublayers();
    QList<QString> display;
    foreach(const QString& s, sublayers) display << QString("%1 (dots: %2; dimension: %3; vertexes/points: %4)")
                                                        .arg(s)
                                                        .arg(nl->sublayer(s).size())
                                                        .arg(nl->dimension(s))
                                                        .arg(nl->sublayer(s).size() / nl->dimension(s));
    ListField* f_sublayers = new ListField("sublayers", display, this);
    f_sublayers->setReadOnly(true);
    m_layout->addWidget(f_sublayers);

    // meshes
    QList<MeshLayer*> meshes = nl->meshes();
    f_meshes = new MeshChecklistField("meshes", meshes, this);
    m_layout->addWidget(f_meshes);
    // connect(f_meshes, SIGNAL(checkChanged()), this, SLOT(onMeshesCheckChanges()));

    // Temp layout
    QHBoxLayout* buttonLayout = new QHBoxLayout(this);
    // Export Vawefront
    QPushButton* vawefrontButton = new QPushButton("Export to obj", this);
    vawefrontButton->setToolTip("Export to *.obj file");
    connect(vawefrontButton, SIGNAL(clicked(bool)), SLOT(vawefrontClicked()));
    buttonLayout->addWidget(vawefrontButton);
    // Export to Three.js
    QPushButton* threeButton = new QPushButton("Export to three.js", this);
    threeButton->setToolTip("Export to three.js supported format");
    connect(threeButton, SIGNAL(clicked(bool)), SLOT(threeClicked()));
    buttonLayout->addWidget(threeButton);
    // View
    QPushButton* viewBtn = new QPushButton("View Model", this);
    viewBtn->setToolTip("Open preview for selected meshes");
    connect(viewBtn, SIGNAL(clicked(bool)), SLOT(viewModelClicked()));
    buttonLayout->addWidget(viewBtn);
    // Import
    QPushButton* importObj = new QPushButton("Import from OBJ", this);
    importObj->setToolTip("Import model data from OBJ file");
    connect(importObj, SIGNAL(clicked(bool)), SLOT(importFromObjClicked()));
    buttonLayout->addWidget(importObj);

    // Layout tricks
    buttonLayout->insertStretch(-1, 1);
    m_layout->addLayout(buttonLayout);
}

Vbuf2Editor::~Vbuf2Editor()
{
    if (m_previewServer) {
        m_previewServer->terminate();
        if (!m_previewServer->waitForFinished(1000))
            m_previewServer->kill();
    }
}

void Vbuf2Editor::vawefrontClicked()
{
    bool b = exportObj();
    if (b)
        QMessageBox::information(
            this, "Success", QString("Model has been exported to file '%1'").arg("/obj/" + m_resName + ".obj"));
    else
        QMessageBox::warning(this, "Error", "Failed to save file");
}

void Vbuf2Editor::threeClicked()
{
    bool b = exportThree();
    if (b)
        QMessageBox::information(
            this, "Success", QString("Model has been exported to file '%1'").arg("/obj/" + m_resName + ".json"));
    else
        QMessageBox::warning(this, "Error", "Failed to save file");
}

void Vbuf2Editor::viewModelClicked()
{
    QList<MeshLayer*> meshes = f_meshes->checkedMeshes();
    if (meshes.isEmpty()) {
        QMessageBox::warning(this, "Preview Error", "Select at least one mesh to preview.");
        return;
    }
    openPreview(meshes);
}

void Vbuf2Editor::importFromObjClicked()
{
    Vbuf2Layer* nl = dynamic_cast<Vbuf2Layer*>(m_layer);
    QList<QString> sl = nl->sublayers();
    if (sl.length() != 3 || !sl.contains("pos") || !sl.contains("nrm") || !sl.contains("tex"))
    {
        QMessageBox::warning(
            this, "Warning", "Cannot import data from OBJ\nOnly 3 sublayers supported for now (nrm, pos, tex)");
        return;
    }
    if (nl->meshes().length() > 1)
    {
        QMessageBox::warning(this, "Warning", "Cannot import data from OBJ\nOnly models with 1 mesh supported for now");
        return;
    }
    QString f = QFileDialog::getOpenFileName(this, "Import model", QDir::currentPath(), "Obj files (*.obj)");
    if (f.isEmpty())
        return;

    ObjImporter imp;
    if (!imp.import(f))
    {
        qDebug() << imp.getError() << imp.pos().size() << imp.nrm().size() << imp.tex().size();
        return;
    }

    nl->setSublayerList("pos", imp.pos());
    nl->setSublayerList("nrm", imp.nrm());
    nl->setSublayerList("tex", imp.tex());

    MeshLayer* ml = nl->meshes().at(0);
    ml->setInd(imp.faces());
    onFieldChanged();
}

bool Vbuf2Editor::exportObj()
{
    QList<MeshLayer*> m = f_meshes->checkedMeshes();
    if (!m.length())
        return false;

    ObjExporter exporter(dynamic_cast<Vbuf2Layer*>(m_layer));
    QDir dir(QDir::currentPath() + "/obj");
    if (!dir.exists())
        dir.mkpath(".");
    QString defaultPath = dir.absoluteFilePath(m_resName + ".obj");
    QString path = QFileDialog::getSaveFileName(this, "Export OBJ", defaultPath, "Wavefront OBJ (*.obj)");
    if (path.isEmpty())
        return false;
    return exporter.saveAs(path, m);
}

bool Vbuf2Editor::exportThree()
{
    QList<MeshLayer*> m = f_meshes->checkedMeshes();
    if (!m.length())
        return false;

    ThreeExporter exporter(dynamic_cast<Vbuf2Layer*>(m_layer));
    QDir dir(QDir::currentPath() + "/obj");
    if (!dir.exists())
        dir.mkpath(".");
    QString defaultPath = dir.absoluteFilePath(m_resName + ".json");
    QString path = QFileDialog::getSaveFileName(this, "Export Three.js", defaultPath, "JSON (*.json)");
    if (path.isEmpty())
        return false;
    return exporter.saveAs(m_resName.split('_').last(), path, m);
}

void Vbuf2Editor::openPreview(const QList<MeshLayer*>& meshes)
{
    Vbuf2Layer* vbuf = dynamic_cast<Vbuf2Layer*>(m_layer);
    if (!vbuf) return;

    if (vbuf->sublayers().isEmpty())
        vbuf->parse();

    if (!vbuf->sublayers().contains("pos")) {
        QMessageBox::warning(this, "Preview Error", "VBUF has no position data.");
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
        QMessageBox::critical(this, "Preview Error", "Failed to export OBJ for preview.");
        return;
    }

    // Use first available texture, if any.
    TexLayer* tex = nullptr;
    for (TexLayer* t : vbuf->textures()) {
        if (t && !t->image().isEmpty()) {
            tex = t;
            break;
        }
    }

    QString texFileName;
    if (tex && !tex->image().isEmpty()) {
        texFileName = "preview.png";
        QFile texFile(m_previewTemp->path() + "/" + texFileName);
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
    html << "<script type=\"importmap\">{\"imports\":{\"three\":\"./three.module.js\"}}</script>\n";
    html << "<script type=\"module\">\n";
    html << "import * as THREE from 'three';\n";
    html << "import { OBJLoader } from './OBJLoader.js';\n";
    html << "const statusEl=document.getElementById('status');\n";
    html << "const refreshBtn=document.getElementById('refresh');\n";
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
    html << "let current=null;let loading=false;\n";
    html << "function loadScene(){if(loading) return;loading=true;statusEl.textContent='Loading preview…';if(current){scene.remove(current);}const bust='?ts='+Date.now();const matPromise=new Promise(resolve=>{if(texUrl){const tex=new THREE.TextureLoader().load(texUrl + bust,()=>{tex.flipY=true;const m=new THREE.MeshStandardMaterial({map:tex});m.side=THREE.DoubleSide;resolve(m);});}else{const m=new THREE.MeshStandardMaterial({color:0xcccccc});m.side=THREE.DoubleSide;resolve(m);}});matPromise.then(mat=>{loader.load(objUrl + bust,(obj)=>{obj.traverse(c=>{if(c.isMesh){c.material=mat;c.geometry.computeVertexNormals();}});const box=new THREE.Box3().setFromObject(obj);const size=new THREE.Vector3();box.getSize(size);const center=new THREE.Vector3();box.getCenter(center);obj.position.sub(center);const maxDim=Math.max(size.x,size.y,size.z)||1;const fov=THREE.MathUtils.degToRad(camera.fov);let dist=(maxDim/2)/Math.tan(fov/2);dist*=1.5;controls.dist=dist;current=obj;scene.add(obj);statusEl.textContent='Preview loaded';loading=false;}, undefined, (err)=>{statusEl.textContent='OBJ load failed: '+err;loading=false;});});}\n";
    html << "loadScene();refreshBtn.addEventListener('click', ()=>{loadScene();});\n";
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
