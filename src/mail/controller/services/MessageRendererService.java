package mail.controller.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.web.WebEngine;
import mail.model.EmailMessage;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.security.Provider;

public class MessageRendererService extends Service {

    private EmailMessage emailMessage;
    private WebEngine webEngine;
    private StringBuffer stringBuffer;

    public MessageRendererService(WebEngine webEngine) {
        this.webEngine = webEngine;
        stringBuffer = new StringBuffer();
        this.setOnSucceeded(e -> {
            displayMessage();
        });
    }

    public void setEmailMessage(EmailMessage emailMessage) {
        this.emailMessage = emailMessage;
    }

    private void displayMessage() {
        webEngine.loadContent(stringBuffer.toString());
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    laodMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private void laodMessages() throws MessagingException, IOException {
        stringBuffer.setLength(0);
        Message message = emailMessage.getMessage();
        String contentType = message.getContentType();
        if (isSimpleType(contentType)) {
            stringBuffer.append(message.getContent().toString());
        } else if (isMultipartType(contentType)) {
            Multipart multipart = (Multipart) message.getContent();
            loadMultipart(multipart, stringBuffer);
        }
    }

    private void loadMultipart(Multipart multipart, StringBuffer messageContent) throws MessagingException, IOException {
        for (int i = multipart.getCount() - 1; i >= 0; i--) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            String contentType = bodyPart.getContentType();
            if (isSimpleType(contentType)) {
                messageContent.append(bodyPart.getContent().toString());
            } else if (isMultipartType(contentType)) {
                Multipart multipart2 = (Multipart) bodyPart.getContent();
                loadMultipart(multipart2, messageContent);
            } else if(!isPlainText(contentType)){
                MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                emailMessage.addAttachment(mimeBodyPart);
            }
        }
    }

    private boolean isPlainText(String contentType){
        return contentType.contains("TEXT/PLAIN");
    }

    private boolean isMultipartType(String contentType) {
        return contentType.contains("multipart");
    }

    private boolean isSimpleType(String contentType) {
        return contentType.contains("TEXT/HTML") || contentType.contains("mixed")
                || contentType.contains("text");
    }
}
