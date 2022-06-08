package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.gui.model.LoginMode;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserService;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.util.Password;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.ZoneId;
import java.util.Date;

public class UserSignUpPageController {

	@FXML
	private TextField nameField;
	@FXML
	private TextField surnameField;
	@FXML
	private TextField usernameField;
	@FXML
	private TextField emailField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private RadioButton femaleButton;
	@FXML
	private RadioButton maleButton;
	@FXML
	private DatePicker birthDatePicker;
	@FXML
	private TextField nationalityField;
	@FXML
	private Button signUpButton;
	@FXML
	private Label errorLabel;


	//VARIABLES
	private boolean maleFlag = false;
	private boolean femaleFlag = false;
	private String gender = "";

	//USER VARIABLES
	UserService userService = UserServiceFactory.getService();


	/**
	 * method that register a new user.
	 * it checks if all the mandatory fields are not empty
	 * @param event
	 */
	@FXML
	void signUpUser(ActionEvent event) {

		if(
				nameField.getText() == null ||
				surnameField.getText() == null ||
				usernameField.getText() == null ||
				emailField.getText() == null ||
				passwordField.getText() == null ||
				gender.equals("")||
				birthDatePicker.getValue() == null ||
				nationalityField.getText() == null
		){
			errorLabel.setText("ERROR! SOME FIELD IS MISSING");
		}
		else{
			Password password = new Password(passwordField.getText());
			Date dateOfBirth = Date.from((birthDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
			User user = new User(
					usernameField.getText(),
					emailField.getText(),
					password,
					nameField.getText(),
					surnameField.getText(),
					gender,
					dateOfBirth,
					nationalityField.getText()
			);

			if(userService.insertUser(user) != null){
				errorLabel.setStyle("-fx-text-fill: #21ff00;");
				errorLabel.setText("CONGRATULATIONS! WELCOME TO GAMEFLOWS");
				//page closing
				Stage stage = (Stage) signUpButton.getScene().getWindow();
				stage.close();
			}
			else{
				errorLabel.setStyle("-fx-text-fill: #ff0000;");
				errorLabel.setText("ERROR! USERNAME ALREADY CHOOSEN");
			}
		}
	}


	/**
	 * check for the Male radio button.
	 * it disables Female button if Male is pressed
	 * @param event
	 */
	@FXML
	void chooseMale(ActionEvent event) {
		maleFlag = true;
		gender = "Male";
		if (femaleFlag){
			femaleButton.setSelected(false);
			femaleFlag = false;
		}
	}

	/**
	 * check for the Female radio button.
	 * it disables Male button if Female is pressed
	 * @param event
	 */
	@FXML
	void chooseFemale(ActionEvent event) {
		femaleFlag = true;
		gender = "Female";
		if (maleFlag){
			maleButton.setSelected(false);
			maleFlag = false;
		}
	}


}
