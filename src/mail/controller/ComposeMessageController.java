package mail.controller;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mail.EmailManager;
import mail.controller.services.EmailSenderService;
import mail.model.EmailAccount;
import mail.view.ViewFactory;

import javax.swing.text.html.HTML;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class ComposeMessageController extends BaseController implements Initializable {


    @FXML
    private ChoiceBox<EmailAccount> emailAccountChoiceBox;

    @FXML
    private HTMLEditor htmlEditior;

    @FXML
    private TextField recipientTextField;

    @FXML
    private TextField subjectTextField;

    @FXML
    private Label errorLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button sendButton;

    private List<File> attachments = new ArrayList<>();

    private StringBuffer preview = new StringBuffer();

    private String textFieldInitialValue;

    //Regex used to exclude attachments used as a preview from the final sending result.
    public static final String HTML_REGEX = "(<img src=\")[\\w\\W\\u00C0-\\u00FF]*[.\\w]*(\" width=\"150\" heigth=\"100\">)";

    @FXML
    void attachmentButtonAction() throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null){
            attachments.add(selectedFile);
            populatePreview(selectedFile);
            String editedHTML = htmlEditior.getHtmlText().replaceAll(HTML_REGEX, "");
            System.out.println(editedHTML);
            htmlEditior.setHtmlText(preview.toString() + editedHTML);
        }
    }

    private void populatePreview(File selectedFile) throws MalformedURLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<img src = \"");
        stringBuilder.append(new File(selectedFile.toString()).toURI().toURL());
        stringBuilder.append("\"");
        stringBuilder.append(" width= 150 heigth=100>\n");
        preview.append(stringBuilder.toString());
        System.out.println(preview);
    }

    @FXML
    void sendButton() {
        errorLabel.setText("");
        String editedHTML = htmlEditior.getHtmlText().replaceAll(HTML_REGEX, "");
        //null wasn't working for some reason so i used textFieldInitialValue
        if(recipientTextField.getText() != textFieldInitialValue) {
            EmailSenderService emailSenderService = new EmailSenderService(emailAccountChoiceBox.getValue(),
                    subjectTextField.getText(),
                    recipientTextField.getText(),
                    editedHTML,
                    attachments);
            progressIndicator.progressProperty().bind(emailSenderService.progressProperty());
            emailSenderService.setOnRunning(e ->{
                progressIndicator.setVisible(true);
                mouseTraperancyOfNodes(true);
            });
            emailSenderService.start();
            emailSenderService.setOnSucceeded(e -> {
                mouseTraperancyOfNodes(false);
                EmailSendingResult emailSendingResult = emailSenderService.getValue();
                switch (emailSendingResult) {
                    case SUCCESS -> {
                        Stage stage = (Stage) recipientTextField.getScene().getWindow();
                        viewFactory.closeStage(stage);
                        break;
                    }
                    case FAILED_BY_PROVIDER -> {
                        errorLabel.setText("Provider Error!");
                        progressIndicator.setVisible(false);
                        break;
                    }
                    case FAILED_BY_UNEXPECTED_ERROR -> {
                        errorLabel.setText("Unexpected Error");
                        progressIndicator.setVisible(false);
                        break;
                    }
                }
            });
        } else{
            errorLabel.setText("Enter a recipient first");
        }
    }

    private void mouseTraperancyOfNodes(boolean transperancy) {
        htmlEditior.mouseTransparentProperty().set(transperancy);
        recipientTextField.mouseTransparentProperty().set(transperancy);
        subjectTextField.mouseTransparentProperty().set(transperancy);
        emailAccountChoiceBox.mouseTransparentProperty().set(transperancy);
        sendButton.mouseTransparentProperty().set(transperancy);
    }

    public ComposeMessageController(EmailManager emailManager, ViewFactory viewFactory, String fxmlName) {
        super(emailManager, viewFactory, fxmlName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressIndicator.setVisible(false);
        emailAccountChoiceBox.setItems(emailManager.getEmailAccounts());
        emailAccountChoiceBox.setValue(emailManager.getEmailAccounts().get(0));
        recipientTextField.setOnAction(e -> errorLabel.setText(""));
        errorLabel.setStyle("-fx-text-fill: red");
        textFieldInitialValue = recipientTextField.getText();
    }
}
