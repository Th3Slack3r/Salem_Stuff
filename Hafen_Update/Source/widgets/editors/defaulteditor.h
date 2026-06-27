#ifndef DEFAULTEDITOR_H
#define DEFAULTEDITOR_H

#include "../editor.h"


class DefaultEditor : public Editor
{
public:
    DefaultEditor(Layer *l, QWidget *parent = 0);
};

#endif // DEFAULTEDITOR_H
