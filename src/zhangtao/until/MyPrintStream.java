package zhangtao.until;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.OutputStream;
import java.io.PrintStream;

public class MyPrintStream extends PrintStream {
    private TextArea textArea;

    public MyPrintStream(OutputStream out, TextArea textArea) {
        super(out);
        this.textArea = textArea;
    }

    @Override
    public void println(String s) {
        super.println(s);
        update(s);
    }

    private void update(String s) {
        Platform.runLater(() -> textArea.appendText(s+"\n"));
    }
}
