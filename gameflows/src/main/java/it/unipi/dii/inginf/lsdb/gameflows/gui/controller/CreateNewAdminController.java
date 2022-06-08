package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.Admin;
import it.unipi.dii.inginf.lsdb.gameflows.admin.AdminService;
import it.unipi.dii.inginf.lsdb.gameflows.admin.AdminServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.util.Password;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CreateNewAdminController {

	@FXML
	private TextField nameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label responseLabel;

	@FXML
	private Button signUpButton;

	@FXML
	private TextField usernameField;

	// ADMIN VARIABLE
	private final AdminService adminService = AdminServiceFactory.getInstance();

	/**
	 * method that initialize the element of the page
	 */
	public void initialize(){
		responseLabel.setVisible(false);
	}

	/**
	 * method that add a new admin to the db
	 * @param event of the mouse when the create button is clicked
	 */
	@FXML
	void addNewAdmin (ActionEvent event) {

		if (nameField.getText() == null || usernameField.getText() == null ||
			passwordField.getText() == null || nameField.getText().isEmpty() ||
			usernameField.getText().isEmpty() || passwordField.getText().isEmpty() ||
			nameField.getText().isBlank() || usernameField.getText().isBlank() ||
			passwordField.getText().isBlank())
		{
			responseLabel.setVisible(true);
			responseLabel.setText("ERROR! SOME FIELDS ARE MISSING");
		}
		else {
			Password password = new Password(passwordField.getText());
			Admin admin = new Admin(
					usernameField.getText(),
					password,
					nameField.getText()
			);

			adminService.createAdminAccount(admin);
			responseLabel.setVisible(true);

			//page closing
			Stage stage = (Stage) signUpButton.getScene().getWindow();
			stage.close();
		}

	}

}
