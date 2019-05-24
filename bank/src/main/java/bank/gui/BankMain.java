package bank.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BankMain extends Application {

    private static String name;

    @Override
    public void start(Stage primaryStage) throws Exception{
        // load FXML file is in bank/src/main/resources/bank.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("bank.fxml"));
        Parent root = loader.load();
        BankController bankController = loader.getController();

        bankController.initGateway(name);

        primaryStage.setTitle("BANK - " + name);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.setScene(new Scene(root, 500,300));
        primaryStage.show();
    }


    public static void main(String[] args) {
        name = args[0];
        launch(args);
    }
}
