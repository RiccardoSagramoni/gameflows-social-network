package it.unipi.dii.inginf.lsdb.gameflows.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameflowsApplication extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(GameflowsApplication.class.getResource("gameflows_login.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		stage.setTitle("Welcome to Gameflows App");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}