package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.Admin;
import it.unipi.dii.inginf.lsdb.gameflows.comment.Comment;
import it.unipi.dii.inginf.lsdb.gameflows.comment.CommentService;
import it.unipi.dii.inginf.lsdb.gameflows.comment.CommentServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.comment.InfoPost;
import it.unipi.dii.inginf.lsdb.gameflows.post.LikedPostsCache;
import it.unipi.dii.inginf.lsdb.gameflows.post.Post;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class UserCreateCommentPageController {


	@FXML
	private TextArea commentTextField;

	@FXML
	private Button createCommentButton;

	@FXML
	private Label textTextField;
	@FXML
	private Button cancelButton;


	private final CommentService commentService = CommentServiceFactory.getService();

	//USER VARIABLES
	private User user = null;
	private final Admin admin = null;

	//POST VARIABLES
	private Post post = null;
	private LikedPostsCache likedPosts = null;

	//VIDEOGAME COMMUNITY VARIABLES
	private VideogameCommunity community;

	private Button refreshButton;

	/**
	 * method that receive POST parameters from post page
	 * @param community is the videogame community where to write the comment
	 * @param user is the user that writes the comment
	 * @param post is the post where to write the comment
	 * @param refreshButton button used to refresh the page
	 * @param likedPosts user liked post
	 */
	public void sendInfo (VideogameCommunity community,
	                      User user, Post post,
	                      Button refreshButton,
	                      LikedPostsCache likedPosts)
	{
		this.community = community;
		this.user = user;
		this.post = post;
		this.refreshButton = refreshButton;
		this.likedPosts = likedPosts;
		createCommentButton.setDisable(true);
	}


	/**
	 * method that create a new comment
	 * @param event
	 */
	@FXML
	void createComment(ActionEvent event) {

		insertComment();

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userPostPage.fxml"));
			Parent root1 = fxmlLoader.load();

			UserPostPageController controller2 = fxmlLoader.getController();
			controller2.sendPost(community, post, user, admin, likedPosts);

			Stage stage = new Stage();
			stage.setTitle("Post Page");
			stage.setScene(new Scene(root1));
			stage.show();

			Stage stage1 = (Stage) createCommentButton.getScene().getWindow();
			stage1.close();

			Stage stage2 = (Stage) refreshButton.getScene().getWindow();
			stage2.close();



		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * method that insert the new comment into the db
	 */
	void insertComment(){
		//COMMENT VARIABLES
		Comment comment = new Comment(
				commentTextField.getText(),
				user.getUsername(),
				new InfoPost(
						post.getId(),
						post.getAuthor(),
						post.getVideogameCommunity().getVideogameCommunityId(),
						post.getVideogameCommunity().getVideogameCommunityName()
				)
		);
		commentService.addComment(comment);
	}


	/**
	 * method used to check if the comment field is not empty.
	 * it disable the create comment button if the field is empty
	 * @param event
	 */
	@FXML
	void enableCreateCommentButton(KeyEvent  event) {
		if(!commentTextField.getText().equals("")) {
			createCommentButton.setDisable(false);
		}
		else
			createCommentButton.setDisable(true);
	}


	/**
	 * method used to close the create comment page (cancel button)
	 * @param event
	 */
	@FXML
	void returnPostPage(ActionEvent event) {
		Stage stage1 = (Stage) createCommentButton.getScene().getWindow();
		stage1.close();

	}

}
