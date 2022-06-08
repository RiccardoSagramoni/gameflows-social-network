package it.unipi.dii.inginf.lsdb.gameflows.gui.controller;

import it.unipi.dii.inginf.lsdb.gameflows.admin.*;
import it.unipi.dii.inginf.lsdb.gameflows.gui.controller.listener.*;
import it.unipi.dii.inginf.lsdb.gameflows.post.ResultBestUserByPostAggregation;
import it.unipi.dii.inginf.lsdb.gameflows.user.*;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewInfluencerRankingController {

    private static final Logger LOGGER = LogManager.getLogger(ViewInfluencerRankingController.class);

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private GridPane influencerGridPane;

    @FXML
    private TextField limitNumber;

    @FXML
    private Button nextButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Label responseLabel;

    @FXML
    private Button prevButton;

    @FXML
    private ImageView loadingImage;

    @FXML
    private Button returnButton;

    @FXML
    private DatePicker toDatePicker;

    //VARIABLES
    private int skipCounter = 0;
    private final int skip = 20; //how many influecer to skip per time (coincide with limit)
    private final int limit = 20; //how many influencer to show for each page
    private int influencerToRequest = 25;
    private int columnGridPane = 0;
    private int rowGridPane = 0;

    //USER VARIABLES
    private final UserService userService = UserServiceFactory.getService();
    private List<User> influencers = new ArrayList<>();
    private List<User> totalInfluencers = new ArrayList<>();
    private List<Pair<String, Integer>> totalInfluencersWithGrade= new ArrayList<>();
    private List<String> influencerSelected = new ArrayList<>();

    // ADMIN VARIABLES
    private Admin admin = null;
    private final AdminService adminService = AdminServiceFactory.getInstance();

    //VARIABLES FOR DATE PICKER
    private Date fromDate;
    private Date toDate;

    /**
     * method that set the admin class variable and call the initialize
     *
     * @param admin set by the admin home controller
     */
    public void sendInfluencerRequest(Admin admin){

        this.admin = admin;
        initialize();
    }

    /**
     * method that disable the button for change page
     */
    private void disablePrevNextButton(){
        prevButton.setDisable(true);
        nextButton.setDisable(true);
    }

    /**
     * method that initialize the elements of the page
     */
    private void initialize(){
        skipCounter = 0;
        disablePrevNextButton();
        refreshButton.setDisable(true);
        responseLabel.setVisible(false);
        loadingImage.setVisible(false);

    }

    /**
     * method that convert in date type the two values in the two date picker used by the admin
     */
    private boolean pickDate() {

        responseLabel.setText("");
        responseLabel.setVisible(true);

        if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
            fromDate = Date.from((fromDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());
            toDate = Date.from((toDatePicker.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant());


            if(fromDate.after(toDate)){
                responseLabel.setTextFill(Color.RED);
                responseLabel.setText("Error! This is not a time machine!");
                return false;
            }
            else if(fromDate.equals(toDate)){
                responseLabel.setTextFill(Color.RED);
                responseLabel.setText("Error! Same Day Selected!");
                return false;
            }
            else {
                //clear error message
                responseLabel.setText("");
                return true;
            }
        }
        else{
            responseLabel.setTextFill(Color.RED);
            responseLabel.setText("Error! Some Dates are Missing!");
            return false;
        }
    }

    /**
     * method to check if the next or the prev button should be disabled according to
     * the dimension of the list of the data retrieved from the db, and if yes it disable them
     *
     * @param list of the data retrieved from the db
     */
    private void prevNextButtonsCheck(List list) {
        if((list.size() > 0)){
            if((list.size() < limit)){
                if(skipCounter <= 0 ){
                    disablePrevNextButton();
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
                disablePrevNextButton();
            }
            else{
                prevButton.setDisable(false);
                nextButton.setDisable(true);
            }
        }
    }

    /**
     * method that retrieve the user to show in the current page
     * the users are loaded by using the limit variable, where we select
     * a group of limit user to show for every page
     *
     *
     * @return the list of User to show in the current page
     */
    private List<User> getPageData(){
        //CHECK if prevButton can work
        if (skipCounter < 0){
            skipCounter = 0;
        }

        List<User> influencerToShowInThePage = new ArrayList<>();

        if(totalInfluencers.size() > 0){
            for (int i = skipCounter; (i < (skipCounter + limit)) && (i < totalInfluencers.size()); i++) {
                influencerToShowInThePage.add(totalInfluencers.get(i));
            }
        }

        prevNextButtonsCheck(influencerToShowInThePage);
        return influencerToShowInThePage;

    }

    /**
     * method that retrieve the users from the db,
     * this method retrieve all the user that decided by the admin through the
     * influencerToRequest variable
     *
     * @return the list of User object
     */
    private List<User> getData(){

        influencerSelected.clear();

        if(!(limitNumber.getText().equals(""))){
            int valueInsertedFromAdmin = Integer.parseInt(limitNumber.getText());
            if(valueInsertedFromAdmin > 0){
                influencerToRequest = valueInsertedFromAdmin;
            }
        }

        totalInfluencersWithGrade = adminService.viewInfluencerRanking(
                fromDate, toDate, influencerToRequest);

        limitNumber.setText(Integer.toString(totalInfluencersWithGrade.size()));

        List<User> usersObject = new ArrayList<>();
        totalInfluencersWithGrade.forEach(
                i -> {
                    usersObject.add(new User(i.getKey()));
                    influencerSelected.add(i.getKey());
                }
        );
        prevNextButtonsCheck(usersObject);
        return usersObject;
    }

    /**
     * method that show a number of user decided by the admin, that have the prerequisites
     * in order to become influencers, and rank them
     *
     * @param event of the mouse when the button view influencers is clicked
     */
    @FXML
    void viewInfluencerRanking(ActionEvent event) {

        influencerGridPane.getChildren().clear();
        totalInfluencers.clear();
        influencers.clear();
        refreshButton.setDisable(true);
        disablePrevNextButton();

        if(pickDate()){
            // before long task
            // made visible the  loading gif
            loadingImage.setVisible(true);
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() {
                    // long task
                    totalInfluencers = getData();
                    influencers = getPageData();
                    return null ;
                }
            };
            task.setOnSucceeded(e -> {
                // after long task
                // made invisible the loading gif
                loadingImage.setVisible(false);
                fillGridPane();
                if(influencers.size() > 0){
                    responseLabel.setTextFill(Color.LIGHTGREEN);
                    responseLabel.setText("Research Successfully Executed!");
                    refreshButton.setDisable(false);
                }
                else{
                    refreshButton.setDisable(true);
                }

            });
            new Thread(task).start();
        }
    }

    /**
     * method that update the influncers,
     * in particular give the influencer badge to those user previously loaded with the viewInfluencerRanking,
     * and downgrade those who previously were influencers
     * but now doesn't have anymore the prerequisites and that are not loaded by the viewInfluencerRanking
     *
     * @param event of the mouse when the button refresh is clicked
     */
    @FXML
    void launchUpdateInfluencer(ActionEvent event) {

        try{
            if(adminService.updateInfluencers(influencerSelected)) {
                responseLabel.setVisible(true);
                //clear GridPane and videogames list
                influencerGridPane.getChildren().clear();
                influencers.clear();
            }
            else{
                responseLabel.setVisible(true);
                responseLabel.setStyle("-fx-text-fill: #ff0000;");
                responseLabel.setText("UPDATE ERROR!");
            }

        }
        catch (Exception e){
            e.printStackTrace();
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
        influencerGridPane.getChildren().clear();
        influencers.clear();
        //update the skipcounter
        skipCounter += skip;

        influencers.addAll(getPageData());
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
        influencerGridPane.getChildren().clear();
        influencers.clear();
        //update the skipcounter
        skipCounter -= skip;

        influencers.addAll(getPageData());
        fillGridPane();
    }

    /**
     * method that the admin home page
     *
     * @param event of the mouse when the return home button in clicked
     */
    @FXML
    void returnCommunityPage(ActionEvent event) {
        try {
            //OPEN ADMIN HOME PAGE
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminHome.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();

            //send admin id
            AdminHomeController controller2 = fxmlLoader.getController();
            controller2.sendAdmin(admin.getUsername());

            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setResizable(false);
            stage.setTitle("Gameflows Admin Home");
            stage.show();

            //CLOSE USER PAGE
            Stage stage1 = (Stage) returnButton.getScene().getWindow();
            stage1.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * method that fill the grid pain of the interface with the item User Influencer retrieved from the database
     * all the item are clickable and by clicking one of them, the details of the user will be shown
     */
    @FXML
    void fillGridPane() {
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
            for (int i = 0; i < influencers.size(); i++){
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("itemInfluencer.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();

                ItemInfluencerUserController itemController = fxmlLoader.getController();
                itemController.setData(
                        influencers.get(i),
                        userlistener,
                        i + 1 + skipCounter,
                        totalInfluencersWithGrade.get(i + skipCounter).getValue()
                );

                //choice number of column
                if (columnGridPane == 1) {
                    columnGridPane = 0;
                    rowGridPane++;
                }

                influencerGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)

                // DISPLAY SETTINGS
                GridPane.setMargin(anchorPane, new Insets(10));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
