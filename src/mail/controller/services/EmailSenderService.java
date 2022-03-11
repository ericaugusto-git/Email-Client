package mail.controller.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import mail.controller.EmailSendingResult;
import mail.model.EmailAccount;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;

public class EmailSenderService extends Service<EmailSendingResult> {

    private EmailAccount emailAccount;
    private String subject;
    private String recipient;
    private String content;
    private List<File> attachments;

    public EmailSenderService(EmailAccount emailAccount, String subject, String recipient, String content, List<File> attachments) {
        this.emailAccount = emailAccount;
        this.subject = subject;
        this.recipient = recipient;
        this.content = content;
        this.attachments = attachments;
    }

    @Override
    protected Task<EmailSendingResult> createTask() {
        return new Task<EmailSendingResult>() {
            @Override
            protected EmailSendingResult call(){
                try{
                    //Set the message
                    MimeMessage mimeMessage = new MimeMessage(emailAccount.getSession());
                    mimeMessage.setFrom(emailAccount.getAddress());
                    mimeMessage.addRecipients(Message.RecipientType.TO, recipient);
                    mimeMessage.setSubject(subject);
                    //Set the content
                    Multipart multipart = new MimeMultipart();
                    BodyPart bodyPart = new MimeBodyPart();
                    bodyPart.setContent(content, "text/html");
                    multipart.addBodyPart(bodyPart);
                    mimeMessage.setContent(multipart);
                    //adding attachments:
                    if(attachments.size() > 0){
                        for ( File file: attachments){
                            MimeBodyPart mimeBodyPart = new MimeBodyPart();
                            //getting data from file
                            DataSource source = new FileDataSource(file.getAbsolutePath());
                            //setting the body part content with the above data
                            mimeBodyPart.setDataHandler(new DataHandler(source));
                            mimeBodyPart.setFileName(file.getName());
                            //adding to the message
                            multipart.addBodyPart(mimeBodyPart);
                        }
                    }
                    //Sending the message
                    Transport transport = emailAccount.getSession().getTransport();
                    transport.connect(
                            emailAccount.getProperties().getProperty("outgoingHost"),
                            emailAccount.getAddress(),
                            emailAccount.getPassword()
                    );
                    transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                    transport.close();
                    return EmailSendingResult.SUCCESS;
                } catch (MessagingException e) {
                    e.printStackTrace();
                    return EmailSendingResult.FAILED_BY_PROVIDER;
                }  catch (Exception e) {
                    e.printStackTrace();
                    return EmailSendingResult.FAILED_BY_UNEXPECTED_ERROR;
                }
            }
        };
    }
}
