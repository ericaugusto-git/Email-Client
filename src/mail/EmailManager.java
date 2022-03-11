package mail;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import mail.controller.services.FetchFoldersService;
import mail.controller.services.FolderUpdaterService;
import mail.model.EmailAccount;
import mail.model.EmailMessage;
import mail.model.EmailTreeItem;
import mail.view.IconResolver;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EmailManager {

    private FolderUpdaterService folderUpdaterService;
    private TreeItem<String> foldersRoot = new TreeItem<String>("");
    private List<Folder> folderList = new ArrayList<>();

    private EmailMessage selectedMessage;
    private EmailTreeItem<String> selectedFolder;

    private IconResolver iconResolver = new IconResolver();

    private ObservableList<EmailAccount> emailAccounts = FXCollections.observableArrayList();


    public ObservableList<EmailAccount> getEmailAccounts() {
        return emailAccounts;
    }

    public EmailMessage getSelectedMessage() {
        return selectedMessage;
    }

    //Remove an account from the list using iterator which prevent previous ConcurrentModificationException problem
    public void removeAccount(String address){
        Iterator<EmailAccount> iterator = emailAccounts.iterator();
        while (iterator.hasNext()){
            EmailAccount iteratorEmailAccount = iterator.next();
            if(iteratorEmailAccount.getAddress() == address){
                System.out.println(iteratorEmailAccount.getAddress() + " removed");
                iterator.remove();
            }
        }
    }

    public void setSelectedMessage(EmailMessage selectedMessage) {
        this.selectedMessage = selectedMessage;
    }

    public EmailTreeItem<String> getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(EmailTreeItem<String> selectedFolder) {
        this.selectedFolder = selectedFolder;
    }

    public List<Folder> getFolderList() {
        return folderList;
    }

    public EmailManager() {
        folderUpdaterService = new FolderUpdaterService(folderList);
        folderUpdaterService.start();
    }

    public TreeItem<String> getFoldersRoot() {
        return foldersRoot;
    }

    public void addEmailAccount(EmailAccount emailAccount) {
        emailAccounts.add(emailAccount);
        EmailTreeItem<String> treeItem = new EmailTreeItem<>(emailAccount.getAddress());
        treeItem.setGraphic(iconResolver.getFolderIcon(emailAccount.getAddress()));
        treeItem.setExpanded(true);
        FetchFoldersService fetchFoldersService = new FetchFoldersService(emailAccount.getStore(), treeItem, folderList);
        fetchFoldersService.start();
        foldersRoot.getChildren().add(treeItem);
    }

    public void setRead() {
        try {
            selectedMessage.setRead(true);
            selectedMessage.getMessage().setFlag(Flags.Flag.SEEN, true);
            selectedFolder.decrementMessagesCount();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void setUnread() {
        try {
            selectedMessage.setRead(false);
            selectedMessage.getMessage().setFlag(Flags.Flag.SEEN, false);
            selectedFolder.incrementUnreadMenssagesCount();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(){
        try {
            selectedMessage.getMessage().setFlag(Flags.Flag.DELETED, true);
            selectedFolder.getEmailMassages().remove(selectedMessage);
        } catch (MessagingException e){
            e.printStackTrace();
        }
    }
    public int getMessagesCount(){
        return selectedFolder.getMessagesCount();
    }

    public int getUnreadMessagesCount(){
        return selectedFolder.getUnreadMessagesCount();
    }
}
