package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.Admin;
import it.unipi.dii.inginf.lsdb.gameflows.comment.Comment;
import it.unipi.dii.inginf.lsdb.gameflows.comment.CommentService;
import it.unipi.dii.inginf.lsdb.gameflows.comment.CommentServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.comment.LikedCommentsCache;
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
import javafx.stage.Stage;

import java.io.IOException;

public class ItemCommentController {

	@FXML
	private Label commentAuthorLabel;
	@FXML
	private Button likeButton;
	@FXML
	private Label commentLikesLabel;
	@FXML
	private Button commentRemoveButton;
	@FXML
	private Label commentTimestampLabel;

	@FXML
	private TextArea commentTextLabel;

	//COMMENT VARIABLES
	private Comment comment;
	private final CommentService commentService = CommentServiceFactory.getService();
	private LikedCommentsCache likedComments = null;

	//USER VARIABLES
	private User user = null;
	private Admin admin = null;

	//POST VARIABLES
	private Post post;
	private LikedPostsCache likedPosts;

	//VIDEOGAME COMMUNITY VARIABLES
	VideogameCommunity community;

	/**
	 * method that initialize the interface and call the methods to retrieve the first data from the db
	 */
	void initialize(){
		if(user != null){
			//initialize like button
			if(likedComments.isCommentLiked(comment.getId())){
				likeButton.setText("Dislike");
			}
			else {
				likeButton.setText("Like");
			}

			//initialize remove button
			commentRemoveButton.setVisible(
					user.getUsername().equals(comment.getAuthor())
			);
		}
		else {
			likeButton.setVisible(false);
		}
	}

	/**
	 * method that receive parameters from calling page to set COMMENT item values
	 * @param community object
	 * @param post object
	 * @param comment object
	 * @param user object
	 * @param admin object
	 * @param likedComments list of liked comments of the user
	 * @param likedPosts list of liked post of the user
	 */
	public void setData (VideogameCommunity community,
	                     Post post, Comment comment,
	                     User user, Admin admin,
	                     LikedCommentsCache likedComments,
	                     LikedPostsCache likedPosts)
	{
		this.community = community;
		this.comment = comment;

		if(admin == null){
			this.user = user;
			this.likedComments = likedComments;
			this.likedPosts = likedPosts;
		}
		else{
			this.admin = admin;
		}

		this.post = post;

		//set all elements
		commentAuthorLabel.setText(comment.getAuthor());
		commentLikesLabel.setText(String.valueOf(comment.getLikes()));
		commentTimestampLabel.setText(comment.getTimestamp().toString());
		commentTextLabel.setText(comment.getText());

		initialize();
	}

	/**
	 * method that allow to remove a comment
	 * @param event
	 * @throws IOException
	 */
	@FXML
	void removeComment(ActionEvent event) throws IOException {

		commentRemoveButton.setDisable(true);
		commentService.deleteCommentById(comment.getId());

		//REFRESH POST PAGE
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userPostPage.fxml"));
		Parent root1 = fxmlLoader.load();

		UserPostPageController controller2 = fxmlLoader.getController();
		controller2.sendPost(community, post, user, admin, likedPosts);

		Stage stage = new Stage();
		stage.setTitle("Post page");
		stage.setScene(new Scene(root1));
		stage.setResizable(false);
		stage.show();

		Stage stage1 = (Stage) commentRemoveButton.getScene().getWindow();
		stage1.close();
	}


	/**
	 * method that allow the user to like or dislike a comment
	 * @param event
	 */
	@FXML
	void likeDislikeComment (ActionEvent event) {
		if (likeButton.getText().equals("Like")) {
			likedComments.likeComment(comment.getId(),user.getUsername(),true);

			likeButton.setText("Dislike");
			int likes = (Integer.parseInt(commentLikesLabel.getText())) + 1;
			commentLikesLabel.setText(""+likes);
		}
		else if (likeButton.getText().equals("Dislike")) {
			likedComments.likeComment(comment.getId(),user.getUsername(),false);

			likeButton.setText("Like");
			int likes = (Integer.parseInt(commentLikesLabel.getText())) - 1;
			commentLikesLabel.setText(""+likes);
		}
	}


}

