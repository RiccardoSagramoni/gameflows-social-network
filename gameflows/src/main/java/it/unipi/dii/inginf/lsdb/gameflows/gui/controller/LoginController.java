package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.AdminService;
import it.unipi.dii.inginf.lsdb.gameflows.admin.AdminServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.gui.model.LoginMode;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserService;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserServiceFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;

public class LoginController {

	@FXML
	private Label loginLabel;
	@FXML
	private RadioButton adminButton, userButton;
	@FXML
	private Button loginButton;
	@FXML
	private PasswordField passwordTextField;
	@FXML
	private TextField usernameTextField;


	//VARIABLES
	private boolean adminFlag = false;
	private boolean userFlag = false;

	//USER VARIABLES
	private final UserService userService = UserServiceFactory.getService();
	private LoginMode loginMode = null;
	private final AdminService adminService = AdminServiceFactory.getInstance();


	/**
	 * Main method for the login. based on the loginMode it makes the login for
	 * the admin or the user
	 * @param event
	 */
	@FXML
	void loginUserAdmin(ActionEvent event) {
		//check login mode
		if(loginMode == LoginMode.user){
			//check username and password
			if(userService.login(usernameTextField.getText(), passwordTextField.getText() )){
				try {
					//OPEN USER HOME PAGE
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userHome.fxml"));
					Parent root1 = fxmlLoader.load();

					//send user id
					String username = usernameTextField.getText();
					UserHomeController controller2 = fxmlLoader.getController();
					controller2.sendUser(username);

					Stage stage = new Stage();
					stage.setTitle("Gameflows User Home");
					stage.setScene(new Scene(root1));
					stage.setResizable(false);
					stage.show();

					loginLabel.setStyle("-fx-text-fill: #21ff00;");
					loginLabel.setText("SUCCESSFULLY LOGIN");

					//CLOSE LOGIN PAGE
					Stage stage1 = (Stage) loginButton.getScene().getWindow();
					stage1.close();

				}catch (Exception e){
					e.printStackTrace();
				}
			}
			//INCORRECT USERNAME OR PASSWORD
			else{
				loginLabel.setStyle("-fx-text-fill: #ff0000;");
				loginLabel.setText("LOGIN ERROR!");
			}
		}
		else if(loginMode == LoginMode.admin) {

			if(adminService.login(usernameTextField.getText(), passwordTextField.getText() )){

				try {
					//OPEN USER HOME PAGE
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminHome.fxml"));
					Parent root1 = fxmlLoader.load();

					//send user id
					String username = usernameTextField.getText();
					AdminHomeController controller2 = fxmlLoader.getController();
					controller2.sendAdmin(username);

					Stage stage = new Stage();
					stage.setTitle("Gameflows User Home");
					stage.setScene(new Scene(root1));
					stage.setResizable(false);
					stage.show();

					loginLabel.setStyle("-fx-text-fill: #21ff00;");
					loginLabel.setText("SUCCESSFULLY LOGIN");

					//CLOSE LOGIN PAGE
					Stage stage1 = (Stage) loginButton.getScene().getWindow();
					stage1.close();

				}catch (Exception e){
					e.printStackTrace();
				}
			}
			//INCORRECT USERNAME OR PASSWORD
			else{
				loginLabel.setStyle("-fx-text-fill: #ff0000;");
				loginLabel.setText("LOGIN ERROR!");
			}
		}
		else{
			loginLabel.setStyle("-fx-text-fill: #ff0000;");
			loginLabel.setText("LOGIN ERROR!");
		}

	}

	/**
	 * method that set the loginMode on "admin"
	 * @param event
	 */
	@FXML
	void chooseAdminLogin(ActionEvent event) {
		adminFlag = true;
		adminButton.setSelected(true);
		loginMode = LoginMode.admin;
		if (userFlag){
			userButton.setSelected(false);
			userFlag = false;
		}
	}

	/**
	 * method that set the loginMode on "user"
	 * @param event
	 */
	@FXML
	void chooseUserLogin(ActionEvent event) {
		userFlag = true;
		userButton.setSelected(true);
		loginMode = LoginMode.user;
		if (adminFlag){
			adminButton.setSelected(false);
			adminFlag = false;
		}
	}

	/**
	 * method that open the sign up page.
	 * It let the registration of a new user
	 * @param event
	 */
	@FXML
	void signUpUser(ActionEvent event) {
		try {
			//OPEN SIGN UP PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userSignUpPage.fxml"));
			Parent root1 = fxmlLoader.load();

			Stage stage = new Stage();
			stage.setTitle("Sign Up Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
