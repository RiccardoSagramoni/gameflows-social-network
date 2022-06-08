package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.gui.GameflowsApplication;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.VideogameListener;
import it.unipi.dii.inginf.lsdb.gameflows.gui.model.AdminResearchMode;
import it.unipi.dii.inginf.lsdb.gameflows.gui.model.HomeResearchMode;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostService;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.post.ResultBestVideogameCommunityAggregation;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserService;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.FollowedVideogameCache;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityService;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityServiceFactory;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserHomeController{

	//Videogame Controller
	@FXML
	private ImageView logo;
	@FXML
	private Label name;

	//User Home Controller
	@FXML
	private Button nextButton, prevButton;
	@FXML
	private AnchorPane videogameAnchorPane;
	@FXML
	private GridPane videogameGridPane;
	@FXML
	private HBox videogameHBox;
	@FXML
	private ScrollPane videogameScrollPane;
	@FXML
	private Button searchCommunityButton;
	@FXML
	private TextField searchCommunityField;
	@FXML
	private Button allCommunitiesButton;
	@FXML
	private Button followedCommunitiesButton;
	@FXML
	private Button mostActiveCommunitiesButton;
	@FXML
	private Button suggestedCommunitiesButton;
	@FXML
	private Label userLabel;
	@FXML
	private DatePicker fromDatePicker;
	@FXML
	private DatePicker toDatePicker;
	@FXML
	private Button datesConfirmButton;
	@FXML
	private AnchorPane dateAnchorPane;
	@FXML
	private Button logoutButton;
	@FXML
	private ImageView loadImage;
	@FXML
	private Label mostActiveErrorLabel;


	//VIDEOGAMES VARIABLES
	private final VideogameCommunityService videogameService = VideogameCommunityServiceFactory.getService();
	private final List<VideogameCommunity> videogames = new ArrayList<>();
	private List<ResultBestVideogameCommunityAggregation> resultAggregationList = null;
	private VideogameListener videogameListener;

	//POST VARIABLES
	private final PostService postService = PostServiceFactory.getService();

	//USER VARIABLES
	private final UserService userService = UserServiceFactory.getService();
	private User user = null;

	//VARIABLES
	private int skipCounter = 0;
	private final static int SKIP = 20; //how many videogame to skip per time
	private final static int LIMIT = 20; //how many videogame community to show for each page

	//VARIABLES FOR SEARCH COMMUNITIES BY NAME
	private String match;
	private HomeResearchMode choiceMode;

	//VARIABLES FOR DATE PICKER
	private Date fromDate;
	private Date toDate;


	/**
	 * this method set the variable user of the class and insert the username on the page
	 *
	 * @param username of the user
	 */
	public void sendUser(String username){
		if (user == null || !user.getUsername().equals(username)) {
			user = userService.find(username);
			userLabel.setText(user.getUsername());
		}
	}

	/**
	 * methods that retrieve the communities from the db,
	 * used for the research where we match the string typed by the user
	 *
	 * @param match is a string with the name of the community or a part of it typed by the user
	 * @return a list of community object that satisfy the match of the search
	 */
	private List<VideogameCommunity> getData(String match){
		List<VideogameCommunity> communities = videogameService.search(match,skipCounter, LIMIT);

		prevNextButtonsCheck(communities);
		return communities;
	}

	/**
	 * methods that retrieve the communities from the db.
	 * it makes different research by using choiceMode
	 *
	 * @return a list of community object
	 */
	private List<VideogameCommunity> getData(){
		List<VideogameCommunity> communities = new ArrayList<>();
		resultAggregationList = null;

		switch (choiceMode){
			case search:
				break;
			case all:
				communities = videogameService.browse(skipCounter, LIMIT);
				break;

			case followed:
				List<VideogameCommunity> followedCommunity =
						FollowedVideogameCache.getInstance(user).getList();

				for (int i = skipCounter; (i < (skipCounter + LIMIT)) && (i < followedCommunity.size()); i++) {
					communities.add(
							new VideogameCommunity(
									followedCommunity.get(i).getId(),
									followedCommunity.get(i).getName()
							)
					);
				}
				break;

			case mostActive:
				resultAggregationList =
						postService.bestVideogameCommunities(fromDate, toDate, LIMIT);

				for (var videogameObject : resultAggregationList) {
					communities.add(
							new VideogameCommunity(
									videogameObject.getVideogameCommunityId(),
									videogameObject.getVideogameCommunityName()
							)
					);
				}
				break;

			case suggested:
				List<VideogameCommunity> suggestedResult =
						videogameService.viewSuggestedVideogameCommunities(user, LIMIT);

				for (var videogameObject : suggestedResult) {
					communities.add(
							new VideogameCommunity(
									videogameObject.getId(),
									videogameObject.getName()
							)
					);
				}
				break;

			default:
				communities = new ArrayList<>();
				break;
		}
		prevNextButtonsCheck(communities);
		return communities;
	}

	/**
	 * method to check if the next or the prev button should be disabled according to
	 * the dimension of the list of the data retrieved from the db, and if yes it disable them
	 *
	 * @param communities of the data retrieved from the db
	 */
	void prevNextButtonsCheck(List<VideogameCommunity> communities){
		if((communities.size() > 0)){
			if((communities.size() < LIMIT)){
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
	public void initialize(){
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);
		loadImage.setVisible(false);

		//clear variables and choice the search mode
		nextButton.setDisable(true);
		prevButton.setDisable(true);
		choiceMode = HomeResearchMode.all;

		//get videogame community results
		videogames.addAll(getData());
		//put all videogame communities in the Pane
		fillGridPane();

		selectSidebarButton(allCommunitiesButton);
	}

	/**
	 * method that disable the button for change page
	 */
	private void disablePrevNextButton(){
		prevButton.setDisable(true);
		nextButton.setDisable(true);
	}

	/**
	 * Action method when searchButton is pressed. Research videogame communities by using string match method
	 */
	@FXML
	void searchCommunities() {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		match = searchCommunityField.getText();
		choiceMode = HomeResearchMode.search;
		skipCounter = 0;
		videogameGridPane.getChildren().clear();
		videogames.clear();

		//get videogame community results
		videogames.addAll(getData(match));

		//put all videogame communities in the Pane
		fillGridPane();

		resetSidebarButtons();
	}

	/**
	 * Method that allow the user to see all his followed videogame communities
	 */
	@FXML
	void viewFollowedCommunities() {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		choiceMode = HomeResearchMode.followed;
		skipCounter = 0;
		videogameGridPane.getChildren().clear();
		videogames.clear();
		disablePrevNextButton();

		// before long task
		// makes visible the loading gif
		loadImage.setVisible(true);
		Task<Void> task = new Task<>() {
			@Override
			public Void call() {
				// long task
				//get videogame community results
				videogames.addAll(getData());

				return null ;
			}
		};
		task.setOnSucceeded(e -> {
			// after long task
			// made invisible the loading gif
			loadImage.setVisible(false);
			//put all videogame communities in the Pane
			fillGridPane();
		});
		new Thread(task).start();


		selectSidebarButton(followedCommunitiesButton);
	}

	/**
	 * Method that allow the user to see all the videogame communities
	 */
	@FXML
	void viewAllCommunities() {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		choiceMode = HomeResearchMode.all;
		skipCounter = 0;
		videogameGridPane.getChildren().clear();
		videogames.clear();

		//get videogame community results
		videogames.addAll(getData());

		//put all videogame communities in the Pane
		fillGridPane();

		selectSidebarButton(allCommunitiesButton);
	}


	/**
	 * Method that allow the user to see all suggested videogame communities
	 */
	@FXML
	void viewSuggestedCommunities() {
		//disable mostActiveCommunity choice mode
		dateAnchorPane.setVisible(false);

		//clear variables and choice the search mode
		choiceMode = HomeResearchMode.suggested;
		skipCounter = 0;
		videogameGridPane.getChildren().clear();
		videogames.clear();
		disablePrevNextButton();

		// before long task
		// makes visible the loading gif
		loadImage.setVisible(true);
		Task<Void> task = new Task<>() {
			@Override
			public Void call() {
				// long task
				//get videogame community results
				videogames.addAll(getData());

				return null ;
			}
		};
		task.setOnSucceeded(e -> {
			// after long task
			// made invisible the loading gif
			loadImage.setVisible(false);
			disablePrevNextButton();
			//put all videogame communities in the Pane
			fillGridPane();
		});
		new Thread(task).start();

		selectSidebarButton(suggestedCommunitiesButton);
	}

	/**
	 * Method that allow the user to see the most active videogame communities.
	 * it enable the date picker to let the user choice the date interval
	 */
	@FXML
	void viewMostActiveCommunities() {
		//clear variables
		fromDate = null;
		toDate = null;

		boolean visibility = !dateAnchorPane.isVisible();
		fromDatePicker.setDisable(!visibility);
		toDatePicker.setDisable(!visibility);
		datesConfirmButton.setDisable(!visibility);
		mostActiveErrorLabel.setText("");

		//make visible the date picker (let the user choice)
		dateAnchorPane.setVisible(visibility);

		selectSidebarButton(mostActiveCommunitiesButton);
	}


	/**
	 * Method used in viewMostActiveCommunities.
	 * it let the user choose with a date picker and launch the research
	 */
	@FXML
	void launchResearchMostActiveCommunities(){
		pickDate();
		choiceMode = HomeResearchMode.mostActive;
		disablePrevNextButton();
		//choice the search mode
		if(fromDate == null || toDate == null) {
			//error message
			mostActiveErrorLabel.setText("");
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
				videogameGridPane.getChildren().clear();
				videogames.clear();

				// before long task
				// makes visible the loading gif
				loadImage.setVisible(true);
				Task<Void> task = new Task<>() {
					@Override
					public Void call() {
						// long task
						//get videogame community results
						resultAggregationList = postService.bestVideogameCommunities(fromDate, toDate, LIMIT);
						disablePrevNextButton();

						return null ;
					}
				};
				task.setOnSucceeded(e -> {
					// after long task
					// made invisible the loading gif
					loadImage.setVisible(false);//put all videogame communities in the Pane
					fillGridPane();
				});
				new Thread(task).start();




			}
		}
	}

	/**
	 * Method used in viewMostActiveCommunities.
	 * used to pick the interval dates.
	 */
	void pickDate() {
		if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
			fromDate = Date.from((fromDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
			toDate = Date.from((toDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
	}


	/**
	 * Action method when the nextButton is pressed.
	 * Shows the next block of videogame communities researched.
	 */
	@FXML
	void goNextPage() {
		//clear variables
		videogameGridPane.getChildren().clear();
		videogames.clear();

		//update the skipcounter
		skipCounter += SKIP;

		//choice the search mode
		switch (choiceMode){
			case search:
				videogames.addAll(getData(match));
				break;
			case all:
			case mostActive:
			case followed:
			case suggested:
				videogames.addAll(getData());
				break;
		}

		//put all videogame communities in the Pane
		fillGridPane();
	}

	/**
	 * Action method when the nextButton is pressed.
	 * Shows the previous block of videogame communities researched.
	 */
	@FXML
	void goPrevPage() {
		//clear variables
		videogameGridPane.getChildren().clear();
		videogames.clear();

		//update the skipcounter
		skipCounter -= SKIP;

		//choice the search mode
		switch (choiceMode){
			case search:
				videogames.addAll(getData(match));
				break;
			case all:
			case mostActive:
			case followed:
			case suggested:
				videogames.addAll(getData());
				break;
		}

		//put all videogame communities in the Pane
		fillGridPane();
	}

	/**
	 * method that let a user do the logout and return to the login page
	 */
	@FXML
	void logout() {
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

	/**
	 * method called in all methods that need to create videogames item to put in the GridPane
	 */
	@FXML
	void fillGridPane() {
		int column = 0, row = 1;
		//OPEN USER COMMUNITY PAGE CLICKING ON A VIDEOGAME ITEM (if click on a community open its community page)
		videogameListener = new VideogameListener() {
			@Override
			public void  onClickListener(javafx.scene.input.MouseEvent event, VideogameCommunity community) {
				try {
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("userCommunityPage.fxml"));
					Parent root1 = fxmlLoader.load();

					UserCommunityPageController controller2 = fxmlLoader.getController();
					controller2.sendVideogameCommunity(community, user, null);

					Stage stage = new Stage();
					stage.setTitle("Gameflows Community Page");
					stage.setScene(new Scene(root1));
					stage.setResizable(false);
					stage.show();

					Stage stage1 = (Stage) videogameGridPane.getScene().getWindow();
					stage1.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		//CREATE FOR EACH VIDEOGAME COMMUNITY AN ITEM (ItemVideogame)
		try {
			FXMLLoader fxmlLoader;
			if(choiceMode == HomeResearchMode.mostActive) {

				for (int i = 0; i < resultAggregationList.size(); i++) {
					ResultBestVideogameCommunityAggregation videogameWithStats = resultAggregationList.get(i);
					fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(getClass().getResource("itemVideogameWithStats.fxml"));
					AnchorPane anchorPane = fxmlLoader.load();

					ItemVideogameWithStatsController itemController = fxmlLoader.getController();
					itemController.setDataBestCommunity(videogameWithStats, videogameListener);

					//choice number of column
					if (column == 1) {
						column = 0;
						row++;
					}

					videogameGridPane.add(anchorPane, column++, row); //(child,column,row)
					//DISPLAY SETTINGS
					//set grid width
					videogameGridPane.setPrefWidth(485);
					//set grid height
					videogameGridPane.setPrefHeight(381);
					GridPane.setMargin(anchorPane, new Insets(10));
				}
			}
			else {

				for (int i = 0; i < videogames.size(); i++) { // @Luca iteri lista di videogamecommunity
					VideogameCommunity videogame = videogames.get(i);

					fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(getClass().getResource("itemVideogame.fxml"));
					AnchorPane anchorPane = fxmlLoader.load();


					ItemVideogameController itemController = fxmlLoader.getController();
					itemController.setData( videogame, videogameListener, choiceMode != HomeResearchMode.all);

					//choice number of column
					if (column == 3) {
						column = 0;
						row++;
					}

					videogameGridPane.add(anchorPane, column++, row); //(child,column,row)
					//DISPLAY SETTINGS
					//set grid width
					videogameGridPane.setPrefWidth(430);
					//set grid height
					videogameGridPane.setPrefHeight(71);
					GridPane.setMargin(anchorPane, new Insets(10));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used in selectSidebarButton.
	 * reset buttons to their original color.
	 */
	private void resetSidebarButtons () {
		allCommunitiesButton.getStyleClass().remove("selected-button");
		followedCommunitiesButton.getStyleClass().remove("selected-button");
		mostActiveCommunitiesButton.getStyleClass().remove("selected-button");
		suggestedCommunitiesButton.getStyleClass().remove("selected-button");
	}

	/**
	 *method that differently color a button when is pressed
	 * @param button is the button to be differently colored
	 */
	private void selectSidebarButton (Button button) {
		resetSidebarButtons();
		button.getStyleClass().add("selected-button");
	}

}
