#ifndef SRCEDITOR_H
#define SRCEDITOR_H

#include "../editor.h"

class SrcEditor : public Editor
{
public:
    SrcEditor(Layer *l, QWidget *parent = nullptr);
};

#endif // SRCEDITOR_H
