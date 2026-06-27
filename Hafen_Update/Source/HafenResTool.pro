#-------------------------------------------------
#
# Project created by QtCreator 2017-03-06T15:29:37
#
#-------------------------------------------------

QT       += core gui network sql xml multimedia

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = HafenResTool
TEMPLATE = app


SOURCES += main.cpp\
        hafenrestool.cpp \
    resources/layers/srclayer.cpp \
    resources/resource.cpp \
    resources/streams/readstream.cpp \
    resources/streams/writestream.cpp \
    resources/layers/imagelayer.cpp \
    resources/layers/unknownlayer.cpp \
    tools/objimporter.cpp \
    widgets/editors/srceditor.cpp \
    widgets/layerwidget.cpp \
    resources/layers/texlayer.cpp \
    widgets/editor.cpp \
    widgets/field.cpp \
    widgets/fields/intfield.cpp \
    widgets/fields/pointfield.cpp \
    widgets/fields/booleanfield.cpp \
    widgets/fields/pixmapfield.cpp \
    widgets/editors/imageeditor.cpp \
    widgets/editors/defaulteditor.cpp \
    widgets/fields/defaultfield.cpp \
    resources/layers/paginalayer.cpp \
    widgets/fields/textfield.cpp \
    widgets/editors/paginaeditor.cpp \
    resources/layers/tooltiplayer.cpp \
    widgets/editors/tooltipeditor.cpp \
    resources/layers/actionlayer.cpp \
    widgets/fields/stringfield.cpp \
    widgets/editors/actioneditor.cpp \
    widgets/fields/listfield.cpp \
    widgets/fields/dropdownfield.cpp \
    widgets/editors/texeditor.cpp \
    widgets/fields/labelfield.cpp \
    resources/layers/neglayer.cpp \
    widgets/editors/negeditor.cpp \
    resources/layers/mat2layer.cpp \
    resources/types/variantlist.cpp \
    widgets/editors/mat2editor.cpp \
    widgets/fields/variantlistfield.cpp \
    widgets/helpers/pointdialog.cpp \
    tools/resourcedictionary.cpp \
    widgets/helpers/remoteresourcedialog.cpp \
    resources/layers/meshlayer.cpp \
    widgets/editors/mesheditor.cpp \
    resources/layers/codelayer.cpp \
    tools/decompiler.cpp \
    widgets/editors/codeeditor.cpp \
    resources/layers/codeentrylayer.cpp \
    widgets/fields/mapfield.cpp \
    widgets/editors/codeentryeditor.cpp \
    resources/layers/rlinklayer.cpp \
    widgets/editors/rlinkeditor.cpp \
    resources/layers/skanlayer.cpp \
    resources/layers/skellayer.cpp \
    resources/types/point3f.cpp \
    widgets/fields/bonelistfield.cpp \
    widgets/editors/skaneditor.cpp \
    widgets/editors/skeleditor.cpp \
    widgets/helpers/point3fdialog.cpp \
    resources/layers/tileset2layer.cpp \
    resources/layers/tilesetlayer.cpp \
    resources/layers/tilelayer.cpp \
    widgets/fields/flavobjfield.cpp \
    widgets/editors/tileset2editor.cpp \
    widgets/editors/tileseteditor.cpp \
    widgets/editors/tileeditor.cpp \
    resources/layers/foodevlayer.cpp \
    widgets/fields/colorfield.cpp \
    widgets/editors/foodeveditor.cpp \
    resources/layers/audio2layer.cpp \
    resources/layers/audiolayer.cpp \
    resources/layers/vbuf2layer.cpp \
    widgets/editors/vbuf2editor.cpp \
    widgets/fields/meshchecklistfield.cpp \
    tools/objexporter.cpp \
    tools/transformer.cpp \
    tools/xmlimporter.cpp \
    widgets/editors/audio2editor.cpp \
    widgets/editors/audioeditor.cpp \
    widgets/fields/floatfield.cpp \
    resources/layers/clamblayer.cpp \
    widgets/fields/pairlistfield.cpp \
    widgets/editors/clambeditor.cpp \
    tools/threeexporter.cpp \
    tools/skanexporter.cpp \
    tools/kritterexporter.cpp

