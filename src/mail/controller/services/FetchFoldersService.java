package mail.controller.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import mail.model.EmailTreeItem;
import mail.view.IconResolver;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.util.List;

public class FetchFoldersService extends Service<Void> {

    private Store store;
    private EmailTreeItem<String> foldersRoot;
    private List<Folder> folderList;

    private IconResolver iconResolver = new IconResolver();

    public FetchFoldersService(Store store, EmailTreeItem<String> foldersRoot, List<Folder> folders) {
        this.store = store;
        this.foldersRoot = foldersRoot;
        this.folderList = folders;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                fetchFolders();
                return null;
            }
        };
    }

    private void fetchFolders() throws MessagingException {
        Folder[] folders = store.getDefaultFolder().list();
        handleFolders(folders, foldersRoot);
    }

    private void handleFolders(Folder[] folders, EmailTreeItem<String> foldersRoot) throws MessagingException {
        for (Folder folder : folders) {
            folderList.add(folder);
            EmailTreeItem<String> emailTreeItem = new EmailTreeItem<String>(folder.getName());
            emailTreeItem.setGraphic(iconResolver.getFolderIcon(folder.getName()));
            foldersRoot.getChildren().add((emailTreeItem));
            fetchMessagesOnFolder(folder, emailTreeItem);
            foldersRoot.setExpanded(true);
            addMessageListenerToFolder(folder,emailTreeItem);
            if(folder.getType() == Folder.HOLDS_FOLDERS){
                Folder[] subFolders = folder.list();
                handleFolders(subFolders,emailTreeItem);
            }
        }

    }

    //Listen to new messages and add it to the top
    private void addMessageListenerToFolder(Folder folder, EmailTreeItem<String> emailTreeItem) {
        folder.addMessageCountListener(new MessageCountListener() {
            @Override
            public void messagesAdded(MessageCountEvent messageCountEvent) {
                System.out.println("Message added: " + messageCountEvent);
                for(int i = 0;i < messageCountEvent.getMessages().length; i++){
                    try{
                        Message message = folder.getMessage(folder.getMessageCount() - i);
                        emailTreeItem.addMessageToTop(message);
                    }catch (MessagingException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void messagesRemoved(MessageCountEvent messageCountEvent) {
                System.out.println("Message removed: " + messageCountEvent);
            }
        });
    }

    private void fetchMessagesOnFolder(Folder folder, EmailTreeItem<String> emailTreeItem) {
        Service fetchMessageService = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if(folder.getType() != Folder.HOLDS_FOLDERS){
                            folder.open(Folder.READ_WRITE);
                            int folderSize = folder.getMessageCount();
                            for(int i = folderSize; i > 0;i--){
                                emailTreeItem.addMessage(folder.getMessage(i));
                                System.out.println(folder.getMessage(i).getSubject() + " " + folder.getMessage(i).getFrom()[0].toString());
                                updateProgress(i,folderSize);
                            }
                        }
                        return null;
                    }
                };
            }
        };
        fetchMessageService.start();
    }
}