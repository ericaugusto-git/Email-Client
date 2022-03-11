package mail;

import javafx.application.Application;
import javafx.stage.Stage;
import mail.controller.persistence.AccountRememberPreference;
import mail.controller.persistence.PersistenceAccess;
import mail.controller.persistence.ValidAccount;
import mail.controller.services.LoginService;
import mail.model.EmailAccount;
import mail.view.ViewFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    private EmailManager emailManager = new EmailManager();
    private PersistenceAccess persistenceAccess = new PersistenceAccess();

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewFactory viewFactory = new ViewFactory(emailManager);
        List<ValidAccount> validAccounts = persistenceAccess.loadValidAccounts();
        boolean automaticLogin = persistenceAccess.readAccountPreference();
        System.out.println(automaticLogin);
        if (validAccounts.size() > 0) {
            for (ValidAccount validAccount : validAccounts) {
                if (automaticLogin == true) {
                    EmailAccount emailAccount = new EmailAccount(validAccount.getAddress(), validAccount.getPassword());
                    LoginService loginService = new LoginService(emailAccount, emailManager);
                    loginService.start();
                    loginService.setOnSucceeded(e -> {
                        emailManager.addEmailAccount(emailAccount);
                    });
                }
            }
            viewFactory.showMainWindow();
        } else {
            viewFactory.showLoginWindow(true);
        }
        viewFactory.updateStyles();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        List<ValidAccount> validAccounts = new ArrayList<>();
        boolean automaticLogin = persistenceAccess.readAccountPreference();
        for (EmailAccount emailAccount : emailManager.getEmailAccounts()) {
            if (automaticLogin == true) {
                validAccounts.add(new ValidAccount(emailAccount.getAddress(), emailAccount.getPassword()));
                System.out.println(emailAccount.getAddress());
            }
        }
        persistenceAccess.saveValidAccounts(validAccounts);
    }
}
