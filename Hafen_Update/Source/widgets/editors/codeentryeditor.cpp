#include "codeentryeditor.h"

#include "../../resources/layers/codeentrylayer.h"

#include "../fields/mapfield.h"

CodeentryEditor::CodeentryEditor(Layer *l, QWidget *parent)
    : Editor(l, parent)
{
    // Cast layer
    CodeentryLayer *nl = dynamic_cast<CodeentryLayer *>(l);

    // Set up fields widgets and add them to layout
    // map
    MapField::MapType type;
    switch (nl->t()) {
    case 0: type = MapField::Int; break;
    case 1: type = MapField::String; break;
    default: type = MapField::String; break;
    }

    MapField *f_map = new MapField("map", nl->map(), type, this);
    m_layout->addWidget(f_map);
}
