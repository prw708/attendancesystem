package com.example.attendancesystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

public class ChangePasswordController {

    @FXML private Label errors;
    @FXML private PasswordField currentPassword;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    private boolean valid;
    private JDBCPostgreSQLConnect sqlConnect;
    private ValidateInputs validate;

    public ChangePasswordController() {
        sqlConnect = new JDBCPostgreSQLConnect();
        validate = new ValidateInputs();
        valid = false;
    }

    @FXML
    protected void onCancelButtonClick(ActionEvent event) throws IOException {
        CalendarController.setCurrentPopup(false);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onChangeButtonClick(ActionEvent event) throws IOException {
        valid = validate.validatePassword(currentPassword.getText()) &&
                validate.validateNewPasswords(password.getText(), confirmPassword.getText());
        if (valid) {
            errors.setVisible(false);
            boolean changed = sqlConnect.changePassword(CalendarController.getLoggedInAs(), currentPassword.getText(), password.getText());
            if (changed) {
                FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("confirm.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
                ConfirmController controller = fxmlLoader.getController();
                controller.setTitle("Password Change Confirmed");
                controller.setMainContent("Your password has been changed successfully.");
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Password Change Confirmed");
                stage.getIcons().add(new Image("attendanceicon.png"));
                stage.show();
                stage.centerOnScreen();
                stage.setAlwaysOnTop(true);
                Stage existingStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                existingStage.close();
            } else {
                errors.setVisible(true);
                errors.setText("Invalid password.");
            }
        } else {
            errors.setVisible(true);
            errors.setText(validate.getError());
        }
    }

}
