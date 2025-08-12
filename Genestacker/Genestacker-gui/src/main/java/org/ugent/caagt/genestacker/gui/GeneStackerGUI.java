package org.ugent.caagt.genestacker.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ugent.caagt.genestacker.cli.Main;

public class GeneStackerGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML layout
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        
        // Set up the scene and stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Gene Stacker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Method to run Gene Stacker with the specified arguments
     * This method can be called from the controller to execute the Gene Stacker algorithm
     * 
     * @param args Command line arguments for Gene Stacker
     */
    public static void runGeneStacker(String[] args) {
        // We can call the Main class from the CLI module directly
        Main.main(args);
    }
}