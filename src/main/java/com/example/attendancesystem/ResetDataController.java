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

public class ResetDataController {

    private JDBCPostgreSQLConnect sqlConnect;

    public ResetDataController() {
        sqlConnect = new JDBCPostgreSQLConnect();
    }

    @FXML
    protected void onResetButtonClick(ActionEvent event) throws IOException {
        boolean reset = sqlConnect.resetData() && CalendarController.getAdmin();
        if (reset) {
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("confirm.fxml"));
            CalendarController.setLoggedInAs(null);
            CalendarController.setFirstName(null);
            CalendarController.setAdmin(false);
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            ConfirmController controller = fxmlLoader.getController();
            controller.setTitle("Reset Data Confirmed");
            controller.setMainContent("All the app's data has been deleted.");
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reset Data Confirmed");
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
