package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.comment.ResultAverageCommentPerPost;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.VideogameListener;
import it.unipi.dii.inginf.lsdb.gameflows.post.ResultBestVideogameCommunityAggregation;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunity;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityService;
import it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity.VideogameCommunityServiceFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.io.FileNotFoundException;

public class ItemVideogameWithStatsController {

    @FXML
    private Label statisticsLabel;

    @FXML
    private Label statistics;

    @FXML
    private Label videogameName;

    // VIDEOGAME VARIABLES
    private VideogameCommunity videogame;
    private VideogameListener videogameListener;
    private final VideogameCommunityService videogameCommunityService = VideogameCommunityServiceFactory.getService();

    /**
     * method that call the on click listener that is redefined in the different fill grid pane
     * in the different pages
     *
     * @param event
     */
    @FXML
    void click(MouseEvent event) {
        VideogameCommunity videogameFromDB = videogameCommunityService.find(videogame.getId());
        videogameListener.onClickListener(event, videogameFromDB);
    }

    /**
     * method that set the data of the item and set the videogame community class variable that will
     * be passed at the onClickListener method by the click function
     *
     * @param averageObject object of ResultAverageCommentPerPost that will set the videogame class variable
     * @param videogameListener object to identify the correct on click listener
     * @throws FileNotFoundException
     */
    public void setDataAverageCommentPerPost(ResultAverageCommentPerPost averageObject, VideogameListener videogameListener) throws FileNotFoundException {
        this.videogame = new VideogameCommunity(averageObject.getVideogameId(), averageObject.getVideogameName()) ;
        this.videogameListener = videogameListener;
        videogameName.setText(averageObject.getVideogameName());
        statisticsLabel.setText("Average Comments Per Post:");
        statistics.setText(String.valueOf(Math.round(averageObject.getAverage() * 100.0) / 100.0));
    }

    public void setDataBestCommunity(ResultBestVideogameCommunityAggregation bestCommunityObject, VideogameListener videogameListener) throws FileNotFoundException {
        this.videogame = new VideogameCommunity(bestCommunityObject.getVideogameCommunityId(), bestCommunityObject.getVideogameCommunityName()) ;
        this.videogameListener = videogameListener;
        videogameName.setText(bestCommunityObject.getVideogameCommunityName());
        statisticsLabel.setText("Number of Likes:");
        statistics.setText(String.valueOf(bestCommunityObject.getLikes()));
    }

}
