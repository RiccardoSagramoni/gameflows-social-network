package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.Admin;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.PostListener;
import it.unipi.dii.inginf.lsdb.gameflows.post.LikedPostsCache;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ItemPostController {


	@FXML
	private Label authorLabel;
	@FXML
	private Button commentButton;
	@FXML
	private Label commentsLabel;
	@FXML
	private Label likesLabel;
	@FXML
	private Button modifyButton;
	@FXML
	private Button removeButton;
	@FXML
	private Label timestampLabel;
	@FXML
	private TextArea titleLabel;
	@FXML
	private Button postLikeButton;


	//COMMUNITY VARIABLES
	private VideogameCommunity community = null;

	//POST VARIABLES
	private final PostService postService = PostServiceFactory.getService();
	private Post post;
	private PostListener postListener; //to create
	private LikedPostsCache likedPosts;

	//USER VARIABLES
	private User user = null;
	private Admin admin = null;


	/**
	 * method that receive parameters from calling page to set POST item values
	 * @param community object
	 * @param post object
	 * @param postListener object to identify the correct on click listener
	 * @param likedPosts list of all liked post by the user
	 * @param user object
	 * @param admin object
	 * @throws FileNotFoundException
	 */
	public void setData (VideogameCommunity community,
	                     Post post, PostListener postListener,
	                     LikedPostsCache likedPosts,
	                     User user, Admin admin) throws FileNotFoundException
	{
		this.community = community;
		this.post = post;
		this.postListener = postListener;
		if(admin == null){
			this.user = user;
			this.likedPosts = likedPosts;

			//initialize like button
			if(likedPosts.isPostLiked(post.getId())){
				postLikeButton.setText("Dislike");
			}
			else {
				postLikeButton.setText("Like");
			}

			//initialize remove button
			removeButton.setVisible(
					user.getUsername().equals(post.getAuthor())
			);
		}
		else{
			this.admin = admin;
			removeButton.setVisible(true);
			postLikeButton.setVisible(false);
		}

		//set all elements
		likesLabel.setText(String.valueOf(post.getLikes()));
		authorLabel.setText(post.getAuthor());
		titleLabel.setText((post.getTitle()));
		timestampLabel.setText(post.getTimestamp().toString());
	}


	/**
	 * method that allow the user to like or dislike a post
	 * @param event
	 */
	@FXML
	void likeDislikePost (ActionEvent event) {
		if (postLikeButton.getText().equals("Like")) {
			likedPosts.likePost(post.getId(), user.getUsername(), true);

			postLikeButton.setText("Dislike");
			int likes = (Integer.parseInt(likesLabel.getText())) + 1;
			likesLabel.setText("" + likes);
			post.setLikes(likes);
		}
		else if (postLikeButton.getText().equals("Dislike")) {
			likedPosts.likePost(post.getId(), user.getUsername(), false);

			postLikeButton.setText("Like");
			int likes = (Integer.parseInt(likesLabel.getText())) -1;
			likesLabel.setText("" + likes);
			post.setLikes(likes);
		}
	}

	/**
	 * method that remove a post from the db
	 * @param event
	 * @throws IOException
	 */
	@FXML
	void removePost(ActionEvent event) throws IOException {
		removeButton.setDisable(true);
		postService.deletePostById(post.getId());

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCommunityPage.fxml"));
		Parent root1 = fxmlLoader.load();

		UserCommunityPageController controller2 = fxmlLoader.getController();
		if(admin == null){
			controller2.sendVideogameCommunity(community, user, null);
		}
		else{
			controller2.sendVideogameCommunity(community, null, admin);
		}

//		controller2.refreshPage(new ActionEvent());

		Stage stage = new Stage();
		stage.setTitle("Gameflows Community Page");
		stage.setScene(new Scene(root1));
		stage.setResizable(false);
		stage.show();

		//refresh community page
		Stage stage1 = (Stage) removeButton.getScene().getWindow();
		stage1.close();

	}

	/**
	 * method that call the on click listener that is redefined in the different fill grid pane
	 * in the different pages
	 * @param mouseEvent
	 */
	@FXML
	void click(javafx.scene.input.MouseEvent mouseEvent) {
		postListener.onClickPost(mouseEvent, post);
	}
}