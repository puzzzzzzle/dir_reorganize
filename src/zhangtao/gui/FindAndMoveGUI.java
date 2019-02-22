package zhangtao.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FindAndMoveGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resource/sample.fxml"));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        controller.setStage(primaryStage);

        primaryStage.setTitle("文件搬运");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
