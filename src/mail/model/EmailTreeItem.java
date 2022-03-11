package mail.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class EmailTreeItem<String> extends TreeItem<String> {
    private String name;
    private ObservableList<EmailMessage> emailMessages;
    private int unreadMessagesCount;
    private int messagesCount;

    public EmailTreeItem(String name) {
        super(name);
        this.name = name;
        this.emailMessages = FXCollections.observableArrayList();
    }

    public ObservableList<EmailMessage> getEmailMassages(){
        return emailMessages;
    }

    public void addMessage(Message message) throws MessagingException {
        emailMessages.add(fetchMessage(message));
    }
    public void addMessageToTop(Message message) throws MessagingException {
        emailMessages.add(0,fetchMessage(message));
    }

    private EmailMessage fetchMessage(Message message) throws MessagingException {
        boolean messageIsRead = message.getFlags().contains(Flags.Flag.SEEN);
        EmailMessage emailMessage = new EmailMessage(message.getSubject(), message.getFrom()[0].toString(),
                message.getRecipients(MimeMessage.RecipientType.TO)[0].toString(), message.getSize(),
                message.getReceivedDate(),messageIsRead, message);
        if(!messageIsRead){
            incrementUnreadMenssagesCount();
        }
        messagesCount++;
        return emailMessage;
    }

    public void incrementUnreadMenssagesCount() {
        unreadMessagesCount++;
        updateName();
    }

    private void updateName() {
        if(unreadMessagesCount > 0) {
            this.setValue((String) (name + "(" + unreadMessagesCount + ")"));
        }else{
            this.setValue(name);
        }
    }

    public void decrementMessagesCount() {
        unreadMessagesCount--;
        updateName();
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public int getMessagesCount() {
        return messagesCount;
    }
}
