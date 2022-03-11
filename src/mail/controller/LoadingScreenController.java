package mail.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import mail.EmailManager;
import mail.view.ViewFactory;

public class LoadingScreenController extends BaseController{

    @FXML
    private Label attachmentName;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressTitleLabel;

    @FXML
    private Label progressNumber;
    @FXML
    private Label progressTotal;
    private int progressCount = 1;

    public LoadingScreenController(EmailManager emailManager, ViewFactory viewFactory, String fxmlName) {
        super(emailManager, viewFactory, fxmlName);
    }

    @FXML
    public void initialize(){

    }

    public void setUpProgressBar(Task task, int taskTotal) {
        progressBar.progressProperty().bind(task.progressProperty());
        if(taskTotal == 1){
            progressTotal.setText("");
            progressNumber.setText("");
        }else {
            progressTotal.setText(taskTotal + "");
            task.progressProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                    if (t1.doubleValue() > 0) {
                        progressCount++;
                        progressNumber.setText(progressCount + "/");
                    }
                }
            });
        }
        task.setOnSucceeded(e -> viewFactory.closeStage( (Stage) progressTitleLabel.getScene().getWindow()));
    }
}
