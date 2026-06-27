/****************************************************************************
** Meta object code from reading C++ file 'dropdownfield.h'
**
** Created by: The Qt Meta Object Compiler version 69 (Qt 6.10.1)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "../../../widgets/fields/dropdownfield.h"
#include <QtGui/qtextcursor.h>
#include <QtCore/qmetatype.h>

#include <QtCore/qtmochelpers.h>

#include <memory>


#include <QtCore/qxptype_traits.h>
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'dropdownfield.h' doesn't include <QObject>."
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
struct qt_meta_tag_ZN13DropdownFieldE_t {};
} // unnamed namespace

template <> constexpr inline auto DropdownField::qt_create_metaobjectdata<qt_meta_tag_ZN13DropdownFieldE_t>()
{
    namespace QMC = QtMocConstants;
    QtMocHelpers::StringRefStorage qt_stringData {
        "DropdownField"
    };

    QtMocHelpers::UintData qt_methods {
    };
    QtMocHelpers::UintData qt_properties {
    };
    QtMocHelpers::UintData qt_enums {
    };
    return QtMocHelpers::metaObjectData<DropdownField, qt_meta_tag_ZN13DropdownFieldE_t>(QMC::MetaObjectFlag{}, qt_stringData,
            qt_methods, qt_properties, qt_enums);
}
Q_CONSTINIT const QMetaObject DropdownField::staticMetaObject = { {
    QMetaObject::SuperData::link<Field::staticMetaObject>(),
    qt_staticMetaObjectStaticContent<qt_meta_tag_ZN13DropdownFieldE_t>.stringdata,
    qt_staticMetaObjectStaticContent<qt_meta_tag_ZN13DropdownFieldE_t>.data,
    qt_static_metacall,
    nullptr,
    qt_staticMetaObjectRelocatingContent<qt_meta_tag_ZN13DropdownFieldE_t>.metaTypes,
    nullptr
} };

void DropdownField::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    auto *_t = static_cast<DropdownField *>(_o);
    (void)_t;
    (void)_c;
    (void)_id;
    (void)_a;
}

const QMetaObject *DropdownField::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->dynamicMetaObject() : &staticMetaObject;
}

void *DropdownField::qt_metacast(const char *_clname)
{
    if (!_clname) return nullptr;
    if (!strcmp(_clname, qt_staticMetaObjectStaticContent<qt_meta_tag_ZN13DropdownFieldE_t>.strings))
        return static_cast<void*>(this);
    return Field::qt_metacast(_clname);
}

int DropdownField::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = Field::qt_metacall(_c, _id, _a);
    return _id;
}
QT_WARNING_POP
