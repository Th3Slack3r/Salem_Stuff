#include "hafenrestool.h"
#include <QApplication>
#include <QDebug>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    HafenResTool w;
    w.show();

    return a.exec();
}
