package mail.view;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.mail.Folder;

public class IconResolver {

    public Node getFolderIcon(String folderName){
        String lowerCaseFolderName = folderName.toLowerCase();
        ImageView imageView;


        try {
            if (lowerCaseFolderName.contains("@")) {
                imageView = new ImageView(new Image("mail/view/icons/email.png"));
            } else if (lowerCaseFolderName.contains("inbox")) {
                imageView = new ImageView(new Image("mail/view/icons/inbox.png"));
            } else if (lowerCaseFolderName.contains("sent")) {
                imageView = new ImageView(new Image("mail/view/icons/sent2.png"));
            } else if (lowerCaseFolderName.contains("spam")) {
                imageView = new ImageView(new Image("mail/view/icons/spam.png"));
            } else {
                imageView = new ImageView(new Image("mail/view/icons/folder.png"));
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        return imageView;
    }
}
