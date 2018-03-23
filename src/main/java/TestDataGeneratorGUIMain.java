/**
 * Created by blindcant on 7/07/17.
 *
 * https://docs.oracle.com/javase/8/javafx/api/javafx/fxml/doc-files/introduction_to_fxml.html#fxmlloader
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TestDataGeneratorGUIMain extends Application
{
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException
	{
/*		URL url = getClass().getResource("TestDataGeneratorGUI.fxml");
		ResourceBundle resourceBundle = ResourceBundle.getBundle("");
		Parent root = FXMLLoader.load(url, resourceBundle);*/

		Parent root = FXMLLoader.load(getClass().getResource("TestDataGeneratorGUI.fxml"));
		Scene scene = new Scene(root, 1280, 720);
		
		primaryStage.setTitle("Test Data Generator & Exporter");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
