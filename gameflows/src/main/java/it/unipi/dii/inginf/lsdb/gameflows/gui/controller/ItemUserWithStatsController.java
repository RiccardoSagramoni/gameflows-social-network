package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.comment.ResultAverageCommentPerUser;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.UserListener;
import it.unipi.dii.inginf.lsdb.gameflows.post.ResultBestUserByPostAggregation;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserService;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.io.FileNotFoundException;

public class ItemUserWithStatsController {

	@FXML
	private Label statistics;

	@FXML
	private Label statisticsLabel;

	@FXML
	private Label username;

	// USER VARIABLES
	private User user;
	private UserListener userListener;
	private final UserService userService = UserServiceFactory.getService();

	/**
	 * method that call the on click listener that is redefined in the different fill grid pane
	 * in the different pages,
	 * the listener has as parameter userFromDB that is the object user retrieved from the db
	 * using the find method applied to the username obtained by the setData
	 *
	 * @param mouseEvent of the mouse when the user influencer item is clicked
	 */
	@FXML
	void click(MouseEvent mouseEvent) {
		User userFromDB = userService.find(user.getUsername());
		userListener.onClickListener(mouseEvent, userFromDB);
	}

	/**
	 * method that set the data of the item and set the user class variable that will
	 * be passed at the onClickListener method by the click function
	 *
	 * @param user object of ResultBestUserByPostAggregation that will set the user class variable
	 * @param userListener object to identify the correct on click listener
	 * @throws FileNotFoundException
	 */
	public void setDataAverageCommentPerPost(ResultAverageCommentPerUser user, UserListener userListener) throws FileNotFoundException {
		this.user = new User(user.getUsername()) ;
		this.userListener = userListener;
		username.setText(user.getUsername());
		statisticsLabel.setText("Average Comments Per Post:");
		statistics.setText(String.valueOf(Math.round(user.getAverage() * 100.0) / 100.0));
	}

	/**
	 * method that set the data of the item and set the user class variable that will
	 * be passed at the onClickListener method by the click function
	 *
	 * @param user object of ResultBestUserByPostAggregation that will set the user class variable
	 * @param userListener object to identify the correct on click listener
	 * @throws FileNotFoundException
	 */
	public void setDataBestUsers(ResultBestUserByPostAggregation user, UserListener userListener) throws FileNotFoundException {
		this.user = new User(user.getUsername()) ;
		this.userListener = userListener;
		username.setText(user.getUsername());
		statisticsLabel.setText("Number of Post:");
		statistics.setText(String.valueOf(user.getNumPost()));
	}

}
