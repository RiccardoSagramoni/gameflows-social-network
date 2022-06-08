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
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class UserPostPageController {

	@FXML
	private GridPane commentGridPane;
	@FXML
	private ScrollPane commentScrollPane;
	@FXML
	private Button nextButton;
	@FXML
	private Label postAuthorLabel;
	@FXML
	private Label postLikesLabel;
	@FXML
	private Button postModifyButton;
	@FXML
	private Button postRemoveButton;
	@FXML
	private TextArea postTextLabel;
	@FXML
	private Label postTimestampLabel;
	@FXML
	private TextArea postTitleLabel;
	@FXML
	private Button prevButton;
	@FXML
	private AnchorPane videogameAnchorPane;
	@FXML
	private Button addCommentButton;
	@FXML
	private Button refreshButton;
	@FXML
	private Button returnButton;


	//VIDEOGAME COMMUNITY VARIABLES
	private VideogameCommunity community;

	//POST VARIABLES
	private Post post;
	private ObjectId post_id = new ObjectId();
	private LikedPostsCache likedPosts;
	private LikedCommentsCache likedComments = null;

	//COMMENT VARIABLES
	private final CommentService CommentCollection = CommentServiceFactory.getService();
	private final List<Comment> comments = new ArrayList<>();

	//USER VARIABLES
	private User user = null;
	private Admin admin = null;

	//VARIABLES
	private int skipCounter = 0;
	private static final int SKIP = 20; //how many videogame to skip per time (coincide with limit)
	private static final int LIMIT = 20; //how many videogame community to show for each page
	private int columnGridPane = 0;
	private int rowGridPane = 0;


	/**
	 * method that receive post parameters from videogame community page
	 * @param community videogame community object
	 * @param post object
	 * @param user object
	 * @param admin object
	 * @param likedPosts list of liked post of the user
	 */
	public void sendPost (VideogameCommunity community,
	                      Post post, User user, Admin admin,
	                      LikedPostsCache likedPosts)
	{
		this.community = community;

		if(admin == null){
			this.user = user;
			this.likedPosts = likedPosts;

		}
		else{
			this.admin = admin;
		}

		//if needed
		this.post_id = post.getId();
		this.post = post;
		postLikesLabel.setText(String.valueOf(post.getLikes()));
		postAuthorLabel.setText(post.getAuthor());
		postTitleLabel.setText((post.getTitle()));
		postTimestampLabel.setText(post.getTimestamp().toString());
		postTextLabel.setText(post.getText());

		initialize();
	}

	/**
	 * methods that retrieve the comments from the db,
	 *
	 * @return a list of community object
	 */
	private List<Comment> getData(){
		//CHECK if prevButton can work
		if (skipCounter < 0){
			skipCounter = 0;
			prevButton.setDisable(true);
			nextButton.setDisable(false);
		}

		List<Comment> commentList = CommentCollection.browseByPost(post_id,skipCounter, LIMIT);
		prevNextButtonsCheck(commentList);
		return commentList;
	}

	/**
	 * method to check if the next or the prev button should be disabled according to
	 * the dimension of the list of the data retrieved from the db, and if yes it disable them
	 * @param commentList retrieved from the db
	 */
	void prevNextButtonsCheck(List<Comment> commentList){
		if((commentList.size() > 0)){
			if((commentList.size() < LIMIT)){
				if(skipCounter <= 0 ){
					prevButton.setDisable(true);
					nextButton.setDisable(true);
				}
				else{
					prevButton.setDisable(false);
					nextButton.setDisable(true);
				}
			}
			else{
				if(skipCounter <= 0 ){
					prevButton.setDisable(true);
					nextButton.setDisable(false);
				}
				else{
					prevButton.setDisable(false);
					nextButton.setDisable(false);
				}
			}
		}
		else{
			if(skipCounter <= 0 ){
				prevButton.setDisable(true);
				nextButton.setDisable(true);
			}
			else {
				prevButton.setDisable(false);
				nextButton.setDisable(true);
			}
		}
	}

	/**
	 * method that initialize the interface and call the methods to retrieve the first data from the db
	 */
	void initialize(){

		if(user != null){
			//to send list of liked comment
			likedComments = getLikedComments();
		}
		else{
			addCommentButton.setVisible(false);
		}

		skipCounter = 0;
		nextButton.setDisable(false);
		prevButton.setDisable(false);

		//initalize post
		comments.addAll(getData());
		fillGridPane();
	}

	/**
	 * Action method when the nextButton is pressed.
	 * Shows the next block of videogame communities researched.
	 */
	@FXML
	void goNextPage(ActionEvent event) {
		//clear GridPane and videogames list
		commentGridPane.getChildren().clear();
		comments.clear();
		//update the skipcounter
		skipCounter += SKIP;

		comments.addAll(getData());
		fillGridPane();
	}

	/**
	 * Action method when the nextButton is pressed.
	 * Shows the previous block of videogame communities researched.
	 */
	@FXML
	void goPrevPage(ActionEvent event) {
		//clear GridPane and videogames list
		commentGridPane.getChildren().clear();
		comments.clear();
		//update the skipcounter
		skipCounter -= SKIP;

		comments.addAll(getData());
		fillGridPane();
	}

	/**
	 * method that make the refresh of the page
	 * @param event
	 */
	@FXML
	void refreshPage(ActionEvent event) {
		//clear GridPane and videogames list
		commentGridPane.getChildren().clear();
		comments.clear();
		initialize();
	}

	/**
	 * method that let a user to return into the videogame community page
	 * @param event
	 */
	@FXML
	void returnCommunityPage(ActionEvent event) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCommunityPage.fxml"));
			Parent root1 = fxmlLoader.load();

			UserCommunityPageController controller2 = fxmlLoader.getController();
			if(admin == null){
				controller2.sendVideogameCommunity(community, user, null);
			}
			else{
				controller2.sendVideogameCommunity(community, null, admin);
			}

			Stage stage = new Stage();
			stage.setTitle("Gameflows Community Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

			Stage stage1 = (Stage) returnButton.getScene().getWindow();
			stage1.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * method that let the user create a new comment.
	 * It opens Create Comment Page
	 * @param event
	 */
	@FXML
	void addComment(ActionEvent event) {
		try {
			//OPEN CREATE POST PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCreateCommentPage.fxml"));
			Parent root1 = fxmlLoader.load();

			UserCreateCommentPageController controller = fxmlLoader.getController();
			controller.sendInfo(community,user, post, refreshButton, likedPosts);

			Stage stage = new Stage();
			stage.setTitle("Create Comment Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();



		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	void setGridPaneColumnAndRow(int listDimension){

		columnGridPane = 0;
		rowGridPane = 1;
	}

	/**
	 * method called in all methods that need to create comment items to put in the GridPane
	 */
	@FXML
	void fillGridPane() {

		columnGridPane = 0;
		rowGridPane = 0;

		setGridPaneColumnAndRow(comments.size());

		try {
			for (Comment comment : comments) {
				FXMLLoader fxmlLoader = new FXMLLoader();
				fxmlLoader.setLocation(getClass().getResource("itemComment.fxml"));
				AnchorPane anchorPane = fxmlLoader.load();

				ItemCommentController itemController = fxmlLoader.getController();
				itemController.setData(community,post,comment,user, admin, likedComments,likedPosts);

				if (columnGridPane == 1) {
					columnGridPane = 0;
					rowGridPane++;
				}
				//put a comment item in the Pane
				commentGridPane.add(anchorPane, columnGridPane++, rowGridPane);

				//DISPLAY SETTINGS
				//set grid width
				commentGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
				commentGridPane.setPrefWidth(430);
				commentGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
				//set grid height
				commentGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
				commentGridPane.setPrefHeight(71);
				commentGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
				GridPane.setMargin(anchorPane, new Insets(10));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that retrieve all the liked comment of the user
	 * @return a local copy of the liked comments if application is in user mode, null if in admin mode
	 */
	private LikedCommentsCache getLikedComments () {
		if (admin != null) {
			return null;
		}

		return new LikedCommentsCache(user.getUsername(), post_id);
	}

}
