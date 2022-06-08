package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.Admin;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.UserListener;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostService;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.post.ResultBestUserByPostAggregation;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserService;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewBestUsersByNumberOfPostsController {

	// Logger
	private static final Logger LOGGER = LogManager.getLogger(ViewBestUsersByNumberOfPostsController.class);

	@FXML
	private GridPane bestUsersGridPane;

	@FXML
	private Label errorLabel;

	@FXML
	private TextField limitNumber;

	@FXML
	private ImageView loadingImage;

	@FXML
	private Button nextButton;

	@FXML
	private Button prevButton;

	@FXML
	private Button returnButton;

	//VARIABLES
	private int skipCounter = 0;
	private final int skip = 20; //how many influecer to skip per time (coincide with limit)
	private final int limit = 20; //how many influencer to show for each page
	private int bestUsersToRequest = 25;
	private int columnGridPane = 0;
	private int rowGridPane = 0;

	//USER AND ADMIN VARIABLES
	private final UserService userService = UserServiceFactory.getService();
	private List<ResultBestUserByPostAggregation> bestUsers = new ArrayList<>();
	private List<ResultBestUserByPostAggregation> totalBestUsers = new ArrayList<>();

	private Admin admin = null;
	private VideogameCommunity videogame = null;
	private final PostService postService = PostServiceFactory.getService();

	public void viewBestUsersByNumberOfPosts(Admin admin, VideogameCommunity videogame){
		this.admin = admin;
		this.videogame = videogame;
		initialize();
	}

	/**
	 * method that initialize the elements of the page
	 */
	private void initialize(){
		skipCounter = 0;
		nextButton.setDisable(true);
		prevButton.setDisable(true);
		errorLabel.setVisible(false);
		loadingImage.setVisible(false);
	}

	/**
	 * method to check if the next or the prev button should be disabled according to
	 * the dimension of the list of the data retrieved from the db, and if yes it disable them
	 *
	 * @param list of the data retrieved from the db
	 */
	private void prevNextButtonsCheck(List list) {
		LOGGER.info("sono qui");
		if((list.size() > 0)){
			if((list.size() < limit)){
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
			else{
				prevButton.setDisable(false);
				nextButton.setDisable(true);
			}

		}
	}

	/**
	 * method that specifies that we have to go on the next page
	 *
	 * @param event of the mouse when the button next page is clicked
	 */
	@FXML
	void goNextPage(ActionEvent event) {
		//clear GridPane and videogames list
		bestUsersGridPane.getChildren().clear();
		bestUsers.clear();
		//update the skipcounter
		skipCounter += skip;

		bestUsers.addAll(getPageData());
		fillGridPane();
	}

	/**
	 * method that specifies that we have to go on the previous page
	 *
	 * @param event of the mouse when the button previous page is clicked
	 */
	@FXML
	void goPrevPage(ActionEvent event) {

		//clear GridPane and videogames list
		bestUsersGridPane.getChildren().clear();
		bestUsers.clear();
		//update the skipcounter
		skipCounter -= skip;

		bestUsers.addAll(getPageData());
		fillGridPane();
	}

	/**
	 * method that the admin home page
	 *
	 * @param event of the mouse when the return home button in clicked
	 */
	@FXML
	void returnHomePage (ActionEvent event) {
		try {
			//OPEN ADMIN HOME PAGE
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminHome.fxml"));
			Parent root1 = fxmlLoader.load();

			//send admin id
			AdminHomeController controller2 = fxmlLoader.getController();
			controller2.sendAdmin(admin.getUsername());

			Stage stage = new Stage();
			stage.setScene(new Scene(root1));
			stage.setResizable(false);
			stage.show();

			//CLOSE USER PAGE
			Stage stage1 = (Stage) returnButton.getScene().getWindow();
			stage1.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * method that retrieve the user to show in the current page
	 * the users are loaded by using the limit variable, where we select
	 * a group of limit user to show for every page
	 *
	 *
	 * @return the list of ResultBestUserByPostAggregation to show in the current page
	 */
	private List<ResultBestUserByPostAggregation> getPageData(){
		//CHECK if prevButton can work
		if (skipCounter < 0){
			skipCounter = 0;
		}

		List<ResultBestUserByPostAggregation> bestUsersToShowInThePage = new ArrayList<>(20);

		for (int i = skipCounter; (i < (skipCounter + limit)) && (i < totalBestUsers.size()); i++) {
			bestUsersToShowInThePage.add(totalBestUsers.get(i));
		}

		prevNextButtonsCheck(bestUsersToShowInThePage);
		return bestUsersToShowInThePage;

	}

	/**
	 * method that retrieve the users from the db,
	 * this method retrieve all the user that decided by the admin through the
	 * bestUsersToRequest variable
	 *
	 * @return the list of ResultBestUserByPostAggregation object
	 */
	private List<ResultBestUserByPostAggregation> getData(){

		List<ResultBestUserByPostAggregation> totalBestUsers = new ArrayList<>();

		int valueInsertedFromAdmin = Integer.parseInt(limitNumber.getText());
		if(valueInsertedFromAdmin > 0){
			bestUsersToRequest = valueInsertedFromAdmin;
		}

		totalBestUsers = postService.bestUsersByNumberOfPosts(videogame.getId(), bestUsersToRequest);

		limitNumber.setText(Integer.toString(totalBestUsers.size()));

		return totalBestUsers;
	}

	/**
	 * method that sent the request of loading of the best user by number of posts
	 *
	 * @param event of the mouse when the Apply filters and view User is clicked
	 */
	@FXML
	void launchRequestOfBestUsers(ActionEvent event) {
		// before long task
		// made visible the  loading gif
		bestUsersGridPane.getChildren().clear();
		totalBestUsers.clear();
		bestUsers.clear();

		loadingImage.setVisible(true);
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {
				// long task
				totalBestUsers = getData();
				bestUsers = getPageData();

				return null ;
			}
		};
		task.setOnSucceeded(e -> {
			// after long task
			// made invisible the loading gif
			loadingImage.setVisible(false);
			fillGridPane();
		});
		new Thread(task).start();

	}

	/**
	 * method that set the starting right value of column and row of the grid pain
	 * the values depend on the list size of object retrieved from the db
	 */
	private void setGridPaneColumnAndRow(){
		columnGridPane = 0;
		rowGridPane = 1;
	}

	/**
	 * method that fill the grid pain of the interface with the item User With Stats retrieved from the database
	 * all the item are clickable and by clicking one of them, the details of the user will be shown
	 */
	void fillGridPane(){

		//OPEN USER COMMUNITY PAGE CLICKING ON A VIDEOGAME ITEM (if click on a community open its community page)
		UserListener userlistener = new UserListener() {
			@Override
			public void onClickListener(javafx.scene.input.MouseEvent event, User user) {
				try {
					FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminUserPage.fxml"));
					Parent root1 = (Parent) fxmlLoader.load();

					User userFromDB = userService.find(user.getUsername());

					AdminUserInfoPageController controller2 = fxmlLoader.getController();
					controller2.viewUserInfo(admin, userFromDB);

					Stage stage = new Stage();
					stage.setTitle("User Info Page");
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

		setGridPaneColumnAndRow();

		//CREATE FOR EACH VIDEOGAME COMMUNITY AN ITEM (ItemVideogame)
		try {
			for (ResultBestUserByPostAggregation user : bestUsers) {
				FXMLLoader fxmlLoader = new FXMLLoader();
				fxmlLoader.setLocation(getClass().getResource("ItemUserWithStats.fxml"));
				AnchorPane anchorPane = fxmlLoader.load();

				ItemUserWithStatsController itemController = fxmlLoader.getController();
				itemController.setDataBestUsers(user, userlistener);
				//choice number of column
				if (columnGridPane == 1) {
					columnGridPane = 0;
					rowGridPane++;
				}
				bestUsersGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
				//DISPLAY SETTINGS
				GridPane.setMargin(anchorPane, new Insets(10));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
