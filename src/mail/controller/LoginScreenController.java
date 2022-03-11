package mail.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import mail.EmailManager;
import mail.controller.persistence.PersistenceAccess;
import mail.controller.services.LoginService;
import mail.model.EmailAccount;
import mail.view.ViewFactory;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginScreenController extends BaseController implements Initializable {
    @FXML
    private TextField loginTextField;
    @FXML
    private Label loginErroLabel;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordErroLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private ProgressIndicator progressIndicator;


    @FXML
    private Button loginButton;

    private LoginService loginService;

    @FXML
    private CheckBox rememberCheckBox;

    private PersistenceAccess persistenceAccess = new PersistenceAccess();

    private int count = 0;


    public LoginScreenController(EmailManager emailManager, ViewFactory viewFactory, String fxmlName) {
        super(emailManager, viewFactory, fxmlName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressIndicator.setVisible(false);
        loginTextField.setText("nishikimail19@gmail.com");
        passwordField.setText("watashinopasuwodo");
        errorLabel.setStyle("-fx-text-fill: red");
        setUpLoginField();
        setUpPasswordField();
    }

    @FXML
    public void whenSelected() {
        if(rememberCheckBox.isVisible()) {
            persistenceAccess.saveAccountsLoginPreference(rememberCheckBox);
        }
    }

    public void checkBoxVisible(boolean visible){
        rememberCheckBox.setVisible(visible);
    }

    @FXML
    void loginButtonAction() {
        errorLabel.setText("");
        whenSelected();
        if(fieldsAreValid()){
            EmailAccount emailAccount = new EmailAccount(loginTextField.getText(),passwordField.getText());
            loginService = new LoginService(emailAccount, emailManager);
            if(!loginService.isRunning()) {
                progressIndicator.progressProperty().bind(loginService.progressProperty());
                loginService.setOnRunning(e -> {
                    progressIndicator.setVisible(true);
                    mouseTransparent(true);
                });
                loginService.start();
                loginService.setOnSucceeded(e -> {
                    EmailLoginResult emailLoginResult = loginService.getValue();
                    System.out.println(loginService.getValue());
                    switch (emailLoginResult) {
                        case SUCCESS:
                            System.out.println("login succesfull!!!" + emailAccount);
                            emailManager.addEmailAccount(emailAccount);
                            if (!viewFactory.isMainWindowInitialize()) {
                                viewFactory.showMainWindow();
                            }
                            Stage stage = (Stage) loginTextField.getScene().getWindow();
                            viewFactory.closeStage(stage);
                            return;
                        case FAILED_BY_CREDENTIALS:
                            errorLabel.setText("Invalid credentials");
                            progressIndicator.setVisible(false);
                            mouseTransparent(false);
                            return;
                        case FAILED_BY_NETWORK:
                            progressIndicator.setVisible(false);
                            mouseTransparent(false);
                            errorLabel.setText("Netowork error");
                            return;
                        case FAILED_BY_UNEXPECTED_ERROR:
                            progressIndicator.setVisible(false);
                            mouseTransparent(false);
                            errorLabel.setText("An unexpected error has ocurred");
                            return;
                        default:
                            return;
                    }
                });
            }
        }
    }

    public void mouseTransparent(boolean isTransparent){
        rememberCheckBox.mouseTransparentProperty().set(isTransparent);
        loginButton.mouseTransparentProperty().set(isTransparent);
        loginTextField.mouseTransparentProperty().set(isTransparent);
        passwordField.mouseTransparentProperty().set(isTransparent);
    }

    private boolean fieldsAreValid() {
        if(loginTextField.getText().isEmpty()){
            loginErroLabel.setText("Please fill the email first");
            return false;
        }else {
            loginErroLabel.setText("");
        }
        if(passwordField.getText().isEmpty()){
            passwordErroLabel.setText("Plase fill password first");
            return false;
        }else{
            passwordErroLabel.setText("");
        }
        return true;
    }

    private void setUpPasswordField() {
        passwordField.setOnInputMethodTextChanged(e -> {
            passwordErroLabel.setText("");
            errorLabel.setText("");
        });
        passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if(key.getCode().equals(KeyCode.ENTER)){
                    loginButtonAction();
                }
            }
        });
    }

    private void setUpLoginField() {
        loginTextField.setOnAction(e -> {
            loginErroLabel.setText("");
            errorLabel.setText("");
        });
        loginTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if(key.getCode().equals(KeyCode.ENTER)){
                    loginButtonAction();
                }
            }
        });
    }
}
