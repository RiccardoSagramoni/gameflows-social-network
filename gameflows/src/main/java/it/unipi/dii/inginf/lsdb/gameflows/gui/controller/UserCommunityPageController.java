package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.PostListener;
import it.unipi.dii.inginf.lsdb.gameflows.gui.model.CommunityPageResearchMode;
import it.unipi.dii.inginf.lsdb.gameflows.admin.*;
import it.unipi.dii.inginf.lsdb.gameflows.post.*;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserService;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.FollowedVideogameCache;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityService;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityServiceFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserCommunityPageController{

	private static final Logger LOGGER = LogManager.getLogger(UserCommunityPageController.class);

	@FXML
	private Button nextButton;
	@FXML
	private AnchorPane postAnchorPane;
	@FXML
	private GridPane postGridPane;
	@FXML
	private ScrollPane postScrollPane;
	@FXML
	private Button prevButton;
	@FXML
	private ImageView communityLogoImageView;
	@FXML
	private Label communityNameLabel;
	@FXML
	private CustomMenuItem mostLikedPostButton;
	@FXML
	private CustomMenuItem mostRecentPostButton;
	@FXML
	private Button followButton;
	@FXML
	private Button writePostButton;
	@FXML
	private Button deleteCommunityButton;
	@FXML
	private CustomMenuItem mostLikedInfluencerPostButton;
	@FXML
	private CustomMenuItem mostRecentInfluencerPostButton;
	@FXML
	private Button refreshButton;
	@FXML
	private Button viewInfoButton;
	@FXML
	private Button returnToHomeButton;
	@FXML
	private Button viewUserRankingByPost;
	@FXML
	private Line separetorToMakeInvisible;
	@FXML
	private Line separetorToMakeInvisible2;


	//VARIABLES
	private CommunityPageResearchMode choiceMode = null;
	private int skipCounter = 0;
	private final int skip = 20; //how many videogame to skip per time (coincide with limit)
	private final int limit = 20; //how many videogame community to show for each page
	private int columnGridPane = 0;
	private int rowGridPane = 0;

	//VIDEOGAME VARIABLES
	private VideogameCommunity community = null;
	private ObjectId videogame_id = null;
	private final VideogameCommunityService videogameCommunityService
			= VideogameCommunityServiceFactory.getService();
	private FollowedVideogameCache followedVideogameCache = null;
	private LikedPostsCache likedPosts = null;

	//POST VARIABLES
	private final PostService PostCollection = PostServiceFactory.getService();
	private final List<Post> posts = new ArrayList<>();

	//USER VARIABLES
	private final UserService userService = UserServiceFactory.getService();
	private User user = null;
	private Admin admin = null;


	/**
	 * method that receive parameters from calling page to set VIDEOGAME COMMUNITY item
	 * @param community object
	 * @param usernameUser user object
	 * @param usernameAdmin admin object
	 */
	public void sendVideogameCommunity(VideogameCommunity community, User usernameUser, Admin usernameAdmin){
		this.community = community;
		if(usernameUser == null){
			this.admin = usernameAdmin;
		}
		else{
			this.user = usernameUser;
		}

		videogame_id = community.getId();

		communityNameLabel.setText(community.getName() + " Community Page");

		String imageSource = "https:"+community.getCover();
		Image image = new Image(imageSource);
		communityLogoImageView.setImage(image);
		//send object id to initalize and search posts
		initialize();
	}

	/**
	 * methods that retrieve the posts from the db.
	 * it makes different research by using choiceMode
	 * @return list of posts
	 */
	private List<Post> getData(){
		List<Post> postList;

		switch (choiceMode){
			case mostRecent:
				postList = PostCollection.browsePosts(
						videogame_id,
						PostFilter.date,
						false,
						skipCounter,
						limit
				);
				break;
			case influencerMostRecent:
				postList = PostCollection.browsePosts(
						videogame_id,
						PostFilter.date,
						true,
						skipCounter,
						limit);
				break;
			case mostLiked:
				postList = PostCollection.browsePosts(
						videogame_id,
						PostFilter.like,
						false,
						skipCounter,
						limit
				);
				break;
			case influencerMostLiked:
				postList = PostCollection.browsePosts(
						videogame_id,
						PostFilter.like,
						true,
						skipCounter,
						limit
				);
				break;
			default:
				postList = new ArrayList<>();
				break;
		}

		prevNextButtonsCheck(postList);
		return postList;
	}

	/**
	 * method to check if the next or the prev button should be disabled according to
	 * the dimension of the list of the data retrieved from the db, and if yes it disable them
	 * @param posts of the data retrieved from the db
	 */
	private void prevNextButtonsCheck(List<Post> posts){
		if((posts.size() > 0)){
			if((posts.size() < limit)){
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
	void initialize() {
		//check and set followButton (Follow/Unfollow)
		if(user != null){
			viewUserRankingByPost.setVisible(false);
			separetorToMakeInvisible.setVisible(false);
			separetorToMakeInvisible2.setVisible(false);

			followedVideogameCache = FollowedVideogameCache.getInstance(user);

			boolean result = followedVideogameCache.isVideogameFollowed(community.getId());

			if (result) {
				followButton.setText("Unfollow");
			}
			else {
				followButton.setText("Follow");
			}

			likedPosts = getLikedPosts();
			deleteCommunityButton.setVisible(false);
			deleteCommunityButton.setDisable(true);
		}
		else{
			followButton.setVisible(false);
			writePostButton.setVisible(false);
		}
		choiceMode = CommunityPageResearchMode.mostRecent;
		skipCounter = 0;
		nextButton.setDisable(false);
		prevButton.setDisable(false);

		// initialize post
		posts.addAll(getData());
		fillGridPane();
	}

	/**
	 * method used by the admin.
	 * It shows a sorted list of users that wrote the higher number of posts
	 * @param event
	 * @throws IOException
	 */
	@FXML
	void viewUserWhoWroteHigherNumberOfPost(ActionEvent event) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("viewBestUsersByNumberOfPosts.fxml"));
		Parent root1 = fxmlLoader.load();

		ViewBestUsersByNumberOfPostsController controller2 = fxmlLoader.getController();
		// chidere sta cosa qua come funziona il send per riciclare delle pagine
		controller2.viewBestUsersByNumberOfPosts(admin, community);

		Stage stage = new Stage();
		stage.setTitle("Best User Ranking by Number of Post Page");
		stage.setScene(new Scene(root1));
		stage.setResizable(false);
		stage.show();

		Stage stage1 = (Stage) postGridPane.getScene().getWindow();
		stage1.close();
	}

	/**
	 * method that shows the most recent posts
	 * @param event
	 */
	@FXML
	void viewMostRecentPosts(ActionEvent event) {
		choiceMode = CommunityPageResearchMode.mostRecent;
		//clear all
		skipCounter = 0;
		postGridPane.getChildren().clear();
		posts.clear();

		posts.addAll(getData());
		fillGridPane();

	}

	/**
	 * method that shows the most recent posts only wrote by influencers
	 * @param event
	 */
	@FXML
	void viewMostRecentInfluencerPosts(ActionEvent event) {
		choiceMode = CommunityPageResearchMode.influencerMostRecent;
		//clear all
		skipCounter = 0;
		postGridPane.getChildren().clear();
		posts.clear();

		posts.addAll(getData());
		fillGridPane();
	}

	/**
	 * method that shows most liked posts, sorted by date (most recent)
	 * @param event
	 */
	@FXML
	void viewMostLikedPost(ActionEvent event) {
		choiceMode = CommunityPageResearchMode.mostLiked;
		//clear all
		skipCounter = 0;
		postGridPane.getChildren().clear();
		posts.clear();

		posts.addAll(getData());
		fillGridPane();
	}

	/**
	 * method that shows most liked posts only wrote by influencers, sorted by date (most recent)
	 * @param event
	 */
	@FXML
	void viewMostLikedInfluencerPost(ActionEvent event) {
		choiceMode = CommunityPageResearchMode.influencerMostLiked;
		//clear all
		skipCounter = 0;
		postGridPane.getChildren().clear();
		posts.clear();

		posts.addAll(getData());
		fillGridPane();
	}


	/**
	 * Method that open the view videogame info page.
	 * It show all addition info on a videogame community
	 * @param event
	 */
	@FXML
	void viewVideogameInfo(ActionEvent event) {
		try {
			//OPEN VIDEOGAME INFO PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userVideogameInfoPage.fxml"));
			Parent root1 = fxmlLoader.load();

			//send the videogame community
			UserVideogameInfoPageController controller = fxmlLoader.getController();
			controller.sendCommunity(community);

			Stage stage = new Stage();
			stage.setTitle("Videogame Community Info Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method that open an alert when the admin select to delete the current videogame community
	 * @param event
	 */
	@FXML
	void viewDeleteCommunityAlert(ActionEvent event) {
		try {
			//OPEN CREATE POST PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("deleteCommunityAlert.fxml"));
			Parent root1 = fxmlLoader.load();

			DeleteCommunityAlertController controller = fxmlLoader.getController();
			controller.setData(community, admin, returnToHomeButton);

			Stage stage = new Stage();
			stage.setTitle("Delete Community Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Action method when the nextButton is pressed.
	 * Shows the next block of posts researched.
	 */
	@FXML
	void goNextPage(ActionEvent event) {
		//clear GridPane and videogames list
		postGridPane.getChildren().clear();
		posts.clear();
		//update the skipcounter
		skipCounter += skip;

		posts.addAll(getData());
		fillGridPane();
	}

	/**
	 * Action method when the nextButton is pressed.
	 * Shows the previous block of posts researched.
	 */
	@FXML
	void goPrevPage(ActionEvent event) {
		//clear GridPane and videogames list
		postGridPane.getChildren().clear();
		posts.clear();
		//update the skipcounter
		skipCounter -= skip;

		posts.addAll(getData());
		fillGridPane();
	}

	/**
	 * method that refresh the page
	 * @param event
	 */
	@FXML
	void refreshPage(ActionEvent event) {

		//clear GridPane and videogames list
		postGridPane.getChildren().clear();
		posts.clear();
		//update the skipcounter
		skipCounter = 0;

		initialize();
	}

	/**
	 * Method that allow the user to follow/unfollow the videogame community
	 * @param event
	 */
	@FXML
	void followCommunity(ActionEvent event) {
		boolean result;

		if (followButton.getText().equals("Follow")) {
			result = followedVideogameCache.followVideogameCommunity(community, true);
			if(result){
				followButton.setText("Unfollow");
			}
		}
		else if (followButton.getText().equals("Unfollow")){
			result = followedVideogameCache.followVideogameCommunity(community, false);
			if(result){
				followButton.setText("Follow");
			}
		}
	}

	/**
	 * method that allow the user to open the write post page
	 * @param event
	 */
	@FXML
	void writePost(ActionEvent event) {
		try {
			//OPEN CREATE POST PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCreatePostPage.fxml"));
			Parent root1 = fxmlLoader.load();

			UserCreatePostPageController controller = fxmlLoader.getController();
			controller.sendCommunity(community, user, refreshButton);

			Stage stage = new Stage();
			stage.setTitle("Create Post Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();


		}
		catch (Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * method that let a user do the return to the home page
	 * @param event
	 */
	@FXML
	void returnToHome(ActionEvent event) {
		try {

			FXMLLoader fxmlLoader;
			Parent root1;

			if(admin == null){
				fxmlLoader = new FXMLLoader(getClass().getResource("userHome.fxml"));
				root1 = fxmlLoader.load();
				UserHomeController controller2 = fxmlLoader.getController();
				controller2.sendUser(user.getUsername());
			}
			else{
				fxmlLoader = new FXMLLoader(getClass().getResource("adminHome.fxml"));
				root1 = fxmlLoader.load();
				AdminHomeController controller2 = fxmlLoader.getController();
				controller2.sendAdmin(admin.getUsername());
			}


			//send user id
			Stage stage = new Stage();
			if(admin == null) {
				stage.setTitle("Gameflows User Home");
			}
			else{
				stage.setTitle("Gameflows Admin Home");
			}
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

			//CLOSE COMMUNITY PAGE
			Stage stage1 = (Stage) returnToHomeButton.getScene().getWindow();
			stage1.close();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * method that set how many post to display in a row
	 * @param listDimension post list dimension
	 */
	void setGridPaneColumnAndRow(int listDimension){

		columnGridPane = 0;
		rowGridPane = 1;
	}

	/**
	 * method called in all methods that need to creat videogames item to put in the GridPane
	 */
	@FXML
	void fillGridPane() {

		//OPEN USER POST PAGE CLICKING ON A POST ITEM- if click on a post open its post page
		//listener to see if a post is selected (then open its post page)
		PostListener postListener = new PostListener() {
			@Override
			public void onClickPost(javafx.scene.input.MouseEvent event, Post post) {
				try {
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userPostPage.fxml"));
					Parent root1 = fxmlLoader.load();

					UserPostPageController controller2 = fxmlLoader.getController();
					controller2.sendPost(community, post, user, admin, likedPosts);

					Stage stage = new Stage();
					stage.setTitle("Post page");
					stage.setScene(new Scene(root1));
					stage.setResizable(false);
					stage.show();

					Stage stage1 = (Stage) postGridPane.getScene().getWindow();
					stage1.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		columnGridPane = 0;
		rowGridPane = 0;

		setGridPaneColumnAndRow(posts.size());
		try {
			for (Post post : posts) {
				FXMLLoader fxmlLoader = new FXMLLoader();
				fxmlLoader.setLocation(getClass().getResource("itemPost.fxml"));
				AnchorPane anchorPane = fxmlLoader.load();

				ItemPostController itemController = fxmlLoader.getController();
				itemController.setData(community, post, postListener,likedPosts,user, admin);

				if (columnGridPane == 1) {
					columnGridPane = 0;
					rowGridPane++;
				}
				//put a post item in the Pane
				postGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)

				//DISPLAY SETTINGS
				//set grid width
				postGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
				postGridPane.setPrefWidth(430);
				postGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
				//set grid height
				postGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
				postGridPane.setPrefHeight(71);
				postGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
				GridPane.setMargin(anchorPane, new Insets(10));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * method that retrieve all the liked post of the user
	 * @return post list
	 */
	private LikedPostsCache getLikedPosts () {
		if (admin != null) {
			return null;
		}

		return new LikedPostsCache(user.getUsername(), community.getId());
	}

}
