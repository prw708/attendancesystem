package com.example.attendancesystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

public class ResetPasswordController {

    private JDBCPostgreSQLConnect sqlConnect;

    public ResetPasswordController() {
        sqlConnect = new JDBCPostgreSQLConnect();
    }

    @FXML
    protected void onResetButtonClick(ActionEvent event) throws IOException {
        boolean reset = sqlConnect.resetPassword(CalendarController.getLoggedInAs()) && CalendarController.getAdmin();
        if (reset) {
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("confirm.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            ConfirmController controller = fxmlLoader.getController();
            controller.setTitle("Password Reset Confirmed");
            controller.setMainContent("Your temporary password is Temp1234. Please login and change your password.");
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Password Reset Confirmed");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onCancelButtonClick(ActionEvent event) throws IOException {
        CalendarController.setCurrentPopup(false);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}
