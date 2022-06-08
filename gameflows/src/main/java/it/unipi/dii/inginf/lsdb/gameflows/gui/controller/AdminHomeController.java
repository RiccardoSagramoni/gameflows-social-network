package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.*;
import it.unipi.dii.inginf.lsdb.gameflows.comment.ResultAverageCommentPerPost;
import it.unipi.dii.inginf.lsdb.gameflows.gui.GameflowsApplication;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.*;
import it.unipi.dii.inginf.lsdb.gameflows.gui.model.*;
import it.unipi.dii.inginf.lsdb.gameflows.post.*;
import it.unipi.dii.inginf.lsdb.gameflows.user.*;
import it.unipi.dii.inginf.lsdb.gameflows.comment.*;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AdminHomeController {

	@FXML
	private GridPane adminGridPane;

	@FXML
	private RadioButton communitiesSearchRadioButton;

	@FXML
	private AnchorPane dateAnchorPane;

	@FXML
	private Button datesConfirmButton;

	@FXML
	private DatePicker fromDatePicker;

	@FXML
	private Button logoutButton;

	@FXML
	private Label mostActiveErrorLabel;

	@FXML
	private Button nextButton;

	@FXML
	private Button prevButton;

	@FXML
	private Button mostActiveCommunitiesButton;

	@FXML
	private Button allCommunitiesButton;

	@FXML
	private Button newAdminButton;

	@FXML
	private Button allUsersButton;

	@FXML
	private Button viewInfluencerRankingButton;

	@FXML
	private Button newVideogameCommunityButton;

	@FXML
	private Button viewAverageNumberOfCommentsForCommunitiesButton;

	@FXML
	private Button viewAverageNumberOfCommentsForUsersButton;

	@FXML
	private TextField searchCommunityAndUsersField;

	@FXML
	private DatePicker toDatePicker;

	@FXML
	private Label userLabel;

	@FXML
	private RadioButton usersSearchRadioButton;

	@FXML
	private Label searchErrorLabel;

	@FXML
	private ImageView loadImage;

	//VIDEOGAMES VARIABLES
	private final VideogameCommunityService videogameService = VideogameCommunityServiceFactory.getService();
	private final List<VideogameCommunity> videogames = new ArrayList<>();
	List<ResultAverageCommentPerPost> communityAverageCommentPerPosts = new ArrayList<>();
	List<ResultAverageCommentPerUser> userAverageCommentPerPosts = new ArrayList<>();
	private List<ResultBestVideogameCommunityAggregation> resultAggregationList = null;

	// POST VARIABLES
	private final PostService postService = PostServiceFactory.getService();

	// COMMENT SERVICE
	private final CommentService commentService = CommentServiceFactory.getService();

	// USER VARIABLES
	private final UserService userService = UserServiceFactory.getService();
	private final List<User> users = new ArrayList<>();

	// ADMIN
	private final AdminService adminService = AdminServiceFactory.getInstance();
	private Admin admin = null;

	// VARIABLE TO HANDLE THE PAGE
	private int skipCounter = 0;
	private final int skip = 20; //how many videogame to skip per time
	private final int limit = 20; //how many videogame community to show for each page
	private boolean usersSearchRadioButtonFlag = false;
	private boolean communitiesSearchRadioButtonFlag = false;
	private int changePageFlag = 0;
	private boolean toLoadItemVideogameCommunityWithStats = false;
	private boolean toLoadItemUsersWithStats = false;
	private int columnGridPane = 0;
	private int rowGridPane = 0;

	//VARIABLES FOR SEARCH COMMUNITIES BY NAME
	private String match;
	private AdminResearchMode choiceMode;

	//VARIABLES FOR DATE PICKER
	private Date fromDate;
	private Date toDate;

	/**
	 * this method set the variable admin of the class and insert the username on the page
	 *
	 * @param username of the admin
	 */
	public void sendAdmin(String username) {
		if (admin == null || !admin.getUsername().equals(username)) {
			admin = adminService.find(username);
			userLabel.setText(admin.getUsername());
		}
	}

	/******************************************************************************************************************/
	/******************************** General Methods used both for comunities and user *******************************/
	/******************************************************************************************************************/

	/***
	 * method that intialize the interface and call the methods to retrieve the first data from the db
	 */
	public void initialize() {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);
		loadImage.setVisible(false);
		searchErrorLabel.setVisible(false);

		//clear variables and choice the search mode
		nextButton.setDisable(false);
		prevButton.setDisable(true);
		choiceMode = AdminResearchMode.allCommunities;

		//get videogame community results
		videogames.addAll(getVideogamesData());
		//put all videogame communities in the Pane
		fillWithVideogameGridPane();

		selectSidebarButton(allCommunitiesButton);
	}

	/**
	 * method that set the search to be applied only to users by setting a specific variable
	 *
	 * @param event of the mouse when there is an interaction with the radio button
	 */
	@FXML
	void chooseUserSearch(ActionEvent event) {
		usersSearchRadioButtonFlag = true;
		usersSearchRadioButton.setSelected(true);
		if(communitiesSearchRadioButtonFlag){
			communitiesSearchRadioButton.setSelected(false);
			communitiesSearchRadioButtonFlag = false;
		}
	}

	/**
	 * method that set the search to be applied only to users by setting a specific variable
	 *
	 * @param event of the mouse when there is an interaction with the radio button
	 */
	@FXML
	void chooseVideogameCommunitiesSearch(ActionEvent event) {
		communitiesSearchRadioButtonFlag = true;
		communitiesSearchRadioButton.setSelected(true);
		if(usersSearchRadioButtonFlag){
			usersSearchRadioButton.setSelected(false);
			usersSearchRadioButtonFlag = false;
		}
	}

	/**
	 * method that search the usernames of the users or the names of the community that is typed in the text area
	 *
	 * @param event of the mouse when the button search is clicked
	 */
	@FXML
	void search(ActionEvent event) {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		match = searchCommunityAndUsersField.getText();

		if(usersSearchRadioButtonFlag){
			choiceMode = AdminResearchMode.searchUsers;
			skipCounter = 0;
			adminGridPane.getChildren().clear();
			users.clear();
			searchErrorLabel.setVisible(false);

			//get users results
			users.addAll(getUsersData(match));

			//put all users  in the Pane
			fillWithUserGridPane();
		}
		else if(communitiesSearchRadioButtonFlag){
			choiceMode = AdminResearchMode.searchCommunities;
			skipCounter = 0;
			adminGridPane.getChildren().clear();
			videogames.clear();
			searchErrorLabel.setVisible(false);

			//get videogame community results
			videogames.addAll(getVideogamesData(match));

			//put all videogame communities in the Pane
			fillWithVideogameGridPane();

		}
		else{
			searchErrorLabel.setVisible(true);
		}

		resetSidebarButtons();
	}

	/**
	 * method to check if the next or the prev button should be disabled according to
	 * the dimension of the list of the data retrieved from the db, and if yes it disable them
	 *
	 * @param list of the data retrieved from the db
	 */
	private void prevNextButtonsCheck(List list) {
		if ((list.size() > 0)) {

			if ((list.size() < limit)) {
				if (skipCounter <= 0) {
					prevButton.setDisable(true);
					nextButton.setDisable(true);
				} else {
					prevButton.setDisable(false);
					nextButton.setDisable(true);
				}
			} else {
				if (skipCounter <= 0) {
					prevButton.setDisable(true);
					nextButton.setDisable(false);
				} else {
					prevButton.setDisable(false);
					nextButton.setDisable(false);
				}
			}
		} else {
			if (skipCounter <= 0) {
				prevButton.setDisable(true);
				nextButton.setDisable(true);
			} else {
				prevButton.setDisable(false);
				nextButton.setDisable(true);
			}
		}
	}

	/**
	 * method that disable the button for change page
	 */
	private void disablePrevNextButton(){
		prevButton.setDisable(true);
		nextButton.setDisable(true);
	}

	/**
	 * method that change the data in the page when we click on the previous or the next button
	 *
	 * @param changePageFlag is an int that specifies if we have to go to the next or the previous page
	 */
	private void changePage(int changePageFlag){
		// clear variables
		videogames.clear();
		users.clear();
		communityAverageCommentPerPosts.clear();
		adminGridPane.getChildren().clear();
		userAverageCommentPerPosts.clear();

		//update the skipcounter
		if(changePageFlag == -1){
			skipCounter -= skip;
		}
		else if(changePageFlag == 1){
			skipCounter += skip;
		}
		else{
			skipCounter = skip;
		}


		//choice the search mode
		switch (choiceMode){
			case searchCommunities:
				videogames.addAll(getVideogamesData(match));
				break;

			case searchUsers:
				users.addAll(getUsersData(match));
				break;

			case allCommunities:
				videogames.addAll(getVideogamesData());
				break;

			case communityWithAverageNumberOfCommentPerPost:
				getResultAverageCommentPerPostData();
				break;

			case usersWithAverageNumberOfCommentPerPost:
				getResultAverageCommentPerPostOfUsersData();
				break;

			case allUsers:
				users.addAll(getUsersData());
				break;
		}

		if(videogames.isEmpty() && communityAverageCommentPerPosts.isEmpty()){
			fillWithUserGridPane();
		}
		else{
			fillWithVideogameGridPane();
		}
	}

	/**
	 * method that specifies that we have to go on the next page
	 *
	 * @param event of the mouse when the button next page is clicked
	 */
	@FXML
	void goNextPage(ActionEvent event) {

		changePageFlag = 1;
		changePage(changePageFlag);
	}

	/**
	 * method that specifies that we have to go on the previous page
	 *
	 * @param event of the mouse when the button previous page is clicked
	 */
	@FXML
	void goPrevPage(ActionEvent event) {

		changePageFlag = -1;
		changePage(changePageFlag);
	}

	/**
	 * method that set the starting right value of column and row of the grid pain
	 * the values depend on the list size of object retrieved from the db
	 *
	 * @param listDimension is the number of the object retrieved from the db
	 */
	private void setGridPaneColumnAndRow(int listDimension){
		if(listDimension <= 3){
			columnGridPane = 0;
			rowGridPane = 0;
		}
		else{
			columnGridPane = 0;
			rowGridPane = 1;
		}
	}

	/**
	 * method that execute the logout of the admin
	 *
	 * @param event of the mouse when the logout previous page is clicked
	 */
	@FXML
	void logout(ActionEvent event) {
		try {
			//OPEN LOGIN PAGE
			Stage stage = new Stage();
			FXMLLoader fxmlLoader = new FXMLLoader(GameflowsApplication.class.getResource("gameflows_login.fxml"));
			Scene scene = new Scene(fxmlLoader.load());
			stage.setTitle("Welcome to Gameflows App");
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();

			//CLOSE USER HOME PAGE
			Stage stage1 = (Stage) logoutButton.getScene().getWindow();
			stage1.close();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/******************************************************************************************************************/
	/******************************************** User Environment Methods ********************************************/
	/******************************************************************************************************************/

	/**
	 * methods that retrieve the users from the db,
	 * used for the research where we match the string typed by the admin
	 *
	 * @param match is a string with the username or a part of it typed by the admin
	 * @return a list of user object that satisfy the match of the search
	 */
	private List<User> getUsersData(String match) {

		List<User> usersSearched= userService.search(match, skipCounter, limit);
		prevNextButtonsCheck(usersSearched);
		return usersSearched;
	}

	/**
	 * methods that retrieve users from the db,
	 * in accordance with the admin's decision on what to visualize
	 *
	 * @return a list of user object that satisfy the choice of the admin
	 */
	private List<User> getUsersData(){

		List<User> userData = new ArrayList<>();
		switch (choiceMode){
			case searchUsers:
				break;
			case allUsers:
				userData = userService.browse(skipCounter,limit);
				break;
			default:
				userData = new ArrayList<>();
				break;
		}
		prevNextButtonsCheck(userData);
		return userData;
	}

	/**
	 * method that create a new account for a new admin
	 *
	 * @param event of the mouse when the create new button is clicked
	 */
	@FXML
	void createNewAdminAccount(ActionEvent event) {
		try {
			//OPEN SIGN UP PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("createNewAdmin.fxml"));
			Parent root1 = fxmlLoader.load();

			Stage stage = new Stage();
			stage.setTitle("Create New Admin Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * method that load all the users from the db
	 *
	 * @param event of the mouse when the view all users is clicked
	 */
	@FXML
	void viewAllUsers(ActionEvent event) {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		choiceMode = AdminResearchMode.allUsers;
		skipCounter = 0;
		adminGridPane.getChildren().clear();
		users.clear();

		//get videogame community results
		users.addAll(getUsersData());

		//put all videogame communities in the Pane
		fillWithUserGridPane();

		selectSidebarButton(allUsersButton);
	}

	/**
	 * method that open a new page that will show the ranking of the most active,
	 * followed and with more interaction users
	 *
	 * @param event of the mouse when the view influencer button is clicked
	 * @throws IOException
	 */
	@FXML
	void viewInfluencerRanking(ActionEvent event) throws IOException{
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("viewInfluencerRanking.fxml"));
		Parent root1 = fxmlLoader.load();

		ViewInfluencerRankingController controller2 = fxmlLoader.getController();
		// chidere sta cosa qua come funziona il send per riciclare delle pagine
		controller2.sendInfluencerRequest(admin);

		Stage stage = new Stage();
		stage.setTitle("Influencer Ranking Page");
		stage.setScene(new Scene(root1));
		stage.setResizable(false);
		stage.show();

		Stage stage1 = (Stage) adminGridPane.getScene().getWindow();
		stage1.close();
	}

	/**
	 * method that retrieve the average comment per post of every community
	 * and store those information into a list of ResultAverageCommentPerPost
	 */
	private void getResultAverageCommentPerPostOfUsersData(){

		// before long task
		// made visible the  loading gif
		loadImage.setVisible(true);

		Task<Void> task = new Task<>() {
			@Override
			public Void call() {
				// long task
				disablePrevNextButton();
				userAverageCommentPerPosts = commentService.averageNumberOfCommentsPerUser(skipCounter, limit);
				return null ;
			}
		};

		task.setOnSucceeded(e -> {
			// after long task
			// made invisible the  loading gif
			loadImage.setVisible(false);

			//put all videogame communities in the Pane
			prevNextButtonsCheck(userAverageCommentPerPosts);
			toLoadItemUsersWithStats = true;
			fillWithUserGridPane();
		});

		new Thread(task).start();


	}

	//averageNumberOfCommentsPerUser()
	/**
	 * method that visualize the average number of comments per post of every
	 * user in the db
	 *
	 * @param event of the mouse when the view community average number of comment per post
	 *              for every user button is clicked
	 */
	@FXML
	void viewUsersAverageNumberOfCommentsPerPost(ActionEvent event) {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);
		videogames.clear();
		users.clear();

		choiceMode = AdminResearchMode.usersWithAverageNumberOfCommentPerPost;
		skipCounter = 0;
		adminGridPane.getChildren().clear();

		toLoadItemUsersWithStats = true;
		getResultAverageCommentPerPostOfUsersData();

		selectSidebarButton(viewAverageNumberOfCommentsForUsersButton);
	}

	/**
	 * method that fill the grid pain of the interface with the item User retrieved from the database
	 * all the item are clickable and by clicking one of them, the details of the user will be shown
	 */
	@FXML
	void fillWithUserGridPane() {
		//OPEN USER COMMUNITY PAGE CLICKING ON A VIDEOGAME ITEM (if click on a community open its community page)
		UserListener userlistener = new UserListener() {
			@Override
			public void onClickListener(javafx.scene.input.MouseEvent event, User user) {
				try {
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminUserPage.fxml"));
					Parent root1 = fxmlLoader.load();

					AdminUserInfoPageController controller2 = fxmlLoader.getController();
					controller2.viewUserInfo(admin, user);

					Stage stage = new Stage();
					stage.setTitle("Gameflows User Page");
					stage.setScene(new Scene(root1));
					stage.setResizable(false);
					stage.show();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		columnGridPane = 0;
		rowGridPane = 0;
		FXMLLoader fxmlLoader;
		if (toLoadItemUsersWithStats){
			setGridPaneColumnAndRow(userAverageCommentPerPosts.size());
			//CREATE FOR EACH VIDEOGAME COMMUNITY AN ITEM (ItemVideogame)
			try {
				for (ResultAverageCommentPerUser userWithStats : userAverageCommentPerPosts) {
					fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(getClass().getResource("ItemUserWithStats.fxml"));
					AnchorPane anchorPane = fxmlLoader.load();

					ItemUserWithStatsController itemController = fxmlLoader.getController();
					itemController.setDataAverageCommentPerPost(userWithStats, userlistener);
					//choice number of column
					if (columnGridPane == 1) {
						columnGridPane = 0;
						rowGridPane++;
					}
					adminGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
					//DISPLAY SETTINGS
					GridPane.setMargin(anchorPane, new Insets(10));
					toLoadItemUsersWithStats = false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			setGridPaneColumnAndRow(users.size());

			//CREATE FOR EACH VIDEOGAME COMMUNITY AN ITEM (ItemVideogame)
			try {
				for (User user : users) {
					fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(getClass().getResource("itemUser.fxml"));
					AnchorPane anchorPane = fxmlLoader.load();

					ItemUserController itemController = fxmlLoader.getController();
					itemController.setData(user, userlistener);


					//choice number of column
					if (columnGridPane == 3) {
						columnGridPane = 0;
						rowGridPane++;
					}
					adminGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)

					//DISPLAY SETTINGS
					//set grid width
					adminGridPane.setPrefWidth(745);
					//set grid height
					adminGridPane.setPrefHeight(460);

					GridPane.setMargin(anchorPane, new Insets(10));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/******************************************************************************************************************/
	/*********************************** videogame communities environment methods ************************************/
	/******************************************************************************************************************/


	/**
	 * methods that retrieve the communities from the db,
	 * used for the research where we match the string typed by the admin
	 *
	 * @param match is a string with the name of the community or a part of it typed by the admin
	 * @return a list of community object that satisfy the match of the search
	 */
	private List<VideogameCommunity> getVideogamesData(String match) {
		List<VideogameCommunity> communities = videogameService.search(match, skipCounter, limit);
		prevNextButtonsCheck(communities);

		return communities;
	}

	/**
	 * method that retrieve communities from the db,
	 * in accordance with the admin's decision on what to visualize
	 *
	 * @return a list of user object that satisfy the choice of the admin
	 */
	private List<VideogameCommunity> getVideogamesData(){
		List<VideogameCommunity> communities = new ArrayList<>();
		resultAggregationList = null;
		switch (choiceMode){
			case searchCommunities:
				break;

			case allCommunities:
				communities = videogameService.browse(skipCounter,limit);
				break;

			default:
				communities = new ArrayList<>();
				break;
		}
		prevNextButtonsCheck(communities);
		return communities;
	}

	/**
	 * method that retrieve the average comment per post of every community
	 * and store those information into a list of ResultAverageCommentPerPost
	 */
	private void getResultAverageCommentPerPostData(){

		// before long task
		// made visible the  loading gif
		loadImage.setVisible(true);

		Task<Void> task = new Task<>() {
			@Override
			public Void call() {
				// long task
				disablePrevNextButton();
				communityAverageCommentPerPosts = commentService.averageNumberOfCommentsPerPost(skipCounter, limit);
				return null ;
			}
		};

		task.setOnSucceeded(e -> {
			// after long task
			// made invisible the  loading gif
			loadImage.setVisible(false);

			//put all videogame communities in the Pane
			prevNextButtonsCheck(communityAverageCommentPerPosts);
			toLoadItemVideogameCommunityWithStats = true;
			fillWithVideogameGridPane();
		});

		new Thread(task).start();


	}

	/**
	 * method that create open a new window, where the admin can create a new community
	 *
	 * @param event of the mouse when the create new community button is clicked
	 */
	@FXML
	void createNewVideogameCommunity(ActionEvent event){
		try {
			//OPEN SIGN UP PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("createNewVideogameCommunity.fxml"));
			Parent root1 = fxmlLoader.load();

			Stage stage = new Stage();
			stage.setTitle("Create New Videogame Community Page");
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * method that visualize all the communities in the db
	 *
	 * @param event of the mouse when the view all communities button is clicked
	 */
	@FXML
	void viewAllCommunities(ActionEvent event) {

		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		choiceMode = AdminResearchMode.allCommunities;
		skipCounter = 0;
		adminGridPane.getChildren().clear();
		videogames.clear();

		//get videogame community results
		videogames.addAll(getVideogamesData());

		//put all videogame communities in the Pane
		fillWithVideogameGridPane();

		selectSidebarButton(allCommunitiesButton);
	}

	/**
	 * method that make visible the date picker and the button that are necessary for
	 * retrieve the data from the db
	 *
	 * @param event of the mouse when the view most active communities button is clicked
	 */
	@FXML
	void viewMostActiveCommunities (ActionEvent event) {
		//clear variables
		fromDate = null;
		toDate = null;

		boolean visibility = !dateAnchorPane.isVisible();
		fromDatePicker.setDisable(!visibility);
		toDatePicker.setDisable(!visibility);
		datesConfirmButton.setDisable(!visibility);

		//make visible the date picker (let the user choice)
		dateAnchorPane.setVisible(visibility);

		selectSidebarButton(mostActiveCommunitiesButton);
	}

	/**
	 * method that convert in date type the two values in the two date picker used by the admin
	 */
	private void pickDate() {
		if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
			fromDate = Date.from((fromDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
			toDate = Date.from((toDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
	}

	/**
	 * method that visualize all the most active communities in a range of time
	 * chosen by the admin through the two date picker
	 *
	 * @param event of the mouse when the confirm dates button is clicked
	 */
	@FXML
	void launchResearchMostActiveCommunities (ActionEvent event) {

		pickDate();
		choiceMode = AdminResearchMode.mostActiveCommunities;

		//choice the search mode
		if(fromDate == null || toDate == null) {
			//error message
			mostActiveErrorLabel.setTextFill(Color.RED);
			mostActiveErrorLabel.setText("Error! Some Dates are Missing!");
		}
		else {
			if(fromDate.after(toDate)){
				mostActiveErrorLabel.setTextFill(Color.RED);
				mostActiveErrorLabel.setText("Error! This is not a time machine!");
			}
			else if(fromDate.equals(toDate)){
				mostActiveErrorLabel.setTextFill(Color.RED);
				mostActiveErrorLabel.setText("Error! Same Day Selected!");
			}
			else {
				//clear error message
				mostActiveErrorLabel.setText("");
				mostActiveErrorLabel.setTextFill(Color.LIGHTGREEN);
				mostActiveErrorLabel.setText("Research Successfully Executed!");

				//clear variables
				skipCounter = 0;

				adminGridPane.getChildren().clear();
				videogames.clear();


				// before long task
				// makes visible the loading gif
				loadImage.setVisible(true);
				Task<Void> task = new Task<>() {
					@Override
					public Void call() {
						// long task
						// get videogame community results
						resultAggregationList = postService.bestVideogameCommunities(fromDate, toDate, limit);
						choiceMode = AdminResearchMode.mostActiveCommunities;
						disablePrevNextButton();

						return null ;
					}
				};
				task.setOnSucceeded(e -> {
					// after long task
					// made invisible the loading gif
					loadImage.setVisible(false);
					//put all videogame communities in the Pane
					toLoadItemVideogameCommunityWithStats = true;
					fillWithVideogameGridPane();
				});
				new Thread(task).start();

			}
		}
	}

	/**
	 * method that visualize the average number of comments per post of every
	 * community in the db
	 *
	 * @param event of the mouse when the view community average number of comment per post
	 *              in every communities button is clicked
	 */
	@FXML
	void viewCommunityAverageNumberOfCommentsPerPost(ActionEvent event) {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);
		videogames.clear();
		users.clear();

		choiceMode = AdminResearchMode.communityWithAverageNumberOfCommentPerPost;
		skipCounter = 0;
		adminGridPane.getChildren().clear();

		toLoadItemVideogameCommunityWithStats = true;
		getResultAverageCommentPerPostData();

		selectSidebarButton(viewAverageNumberOfCommentsForCommunitiesButton);
	}

	/**
	 * method that fill the grid pain of the interface with items of the community retrieved from the database
	 * all the item are clickable and by clicking one of them, the page of the community is shown
	 * there are two type of item for the community that are shown in this page,
	 * one containing the logo and the name of the community only and the other one containing
	 * logo, name and average number of comments per post of the community for the relative method
	 */
	@FXML
	void fillWithVideogameGridPane() {
		//OPEN USER COMMUNITY PAGE CLICKING ON A VIDEOGAME ITEM (if click on a community open its community page)
		// chidere sta cosa qua come funziona il send per riciclare delle pagine
		VideogameListener videogameListener = new VideogameListener() {
			@Override
			public void onClickListener(javafx.scene.input.MouseEvent event, VideogameCommunity community) {
				try {
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCommunityPage.fxml"));
					Parent root1 = fxmlLoader.load();

					UserCommunityPageController controller2 = fxmlLoader.getController();
					controller2.sendVideogameCommunity(community, null, admin);

					Stage stage = new Stage();
					stage.setTitle("Gameflows Community Page");
					stage.setScene(new Scene(root1));
					stage.setResizable(false);
					stage.show();

					Stage stage1 = (Stage) adminGridPane.getScene().getWindow();
					stage1.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		columnGridPane = 0;
		rowGridPane = 0;
		FXMLLoader fxmlLoader;
		if (toLoadItemVideogameCommunityWithStats){
			int dimensionList = 0;
			//CREATE FOR EACH VIDEOGAME COMMUNITY AN ITEM (ItemVideogame)
			try {
				if(choiceMode == AdminResearchMode.mostActiveCommunities) {
					dimensionList = resultAggregationList.size();
				}
				else{
					dimensionList = communityAverageCommentPerPosts.size();
				}
				setGridPaneColumnAndRow(dimensionList);
				for (int i = 0; i < dimensionList; i++) {
					fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(getClass().getResource("itemVideogameWithStats.fxml"));
					AnchorPane anchorPane = fxmlLoader.load();
					ItemVideogameWithStatsController itemController = fxmlLoader.getController();

					if(choiceMode == AdminResearchMode.mostActiveCommunities) {
						ResultBestVideogameCommunityAggregation videogameWithStats = resultAggregationList.get(i);
						itemController.setDataBestCommunity(videogameWithStats, videogameListener);
					}
					else{
						ResultAverageCommentPerPost community = communityAverageCommentPerPosts.get(i);
						itemController.setDataAverageCommentPerPost(community, videogameListener);
					}

					//choice number of column
					if (columnGridPane == 1) {
						columnGridPane = 0;
						rowGridPane++;
					}

					adminGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)

					//DISPLAY SETTINGS
					//set grid width
					adminGridPane.setPrefWidth(745);
					//set grid height
					adminGridPane.setPrefHeight(460);
					GridPane.setMargin(anchorPane, new Insets(10));
					toLoadItemVideogameCommunityWithStats = false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			//CREATE FOR EACH VIDEOGAME COMMUNITY AN ITEM (ItemVideogame)
			try {
				setGridPaneColumnAndRow(videogames.size());
				for (int i = 0; i < videogames.size(); i++) {
					VideogameCommunity videogame = videogames.get(i);

					fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(getClass().getResource("itemVideogame.fxml"));
					AnchorPane anchorPane = fxmlLoader.load();

					ItemVideogameController itemController = fxmlLoader.getController();
					itemController.setData(videogame, videogameListener, choiceMode != AdminResearchMode.allCommunities);

					//choice number of column
					if (columnGridPane == 3) {
						columnGridPane = 0;
						rowGridPane++;
					}

					adminGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)

					//DISPLAY SETTINGS
					//set grid width
					adminGridPane.setPrefWidth(745);
					//set grid height
					adminGridPane.setPrefHeight(460);
					GridPane.setMargin(anchorPane, new Insets(10));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void resetSidebarButtons () {
		allCommunitiesButton.getStyleClass().remove("selected-button");
		mostActiveCommunitiesButton.getStyleClass().remove("selected-button");
		newAdminButton.getStyleClass().remove("selected-button");
		allUsersButton.getStyleClass().remove("selected-button");
		viewInfluencerRankingButton.getStyleClass().remove("selected-button");
		newVideogameCommunityButton.getStyleClass().remove("selected-button");
		viewAverageNumberOfCommentsForCommunitiesButton.getStyleClass().remove("selected-button");
		viewAverageNumberOfCommentsForUsersButton.getStyleClass().remove("selected-button");

	}

	private void selectSidebarButton (Button button) {
		resetSidebarButtons();
		button.getStyleClass().add("selected-button");
	}


}
