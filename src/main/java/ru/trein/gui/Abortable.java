package ru.trein.gui;

/**
 * Created by Viote on 13.06.2017.
 */
public abstract class Abortable {
    protected boolean aborted;

    public void setAborted(boolean value) {
        this.aborted = value;
    }

}
