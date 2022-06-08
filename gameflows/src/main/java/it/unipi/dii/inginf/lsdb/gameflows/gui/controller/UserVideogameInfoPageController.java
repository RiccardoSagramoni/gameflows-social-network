package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;


import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class UserVideogameInfoPageController {


	@FXML
	private ImageView communityLogo;

	@FXML
	private TextArea developerTextArea;

	@FXML
	private TextArea gameModeTextArea;

	@FXML
	private TextArea genreTextArea;

	@FXML
	private TextArea nameTextArea;

	@FXML
	private TextArea platformTextArea;

	@FXML
	private TextArea publisherTextArea;

	@FXML
	private TextArea ratingTextArea;

	@FXML
	private TextArea releaseDateTextArea;

	@FXML
	private TextArea summaryTextArea;

	@FXML
	private Label titleLabel;


	/**
	 * sendCommunity is a public function invoked in UserCommunityPageController where is passed
	 * the clicked videogame community as parameter to display its information
	 * @param community
	 */
	public void sendCommunity (VideogameCommunity community){
		//create gamemodes string
		StringBuilder gamemodes = new StringBuilder();
		if(community.getGameMode()  != null) {
			for (String gamemode : community.getGameMode()) {
				gamemodes.append(" ").append(gamemode);
			}
		}

		//create platforms string
		StringBuilder platforms = new StringBuilder();
		if(community.getPlatform()  != null) {
			for (String platform : community.getPlatform()) {
				platforms.append(" ").append(platform);
			}
		}

		//create genres string
		StringBuilder genres = new StringBuilder();
		if(community.getGenre()  != null) {
			for (String genre : community.getGenre()) {
				genres.append(" ").append(genre);
			}
		}

		//create developers string
		StringBuilder developers = new StringBuilder();
		if(community.getDeveloper()  != null) {
			for (String developer : community.getDeveloper()) {
				developers.append(" ").append(developer);
			}
		}

		//create publishers string
		StringBuilder publishers = new StringBuilder();
		if(community.getPublisher()  != null) {
			for (String publisher : community.getPublisher()) {
				publishers.append(" ").append(publisher);
			}
		}


		//SET ALL ELEMENTS
		titleLabel.setText(community.getName()+" Community Info");
		nameTextArea.setText(community.getName());
		gameModeTextArea.setText(gamemodes.toString());
		platformTextArea.setText(platforms.toString());
		if(community.getReleaseDate() != null) {
			releaseDateTextArea.setText(community.getReleaseDate().toString());
		}
		//if(community.getAggregatedRating() != null || community.getAggregatedRating().toString() != "0"){
		if(community.getAggregatedRating() != null){
			ratingTextArea.setText(community.getAggregatedRating().toString());
		}
		developerTextArea.setText(developers.toString());
		genreTextArea.setText(genres.toString());
		publisherTextArea.setText(publishers.toString());
		if(community.getSummary() != null)
			summaryTextArea.setText(community.getSummary());

		//retrieve and set community logo
		String imageSource = "https:"+community.getCover();
		Image image = new Image(imageSource);
		communityLogo.setImage(image);

	}

}