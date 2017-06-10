package ru.trein.gui.util;


import javafx.scene.control.TextArea;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * Created by Shpien on 09.06.2017.
 */
public class WriterGUI {
    private static WriterGUI instance;
    private TextArea out;

    private WriterGUI() {}

    public static WriterGUI getInstance() {
        if(instance == null)
            instance = new WriterGUI();

        return instance;
    }

    public void setOut(TextArea textArea){
        out = textArea;
    }

    public void writeStatus(String text) {
        out.appendText("Статус:     " + text + '\n');
    }
}
