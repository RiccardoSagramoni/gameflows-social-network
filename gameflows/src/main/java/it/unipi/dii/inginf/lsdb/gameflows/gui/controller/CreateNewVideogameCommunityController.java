package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityService;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityServiceFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CreateNewVideogameCommunityController {

	@FXML
	private TextField aggregatedRatingField;

	@FXML
	private TextField collectionField;

	@FXML
	private TextField coverField;

	@FXML
	private Button createButton;

	@FXML
	private TextField developerField;

	@FXML
	private Label errorLabel;

	@FXML
	private TextField gameModeField;

	@FXML
	private TextField genreField;

	@FXML
	private TextField nameField;

	@FXML
	private TextField platformField;

	@FXML
	private TextField publisherField;

	@FXML
	private DatePicker releaseDateDatePicker;

	@FXML
	private TextField summaryField;

	// DATE VARIABLE
	private Date releaseDate;

	// VIDEOGAME VARIABLE
	private final VideogameCommunityService videogameService = VideogameCommunityServiceFactory.getService();

	/**
	 * method that convert in date type the value in the date picker for the release date
	 */
	private void pickDate() {
		if (releaseDateDatePicker.getValue() != null) {
			releaseDate = Date.from((releaseDateDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		else{
			releaseDate = null;
		}
	}

	/**
	 * method that parse the string typed by the user in the text field, by splitting the string
	 * on every semicolon(;)
	 * this method is applied on the string retrieved from the text field where the admin can insert
	 * more than an option
	 *
	 * @param str is the string typed by the user in the text field with all the option
	 *            divided by a semicolon(;)
	 * @return of the list of string with all the option inserted by the admin
	 */
	private List<String> createListOfStringFromTextField (String str){

		List<String> ListOfStringToReturn = new ArrayList<>();

		String[] stringSplitted;
		// we delete all of the useless spaces
		str = str.replaceAll("\\s+", "");
		// we split the string on every ;
		stringSplitted = str.split(";");
		for (String scan : stringSplitted){
			ListOfStringToReturn = Arrays.asList(stringSplitted);
		}

		return ListOfStringToReturn;
	}

	/**
	 * method that create a new videogame community
	 * @param event of the mouse when the create new videogame community button is clicked
	 */
	@FXML
	void addNewVideogameCommunity(ActionEvent event) {
		if( nameField.getText() == null || summaryField.getText() == null ||
			nameField.getText().isEmpty() || summaryField.getText().isEmpty() ||
			nameField.getText().isBlank() || summaryField.getText().isBlank()){
			errorLabel.setVisible(true);
			errorLabel.setText("ERROR! SOME MANDATORY FIELDS ARE MISSING");
		}
		else{
			pickDate();

			VideogameCommunity videogameCommunity = new VideogameCommunity(

					nameField.getText(),
					summaryField.getText(),
					gameModeField.getText().isEmpty() ? null : createListOfStringFromTextField(gameModeField.getText()),
					platformField.getText().isEmpty() ? null : createListOfStringFromTextField(platformField.getText()),
					coverField.getText().isEmpty() ? null : coverField.getText(),
					genreField.getText().isEmpty() ? null : createListOfStringFromTextField(genreField.getText()),
					collectionField.getText().isEmpty() ? null : collectionField.getText(),
					aggregatedRatingField.getText().isEmpty() ? null : Double.parseDouble(aggregatedRatingField.getText()),
					releaseDate,
					developerField.getText().isEmpty() ? null : createListOfStringFromTextField(developerField.getText()),
					publisherField.getText().isEmpty() ? null : createListOfStringFromTextField(publisherField.getText())
			);

			if(videogameService.insertVideogameCommunity(videogameCommunity) == null){
				errorLabel.setVisible(true);
				errorLabel.setText("ERROR! THE COMMUNITY WAS NOT CREATED ");
			}

			//page closing
			Stage stage = (Stage) createButton.getScene().getWindow();
			stage.close();
		}
	}

}
