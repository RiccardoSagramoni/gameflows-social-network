package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.Admin;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityService;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityServiceFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DeleteCommunityAlertController {

	@FXML
	private Button cancelButton;

	@FXML
	private Label errorLabel;

	//VIDEOGAME VARIABLES
	private VideogameCommunity community;
	private final VideogameCommunityService videogameCommunityService = VideogameCommunityServiceFactory.getService();
	private Admin admin = null;
	private Button returnAdminHome;


	public void setData(VideogameCommunity community, Admin admin, Button returnToHomeButton){
		this.community = community;
		this.admin = admin;
		this.returnAdminHome = returnToHomeButton;
		errorLabel.setVisible(false);
	}

	private void closeDeleteWindow(){
		//CLOSE DELETE ALERT
		Stage stage1 = (Stage) cancelButton.getScene().getWindow();
		stage1.close();
	}

	@FXML
	void cancelDeletingOperation(ActionEvent event) {
		closeDeleteWindow();
	}

	@FXML
	void deleteThisVideogameCommunity(ActionEvent event) throws IOException {
		if(videogameCommunityService.deleteVideogameCommunity(community.getId())){
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminHome.fxml"));
			Parent root1 = fxmlLoader.load();

			//send admin id
			AdminHomeController controller2 = fxmlLoader.getController();
			controller2.sendAdmin(admin.getUsername());

			Stage stage = new Stage();
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

			closeDeleteWindow();

			//CLOSE USER PAGE
			Stage stage1 = (Stage) returnAdminHome.getScene().getWindow();
			stage1.close();


		}
		else{
			errorLabel.setVisible(true);
		}
	}

}
