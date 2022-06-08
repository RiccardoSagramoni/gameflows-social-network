package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.VideogameListener;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityService;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;


public class ItemVideogameController {

	@FXML
	private ImageView logo;
	@FXML
	protected Label name;


	private VideogameCommunity videogame;
	private VideogameListener videogameListener;
	private boolean toFind = false;
	private final VideogameCommunityService videogameService = VideogameCommunityServiceFactory.getService();

	/**
	 * method that call the on click listener that is redefined in the different fill grid pane
	 * in the different pages,
	 * the listener has as parameter userFromDB that is the object user retrieved from the db
	 * using the find method applied to the username obtained by the setData
	 *
	 * @param mouseEvent of the mouse when the videogame community item is clicked
	 */
	@FXML
	public void click(javafx.scene.input.MouseEvent mouseEvent) {

		if(this.toFind){
			VideogameCommunity videogameFromDB = videogameService.find(videogame.getId());
			videogameListener.onClickListener(mouseEvent, videogameFromDB);
		}
		else{
			videogameListener.onClickListener(mouseEvent, videogame);
		}
	}

	/**
	 * method that set the data of the item and set the videogame class variable that will
	 * be passed at the onClickListener method by the click function
	 * @param videogame clicked
	 * @param videogameListener object to identify the correct on click listener
	 * @param toFind parameter that discriminate if make or not the db call
	 * @throws FileNotFoundException
	 */
	public void setData(VideogameCommunity videogame, VideogameListener videogameListener, boolean toFind) throws FileNotFoundException{
		this.videogame = videogame;
		this.videogameListener = videogameListener;
		this.toFind = toFind;
		name.setText(videogame.getName());
	}
}