HEADERS  += hafenrestool.h \
    resources/layers/srclayer.h \
    resources/resource.h \
    resources/streams/readstream.h \
    resources/layers/layer.h \
    resources/streams/writestream.h \
    resources/layers/imagelayer.h \
    resources/layers/unknownlayer.h \
    tools/objimporter.h \
    widgets/editors/srceditor.h \
    widgets/layerwidget.h \
    resources/layers/texlayer.h \
    widgets/editor.h \
    widgets/field.h \
    widgets/fields/intfield.h \
    widgets/fields/pointfield.h \
    widgets/fields/booleanfield.h \
    widgets/fields/pixmapfield.h \
    widgets/editors/imageeditor.h \
    widgets/editors/defaulteditor.h \
    widgets/fields/defaultfield.h \
    resources/layers/paginalayer.h \
    widgets/fields/textfield.h \
    widgets/editors/paginaeditor.h \
    resources/layers/tooltiplayer.h \
    widgets/editors/tooltipeditor.h \
    resources/layers/actionlayer.h \
    widgets/fields/stringfield.h \
    widgets/editors/actioneditor.h \
    widgets/fields/listfield.h \
    widgets/fields/dropdownfield.h \
    widgets/editors/texeditor.h \
    widgets/fields/labelfield.h \
    resources/layers/neglayer.h \
    widgets/editors/negeditor.h \
    resources/layers/mat2layer.h \
    resources/types/variantlist.h \
    widgets/editors/mat2editor.h \
    widgets/fields/variantlistfield.h \
    widgets/helpers/pointdialog.h \
    tools/resourcedictionary.h \
    widgets/helpers/remoteresourcedialog.h \
    resources/layers/meshlayer.h \
    widgets/editors/mesheditor.h \
    resources/layers/codelayer.h \
    tools/decompiler.h \
    widgets/editors/codeeditor.h \
    resources/layers/codeentrylayer.h \
    widgets/fields/mapfield.h \
    widgets/editors/codeentryeditor.h \
    resources/layers/rlinklayer.h \
    widgets/editors/rlinkeditor.h \
    resources/layers/skanlayer.h \
    resources/layers/skellayer.h \
    resources/types/point3f.h \
    widgets/fields/bonelistfield.h \
    widgets/editors/skaneditor.h \
    widgets/editors/skeleditor.h \
    widgets/helpers/point3fdialog.h \
    resources/layers/tileset2layer.h \
    resources/layers/tilesetlayer.h \
    resources/layers/tilelayer.h \
    widgets/fields/flavobjfield.h \
    widgets/editors/tileset2editor.h \
    widgets/editors/tileseteditor.h \
    widgets/editors/tileeditor.h \
    resources/layers/foodevlayer.h \
    widgets/fields/colorfield.h \
    widgets/editors/foodeveditor.h \
    resources/layers/audio2layer.h \
    resources/layers/audiolayer.h \
    resources/layers/vbuf2layer.h \
    widgets/editors/vbuf2editor.h \
    widgets/fields/meshchecklistfield.h \
    tools/objexporter.h \
    tools/transformer.h \
    tools/xmlimporter.h \
    widgets/editors/audio2editor.h \
    widgets/editors/audioeditor.h \
    widgets/fields/floatfield.h \
    resources/layers/clamblayer.h \
    widgets/fields/pairlistfield.h \
    widgets/editors/clambeditor.h \
    tools/threeexporter.h \
    tools/skanexporter.h \
    tools/kritterexporter.h

FORMS    += hafenrestool.ui \
    widgets/layerwidget.ui \
    widgets/fields/intfield.ui \
    widgets/fields/pointfield.ui \
    widgets/fields/booleanfield.ui \
    widgets/fields/pixmapfield.ui \
    widgets/fields/defaultfield.ui \
    widgets/fields/textfield.ui \
    widgets/fields/stringfield.ui \
    widgets/fields/listfield.ui \
    widgets/fields/dropdownfield.ui \
    widgets/fields/labelfield.ui \
    widgets/fields/variantlistfield.ui \
    widgets/helpers/pointdialog.ui \
    widgets/helpers/remoteresourcedialog.ui \
    widgets/fields/mapfield.ui \
    widgets/fields/bonelistfield.ui \
    widgets/helpers/point3fdialog.ui \
    widgets/fields/flavobjfield.ui \
    widgets/fields/colorfield.ui \
    widgets/fields/meshchecklistfield.ui \
    widgets/fields/floatfield.ui \
    widgets/fields/pairlistfield.ui

RC_FILE += mr.rc

RESOURCES += \
    appres.qrc
