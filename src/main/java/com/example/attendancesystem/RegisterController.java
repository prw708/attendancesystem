package com.example.attendancesystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

public class RegisterController {
    @FXML private Label errors;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    private boolean valid;
    private JDBCPostgreSQLConnect sqlConnect;
    private ValidateInputs validate;

    public RegisterController() {
        sqlConnect = new JDBCPostgreSQLConnect();
        validate = new ValidateInputs();
        valid = false;
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.getIcons().add(new Image("attendanceicon.png"));
        stage.show();
        stage.centerOnScreen();
    }

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException {
        valid = validate.validateFirstName(firstName.getText()) &&
                validate.validateLastName(lastName.getText()) &&
                validate.validateNewUsername(username.getText()) &&
                validate.validatePasswords(password.getText(), confirmPassword.getText());
        if (valid) {
            errors.setVisible(false);
            boolean registered = sqlConnect.register(
                    firstName.getText(),
                    lastName.getText(),
                    username.getText(),
                    password.getText());
            if (registered) {
                FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("calendar.fxml"));
                String usernameString = username.getText();
                String firstName = sqlConnect.getFirstName(usernameString);
                boolean admin = sqlConnect.getAdmin(usernameString);
                Scene scene = new Scene(fxmlLoader.load());
                CalendarController.setLoggedInAs(usernameString);
                CalendarController.setFirstName(firstName);
                CalendarController.setAdmin(admin);
                scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
                stage = new Stage();
                stage.setTitle("Attendance System");
                stage.getIcons().add(new Image("attendanceicon.png"));
                stage.setScene(scene);
                stage.show();
                stage.setMaximized(true);
            } else {
                errors.setVisible(true);
                errors.setText("Registration failed.");
            }
        } else {
            errors.setVisible(true);
            errors.setText(validate.getError());
        }
    }
}