package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.UserListener;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.FileNotFoundException;

public class ItemInfluencerUserController {

    @FXML
    private Label grade;

    @FXML
    private Label rank;

    @FXML
    private Label username;

    // USER VARIABLES
    private User user;
    private UserListener userListener;

    /**
     * method that call the on click listener that is redefined in the different fill grid pane
     * in the different pages
     *
     * @param mouseEvent of the mouse when the user influencer item is clicked
     */
    @FXML
    void click(MouseEvent mouseEvent) {

        userListener.onClickListener(mouseEvent, user);
    }

    /**
     * method that set the data of the item and set the user class variable that will
     * be passed at the onClickListener method by the click function
     *
     * @param user object that will set the user class variable
     * @param userListener object to identify the correct on click listener
     * @param index of the rank of the users
     * @throws FileNotFoundException
     */
    public void setData(User user, UserListener userListener, int index, int gradeValue) throws FileNotFoundException {
        this.user = user;
        this.userListener = userListener;
        username.setText(user.getUsername());
        rank.setText(String.valueOf(index));
        grade.setText(String.valueOf(gradeValue));
    }
}



