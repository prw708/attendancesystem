package com.example.attendancesystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import java.io.IOException;

public class LoginController {
    @FXML Label errors;
    @FXML TextField username;
    @FXML TextField password;
    private JDBCPostgreSQLConnect sqlConnect;
    private ValidateInputs validate;
    private boolean valid;

    public LoginController() {
        sqlConnect = new JDBCPostgreSQLConnect();
        validate = new ValidateInputs();
        valid = false;
    }

    @FXML
    protected void onRegisterButtonClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("register.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.getIcons().add(new Image("attendanceicon.png"));
        stage.show();
        stage.centerOnScreen();
    }

    @FXML
    protected void onLoginButtonClick(ActionEvent event) throws IOException {
        valid = validate.validateUsername(username.getText()) &&
                validate.validatePassword(password.getText());
        if (valid) {
            Boolean loggedIn = sqlConnect.login(
                    username.getText(),
                    password.getText());
            if (!loggedIn) {
                errors.setVisible(true);
                errors.setText("Invalid username or password.");
            } else {
                errors.setVisible(false);
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
            }
        } else {
            errors.setVisible(true);
            errors.setText(validate.getError());
        }
    }
}