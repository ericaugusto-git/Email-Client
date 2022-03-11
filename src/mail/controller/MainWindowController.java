package mail.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import mail.EmailManager;
import mail.controller.services.MessageRendererService;
import mail.model.EmailMessage;
import mail.model.EmailTreeItem;
import mail.view.ViewFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MainWindowController extends BaseController implements Initializable {

    @FXML
    private TableView<EmailMessage> mailTableView;

    @FXML
    private TreeView<String> mailTreeView;

    @FXML
    private WebView mailWebView;
    private MessageRendererService messageRendererService;

    @FXML
    private TableColumn<EmailMessage, String> recipientCol;
    @FXML
    private TableColumn<EmailMessage, String> senderCol;
    @FXML
    private TableColumn<EmailMessage, Integer> sizeCol;
    @FXML
    private TableColumn<EmailMessage, Date> dateCol;
    @FXML
    private TableColumn<EmailMessage, Integer> subjectCol;
    @FXML
    private AnchorPane attachmentContainer;
    @FXML
    private Label attachmentLabel;
    @FXML
    private Label attachmentNameLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label unreadLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private SplitMenuButton splitMenuButton;

    public static final String DEFAULT_PATH = System.getProperty("user.home") + "\\Downloads\\";

    private MenuItem markAsUnreadMenuItem = new MenuItem("mark message as unread");
    private MenuItem deleteMessageMenuItem = new MenuItem("delete message");
    private MenuItem deleteAccount = new MenuItem("delete account");

    private int updateProgress = 1;

    private ObservableSet<MimeBodyPart> mimeBodyParts = FXCollections.observableSet();

    public MainWindowController(EmailManager emailManager, ViewFactory viewFactory, String fxmlName) {
        super(emailManager, viewFactory, fxmlName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpMailsTreeView();
        setUpMailsTableView();
        setUpFolderSelection();
        setUpBoldRows();
        setUpMessageRendererService();
        setUpMessageSelection();
        setUpMenuItems();
        mailWebView.setVisible(false);
    }

    //show dialog to select a folder
    private File getSelectedFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(DEFAULT_PATH));
        File selectedFolder = directoryChooser.showDialog(totalLabel.getScene().getWindow());
        return selectedFolder;
    }

    //Action for the Save As button that download all attachments on the folder selected.
    @FXML
    void saveAs() {
        if(mimeBodyParts.size() > 1) {
            File selectedFolder = getSelectedFolder();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    mimeBodyParts.stream().forEach(e -> {
                        try {
                            updateProgress += 1;
                            File file = new File(selectedFolder + "\\" + e.getFileName());
                            e.saveFile(file);
                            if (file.exists()) {
                                updateProgress(updateProgress, mimeBodyParts.size());
                            }
                        }catch (MessagingException messagingException){
                            messagingException.printStackTrace();
                        }catch (IOException ioException){
                            ioException.printStackTrace();
                        }
                    });
                    return null;
                }
            };
            viewFactory.showLoadingSceenController(task,mimeBodyParts.size());
            new Thread(task).start();
        }else{
            if(!mimeBodyParts.isEmpty()){
                downloadAttachmentsService(mimeBodyParts.iterator().next());
            }
        }
    }

    private void downloadAttachmentsService(MimeBodyPart mimeBodyPart) {
        File selectedFolder = getSelectedFolder();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try{
                    File file = new File(selectedFolder + "\\" + mimeBodyPart.getFileName());
                    mimeBodyPart.saveFile(file);
                    updateProgress(1,1);
                }catch (MessagingException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            }
        };
        viewFactory.showLoadingSceenController(task,mimeBodyParts.size());
        new Thread(task).start();
    }

    private void loadAttachments(EmailMessage message) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    splitMenuButton.getItems().clear();
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (message.hasAttachments()) {
                            attachmentContainer.setVisible(true);
                            for (MimeBodyPart mimeBodyPart : message.getAttachmentList()) {
                                try {
                                    MenuItem menuItem = new MenuItem(mimeBodyPart.getFileName());
                                    splitMenuButton.getItems().add(menuItem);
                                    mimeBodyParts.add(mimeBodyPart);
                                    menuItem.setOnAction(e -> {
                                        downloadAttachmentsService(mimeBodyPart);
                                    });
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (splitMenuButton.getItems().stream().count() > 1) {
                                attachmentNameLabel.setText("Attachments");
                            } else {
                                attachmentNameLabel.setText("Attachment");
                            }
                        } else {
                            splitMenuButton.getItems().clear();
                            attachmentContainer.setVisible(false);
                        }
                        attachmentLabel.setText(splitMenuButton.getItems().stream().count() + "");
                    }
                });
            }
        };
        thread.start();
    }


    private void setUpInfoLabels() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1),
                event -> {
                    unreadLabel.textProperty().bind(new SimpleIntegerProperty(emailManager.getUnreadMessagesCount()).asString());
                    totalLabel.textProperty().bind(new SimpleIntegerProperty(emailManager.getSelectedFolder().getMessagesCount()).asString());
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    void optionsAction() {
        viewFactory.showOptionWindow();
    }


    @FXML
    void addAccountAction() {
        viewFactory.showLoginWindow(false);
    }


    @FXML
    void composeMessageAction(ActionEvent event) {
        viewFactory.showComposeMessageWindow();
    }

    @FXML
    void showAppInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Information");
        alert.setContentText("This app was created for educational purposes, it shouldn't be seriously used."
                + " I used the course Advanced Java programming with JavaFx: Write an email client as a reference.");
        alert.showAndWait();
    }

    private void setUpMenuItems() {
        markAsUnreadMenuItem.setOnAction(e -> {
            emailManager.setUnread();
        });
        deleteMessageMenuItem.setOnAction(e -> {
            emailManager.deleteMessage();
            mailWebView.getEngine().loadContent("");
        });
        deleteAccount.setOnAction(e -> {
            removeTreeItem(mailTreeView.getSelectionModel().getSelectedItem());
            mailTableView.setItems(null);
            mailWebView.setVisible(false);
        });
    }

    public void removeTreeItem(TreeItem<String> currentItem){
        if(currentItem.getValue().contains("@")){
            String removedItem = currentItem.getValue().toString();
            emailManager.removeAccount(removedItem);
            currentItem.getParent().getChildren().remove(currentItem);
        }else{
            currentItem = currentItem.getParent();
            removeTreeItem(currentItem);
        }
    }

    private void setUpMessageSelection() {
        mailTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() <= 1) {
                EmailMessage message = mailTableView.getSelectionModel().getSelectedItem();
                if (message != null) {
                    emailManager.setSelectedMessage(message);
                    mailWebView.setVisible(true);
                    if (!message.isRead()) {
                        emailManager.setRead();
                    }
                    emailManager.setSelectedMessage(message);
                    loadAttachments(message);
                    mimeBodyParts.clear();
                    messageRendererService.setEmailMessage(message);
                    messageRendererService.restart();
                }
            } else {
                System.out.println("Click count: " + e.getClickCount());
            }
        });
    }

    private void setUpMessageRendererService() {
        messageRendererService = new MessageRendererService(mailWebView.getEngine());
    }

    private void setUpBoldRows() {
        mailTableView.setRowFactory(new Callback<TableView<EmailMessage>, TableRow<EmailMessage>>() {
            @Override
            public TableRow<EmailMessage> call(TableView<EmailMessage> emailMessageTableView) {
                return new TableRow<>() {
                    @Override
                    protected void updateItem(EmailMessage item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            if (!item.isRead()) {
                                setStyle("-fx-font-weight: bold");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });
    }

    private void setUpFolderSelection() {
        mailTreeView.setOnMouseClicked(e -> {
            EmailTreeItem<String> item = (EmailTreeItem<String>) mailTreeView.getSelectionModel().getSelectedItem();
            if (item != null) {
                emailManager.setSelectedFolder(item);
                mailTableView.setItems(item.getEmailMassages());
                mailWebView.setVisible(false);
                attachmentContainer.setVisible(false);
                setUpInfoLabels();
            }
        });
    }

    private void setUpMailsTableView() {
        senderCol.setCellValueFactory(new PropertyValueFactory<>("sender"));
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        recipientCol.setCellValueFactory(new PropertyValueFactory<>("recipient"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        mailTableView.setContextMenu(new ContextMenu(markAsUnreadMenuItem, deleteMessageMenuItem));
        mailTableView.setPlaceholder(new Label("no messages on this folder"));
    }

    private void setUpMailsTreeView() {
        mailTreeView.setRoot(emailManager.getFoldersRoot());
        mailTreeView.setShowRoot(false);
        mailTreeView.setContextMenu(new ContextMenu(deleteAccount));
    }
}