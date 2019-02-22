package zhangtao.gui;

import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import zhangtao.group.FindAndMove;
import zhangtao.until.MyPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    public Button fromButton;
    public Button toButton;
    public Text fromFileText;
    public Text toFileText;
    public Text patternFileText;
    public CheckBox isMovedCheck;
    public CheckBox isRepeatCheck;
    public Button patternButton;
    public Button executeButton;
    public TextArea statusText;

    private Stage stage = null;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private File from = null;
    private File to = null;
    private File pattern = null;
    private boolean isMove = true;
    private boolean isRepeat = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isMovedCheck.setSelected(true);
        isRepeatCheck.setSelected(true);

        fromButton.setOnMouseClicked(event -> {
            File file = getDirectory("选择要整里的文件夹");
            if (file == null) {
                System.out.println("null");
            } else if (!file.exists() || !file.isDirectory()) {
                showDialog("文件不存在或不是文件夹");
            } else {
                from = file;
                fromFileText.setText(file.getAbsolutePath());
            }
        });
        toButton.setOnMouseClicked(event -> {
            File file = getDirectory("选择要保存到的文件夹");
            if (file == null) {
                System.out.println("null");
            } else if (!file.exists() || !file.isDirectory()) {
                showDialog("文件不存在或不是文件夹");
            } else {
                toFileText.setText(file.getAbsolutePath());
                to = file;
            }
        });
        patternButton.setOnMouseClicked(event -> {
            File file = getFile("选择通配符文件，按行分隔");
            if (file == null) {
                System.out.println("null");
            } else if (!file.exists() || file.isDirectory()) {
                showDialog("文件不存在或是文件夹");
            } else {
                pattern = file;
                patternFileText.setText(file.getAbsolutePath());
            }
        });
        executeButton.setOnMouseClicked(event -> {
            if (from == null) {
                showDialog("来源文件夹不存在");
                return;
            } else if (to == null) {
                showDialog("目标文件夹不存在");
                return;
            }
            if (pattern == null) {
                statusText.setText(statusText.getText() + "\n通配符文件不存在，已在默认位置查找!\n");
                pattern = to.toPath().resolve("pattern.txt").toFile();
                if (pattern == null||!pattern.exists()||pattern.isDirectory()) {
                    showDialog("通配符（匹配规则）文件不存在:" + pattern.getAbsolutePath());
                    return;
                }
            }
            isRepeat =isRepeatCheck.isSelected();
            isMove = isMovedCheck.isSelected();


            if(from.getAbsolutePath().indexOf(to.getAbsolutePath())==0){
                showDialog("要保存的文件夹是目标文件夹的子文件夹，会造成错误！");
                return;
            }
            FindAndMove findAndMove =
                    new FindAndMove(from.getAbsolutePath(),to.getAbsolutePath(),pattern.getAbsolutePath(),isMove,isRepeat);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MyPrintStream printStream = new MyPrintStream(outputStream,statusText);
            findAndMove.setOut(printStream);
            new Thread(()-> findAndMove.execute()).start();
//            findAndMove.execute();
        });
    }

    private File getDirectory(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(stage);
    }
    private File getFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(stage);
    }

    private void showDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING, text);
        alert.showAndWait();
    }

}
