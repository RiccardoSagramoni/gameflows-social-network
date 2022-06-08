package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.*;
import it.unipi.dii.inginf.lsdb.gameflows.user.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminUserInfoPageController {

    @FXML
    private Button banButton;

    @FXML
    private TextArea dateOfBirthTextArea;

    @FXML
    private TextArea emailTextArea;

    @FXML
    private TextArea genderTextArea;

    @FXML
    private TextArea isBlockedTextArea;

    @FXML
    private TextArea isInfluencerTextArea;

    @FXML
    private TextArea nameTextArea;

    @FXML
    private TextArea nationalityTextArea;

    @FXML
    private TextArea surnameTextArea;

    @FXML
    private TextArea usernameTextArea;

    // USER VARIABLE
    private User user = null;

    //ADMIN VARIABLE
    private final AdminService adminService = AdminServiceFactory.getInstance();
    private Admin admin = null;

    /**
     * method that retrieve the information of the user from the user object passed by parameter
     *
     * @param admin object to set the admin variable of the classe
     * @param user object to retrieve the information of the user to show
     */
    public void viewUserInfo(Admin admin, User user){

        this.user = user;
        this.admin = admin;

        usernameTextArea.setText(user.getUsername());
        nameTextArea.setText(user.getName());
        surnameTextArea.setText(user.getSurname());
        emailTextArea.setText(user.getEmail());
        genderTextArea.setText(user.getGender());
        dateOfBirthTextArea.setText(user.getDateOfBirth().toString());
        nationalityTextArea.setText(user.getNationality());
        if(user.isInfluencer()){
            isInfluencerTextArea.setText("Yes");
        }
        else{
            isInfluencerTextArea.setText("No");
        }
        if(user.isBlocked()){
            isBlockedTextArea.setText("Yes");
        }
        else{
            isBlockedTextArea.setText("No");
        }

        if(user.isBlocked()){
            banButton.setText("UnBan");
        }
        else {
            banButton.setText("Ban");
        }

    }

    /**
     * method for ban(block) an user or unban(unblock) him/her
     *
     * @param event of the mouse when the ban/unban button is clicked
     */
    @FXML
    void banOrUnbanUser(ActionEvent event) {
        if(user.isBlocked()){
            user.setBlocked(false);
            banButton.setText("Ban");
            isBlockedTextArea.setText("No");
            adminService.blockUser(user.getUsername(), false);
        }
        else{
            user.setBlocked(true);
            banButton.setText("UnBan");
            isBlockedTextArea.setText("Yes");
            adminService.blockUser(user.getUsername(), true);
        }

    }

}
