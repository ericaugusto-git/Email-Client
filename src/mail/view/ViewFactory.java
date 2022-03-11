package mail.view;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mail.EmailManager;
import mail.controller.*;
import mail.controller.services.EmailSenderService;
import mail.controller.services.LoginService;

import java.io.IOException;
import java.util.ArrayList;

public class ViewFactory {

    private EmailManager emailManager;

    private ColorTheme colorTheme = ColorTheme.DARK;
    private FontSize fontSize = FontSize.MEDIUM;

    private ArrayList<Stage> activeStages;
    private boolean mainWindowInitialize;

    public ViewFactory(EmailManager emailManager) {
        this.emailManager = emailManager;
        activeStages = new ArrayList<Stage>();
    }

    public boolean isMainWindowInitialize(){
        return mainWindowInitialize;
    }

    public ColorTheme getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(ColorTheme colorTheme) {
        this.colorTheme = colorTheme;
    }

    public FontSize getFontSize() {
        return fontSize;
    }

    public void setFontSize(FontSize fontSize) {
        this.fontSize = fontSize;
    }

    public void showLoginWindow(boolean checkBoxVisible){
        LoginScreenController loginScreenController = new LoginScreenController(emailManager,this,"LoginScreen.fxml");
        BaseController controller = loginScreenController;
        initializeStage(controller,false,false,false);
        loginScreenController.checkBoxVisible(checkBoxVisible);
    }

    public void showMainWindow(){
        BaseController controller = new MainWindowController(emailManager, this, "MainWindow.fxml");
        initializeStage(controller,false,true,true);
        mainWindowInitialize = true;
    }

    public void showLoadingSceenController(Task task, int taskTotal){
        LoadingScreenController loadingScreenController = new LoadingScreenController(emailManager,this,"LoadingScreen.fxml");
        BaseController controller = loadingScreenController;
        initializeStage(controller,true,false,false);
        loadingScreenController.setUpProgressBar(task,taskTotal);
    }


    public void showOptionWindow(){
        BaseController controller = new OptionsWindowController(emailManager, this, "OptionsWindow.fxml");
        initializeStage(controller,true,false,false);
    }

    public void showComposeMessageWindow(){
        BaseController controller = new ComposeMessageController(emailManager, this, "ComposeMessageWindow.fxml");
        initializeStage(controller,true,false,false);
    }

    //load window trough parsed BaseController object
    private void initializeStage(BaseController controller, boolean isModdal, boolean isResizable,boolean isMaximized){
        FXMLLoader loader = new FXMLLoader(getClass().getResource(controller.getFxmlName()));
        loader.setController(controller);
        Parent parent = null;
        try {
            parent = loader.load();
        } catch (IOException e){
            e.printStackTrace();
        }
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(isResizable);
        stage.setMaximized(isMaximized);
        stage.getIcons().add(new Image("src/mail/view/icons/logo.png"));
        if(isModdal) {
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.show();
        activeStages.add(stage);
        updateStyles();
    }

    public void closeStage(Stage stageToClose){
        stageToClose.close();
        activeStages.remove(stageToClose);
    }

    public void updateStyles() {
        for(Stage stage : activeStages){
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(ColorTheme.getCssPath(colorTheme));
            scene.getStylesheets().add(FontSize.getCssPath(fontSize));
        }
    }
}