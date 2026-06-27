/****************************************************************************
** Meta object code from reading C++ file 'hafenrestool.h'
**
** Created by: The Qt Meta Object Compiler version 69 (Qt 6.10.1)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "../../../hafenrestool.h"
#include <QtGui/qtextcursor.h>
#include <QtNetwork/QSslError>
#include <QtCore/qmetatype.h>

#include <QtCore/qtmochelpers.h>

#include <memory>


#include <QtCore/qxptype_traits.h>
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'hafenrestool.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 69
#error "This file was generated using the moc from 6.10.1. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

#ifndef Q_CONSTINIT
#define Q_CONSTINIT
#endif

QT_WARNING_PUSH
QT_WARNING_DISABLE_DEPRECATED
QT_WARNING_DISABLE_GCC("-Wuseless-cast")
namespace {
struct qt_meta_tag_ZN12HafenResToolE_t {};
} // unnamed namespace

template <> constexpr inline auto HafenResTool::qt_create_metaobjectdata<qt_meta_tag_ZN12HafenResToolE_t>()
{
    namespace QMC = QtMocConstants;
    QtMocHelpers::StringRefStorage qt_stringData {
        "HafenResTool",
        "onResourceChanged",
        "",
        "onResourceError",
        "e",
        "onResourceInited",
        "onResourceLayerSelected",
        "index",
        "onResourceStartDownload",
        "onResourceEndDownload",
        "on_actionRemoteResource_triggered",
        "on_actionLocalResource_triggered",
        "on_actionQuit_triggered",
        "on_actionSaveResource_triggered",
        "onExportObjClicked",
        "onExportObjWithTextureClicked",
        "onViewObjClicked",
        "on_actionKritterGetter_triggered",
        "onLayerWidgetCurrentRowChanged",
        "on_actionRemove_layer_triggered",
        "on_actionRestore_layer_triggered",
        "on_actionExport_layer_to_raw_format_triggered",
        "on_actionReplace_layer_from_raw_format_triggered",
        "on_actionAdd_layer_from_raw_format_triggered"
    };

    QtMocHelpers::UintData qt_methods {
        // Slot 'onResourceChanged'
        QtMocHelpers::SlotData<void()>(1, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onResourceError'
        QtMocHelpers::SlotData<void(const QString &)>(3, 2, QMC::AccessPrivate, QMetaType::Void, {{
            { QMetaType::QString, 4 },
        }}),
        // Slot 'onResourceInited'
        QtMocHelpers::SlotData<void()>(5, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onResourceLayerSelected'
        QtMocHelpers::SlotData<void(int)>(6, 2, QMC::AccessPrivate, QMetaType::Void, {{
            { QMetaType::Int, 7 },
        }}),
        // Slot 'onResourceStartDownload'
        QtMocHelpers::SlotData<void()>(8, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onResourceEndDownload'
        QtMocHelpers::SlotData<void()>(9, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionRemoteResource_triggered'
        QtMocHelpers::SlotData<void()>(10, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionLocalResource_triggered'
        QtMocHelpers::SlotData<void()>(11, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionQuit_triggered'
        QtMocHelpers::SlotData<void()>(12, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionSaveResource_triggered'
        QtMocHelpers::SlotData<void()>(13, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onExportObjClicked'
        QtMocHelpers::SlotData<void()>(14, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onExportObjWithTextureClicked'
        QtMocHelpers::SlotData<void()>(15, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onViewObjClicked'
        QtMocHelpers::SlotData<void()>(16, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionKritterGetter_triggered'
        QtMocHelpers::SlotData<void()>(17, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'onLayerWidgetCurrentRowChanged'
        QtMocHelpers::SlotData<void(int)>(18, 2, QMC::AccessPrivate, QMetaType::Void, {{
            { QMetaType::Int, 7 },
        }}),
        // Slot 'on_actionRemove_layer_triggered'
        QtMocHelpers::SlotData<void()>(19, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionRestore_layer_triggered'
        QtMocHelpers::SlotData<void()>(20, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionExport_layer_to_raw_format_triggered'
        QtMocHelpers::SlotData<void()>(21, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionReplace_layer_from_raw_format_triggered'
        QtMocHelpers::SlotData<void()>(22, 2, QMC::AccessPrivate, QMetaType::Void),
        // Slot 'on_actionAdd_layer_from_raw_format_triggered'
        QtMocHelpers::SlotData<void()>(23, 2, QMC::AccessPrivate, QMetaType::Void),
    };
    QtMocHelpers::UintData qt_properties {
    };
    QtMocHelpers::UintData qt_enums {
    };
    return QtMocHelpers::metaObjectData<HafenResTool, qt_meta_tag_ZN12HafenResToolE_t>(QMC::MetaObjectFlag{}, qt_stringData,
            qt_methods, qt_properties, qt_enums);
}
Q_CONSTINIT const QMetaObject HafenResTool::staticMetaObject = { {
    QMetaObject::SuperData::link<QMainWindow::staticMetaObject>(),
    qt_staticMetaObjectStaticContent<qt_meta_tag_ZN12HafenResToolE_t>.stringdata,
    qt_staticMetaObjectStaticContent<qt_meta_tag_ZN12HafenResToolE_t>.data,
    qt_static_metacall,
    nullptr,
    qt_staticMetaObjectRelocatingContent<qt_meta_tag_ZN12HafenResToolE_t>.metaTypes,
    nullptr
} };

void HafenResTool::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    auto *_t = static_cast<HafenResTool *>(_o);
    if (_c == QMetaObject::InvokeMetaMethod) {
        switch (_id) {
        case 0: _t->onResourceChanged(); break;
        case 1: _t->onResourceError((*reinterpret_cast<std::add_pointer_t<QString>>(_a[1]))); break;
        case 2: _t->onResourceInited(); break;
        case 3: _t->onResourceLayerSelected((*reinterpret_cast<std::add_pointer_t<int>>(_a[1]))); break;
        case 4: _t->onResourceStartDownload(); break;
        case 5: _t->onResourceEndDownload(); break;
        case 6: _t->on_actionRemoteResource_triggered(); break;
        case 7: _t->on_actionLocalResource_triggered(); break;
        case 8: _t->on_actionQuit_triggered(); break;
        case 9: _t->on_actionSaveResource_triggered(); break;
        case 10: _t->onExportObjClicked(); break;
        case 11: _t->onExportObjWithTextureClicked(); break;
        case 12: _t->onViewObjClicked(); break;
        case 13: _t->on_actionKritterGetter_triggered(); break;
        case 14: _t->onLayerWidgetCurrentRowChanged((*reinterpret_cast<std::add_pointer_t<int>>(_a[1]))); break;
        case 15: _t->on_actionRemove_layer_triggered(); break;
        case 16: _t->on_actionRestore_layer_triggered(); break;
        case 17: _t->on_actionExport_layer_to_raw_format_triggered(); break;
        case 18: _t->on_actionReplace_layer_from_raw_format_triggered(); break;
        case 19: _t->on_actionAdd_layer_from_raw_format_triggered(); break;
        default: ;
        }
    }
}

const QMetaObject *HafenResTool::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->dynamicMetaObject() : &staticMetaObject;
}

void *HafenResTool::qt_metacast(const char *_clname)
{
    if (!_clname) return nullptr;
    if (!strcmp(_clname, qt_staticMetaObjectStaticContent<qt_meta_tag_ZN12HafenResToolE_t>.strings))
        return static_cast<void*>(this);
    return QMainWindow::qt_metacast(_clname);
}

int HafenResTool::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QMainWindow::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 20)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 20;
    }
    if (_c == QMetaObject::RegisterMethodArgumentMetaType) {
        if (_id < 20)
            *reinterpret_cast<QMetaType *>(_a[0]) = QMetaType();
        _id -= 20;
    }
    return _id;
}
QT_WARNING_POP
