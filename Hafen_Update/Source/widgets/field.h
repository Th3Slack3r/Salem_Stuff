#ifndef FIELD_H
#define FIELD_H

#include <QWidget>
#include <QFileDialog>
#include <QMessageBox>

class Field : public QWidget
{
    Q_OBJECT
public:
    explicit Field(const QString &title, QWidget *parent = 0);

    void setReadOnly(bool ro);
    bool getReadOnly() const;

signals:
    void fieldChanged();

protected:
    QString m_title;
    bool m_readOnly;

public slots:

protected slots:
    void onFieldChanged();
};

#endif // FIELD_H
