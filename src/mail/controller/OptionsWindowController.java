package mail.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import mail.EmailManager;
import mail.controller.persistence.PersistenceAccess;
import mail.view.ColorTheme;
import mail.view.FontSize;
import mail.view.ViewFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class OptionsWindowController extends BaseController implements Initializable {

    @FXML
    private Slider fontSizePicker;

    @FXML
    private ChoiceBox<ColorTheme> themePicker;


    @FXML
    private CheckBox automaticLogin;

    private PersistenceAccess persistenceAccess = new PersistenceAccess();

    public OptionsWindowController(EmailManager emailManager, ViewFactory viewFactory, String fxmlName) {
        super(emailManager, viewFactory, fxmlName);
    }

    @FXML
    void applyButton() {
        viewFactory.setColorTheme(themePicker.getValue());
        viewFactory.setFontSize(FontSize.values()[(int)fontSizePicker.getValue()]);
        persistenceAccess.saveAccountsLoginPreference(automaticLogin);
        viewFactory.updateStyles();
        cancelButton();
    }

    @FXML
    void cancelButton() {
        Stage stage = (Stage) themePicker.getScene().getWindow();
        viewFactory.closeStage(stage);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateThemePicker();
        populateColorPicker();
        automaticLogin.setSelected(persistenceAccess.readAccountPreference());
    }

    private void populateColorPicker() {
        fontSizePicker.setMin(0);
        fontSizePicker.setMax(FontSize.values().length - 1);
        fontSizePicker.setValue(viewFactory.getFontSize().ordinal());
        fontSizePicker.setMajorTickUnit(1);
        fontSizePicker.setMinorTickCount(0);
        fontSizePicker.setBlockIncrement(1);
        fontSizePicker.setSnapToTicks(true);
        fontSizePicker.setShowTickMarks(true);
        fontSizePicker.setShowTickLabels(true);
        fontSizePicker.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                int i = object.intValue();
                return FontSize.values()[i].toString();
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });
        fontSizePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            fontSizePicker.setValue(newVal.intValue());
        });
    }

    private void populateThemePicker() {
        themePicker.setItems(FXCollections.observableList(Arrays.asList(ColorTheme.values())));
        themePicker.setValue(viewFactory.getColorTheme());
    }
}
