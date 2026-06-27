#ifndef IMAGEEDITOR_H
#define IMAGEEDITOR_H

#include "../editor.h"

class ImageEditor : public Editor
{
public:
    ImageEditor(Layer *l, QWidget *parent = 0);
};

#endif // IMAGEEDITOR_H
