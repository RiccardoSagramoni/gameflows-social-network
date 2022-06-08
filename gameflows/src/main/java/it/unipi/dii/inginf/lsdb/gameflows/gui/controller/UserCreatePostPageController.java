package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.post.InfoVideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.post.Post;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostService;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class UserCreatePostPageController {

	@FXML
	private Button createPostButton;
	@FXML
	private TextArea textTextField;
	@FXML
	private TextArea titleTextField;
	@FXML
	private Button cancelButton;
	@FXML
	void click(MouseEvent event) {
	}

	//VIDEOGAME VARIABLES
	VideogameCommunity community = null;

	//USER VARIABLES
	User user = null;

	//POST VARIABLES
	Post post = null;
	PostService postService = PostServiceFactory.getService();

	private Button refreshButton;

	/**
	 * method that receive videogame community parameters from videogame community page
	 * @param community is the videogame community where insert the new post
	 * @param user is the user that are writing the post
	 * @param refreshButton button used to refresh the videogame community page
	 */
	public void sendCommunity(VideogameCommunity community, User user, Button refreshButton){
		this.community = community;
		this.user = user;
		this.refreshButton = refreshButton;

	}


	/**
	 * mehtod that create a new post
	 * @param event
	 */
	@FXML
	void sendPostcreated(ActionEvent event) {

		insertPost();
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCommunityPage.fxml"));
			Parent root1 = fxmlLoader.load();


			UserCommunityPageController controller2 = fxmlLoader.getController();
			controller2.sendVideogameCommunity(community, user, null);


			Stage stage = new Stage();
			stage.setTitle("Gameflows Community Page");
			stage.setScene(new Scene(root1));
			stage.show();

			Stage stage1 = (Stage) createPostButton.getScene().getWindow();
			stage1.close();

			Stage stage2 = (Stage) refreshButton.getScene().getWindow();
			stage2.close();



		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * method used sendPostcreated.
	 * create the post and insert it into the db
	 */
	void insertPost(){
		//create new Post item
		this.post = new Post(
				titleTextField.getText(),
				user.getUsername(),
				textTextField.getText(),
				new InfoVideogameCommunity(
						community.getId(),
						community.getName(),
						community.getGenre()
				),
				user.isInfluencer()
		);

		postService.insertPost(post);
	}

	/**
	 * close write post page (cancel button)
	 * @param event
	 */
	@FXML
	void returnCommunityPage(ActionEvent event) {
		Stage stage1 = (Stage) createPostButton.getScene().getWindow();
		stage1.close();

	}



}