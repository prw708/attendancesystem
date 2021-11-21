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

public class DeleteUserController {

    private JDBCPostgreSQLConnect sqlConnect;

    public DeleteUserController() {
        sqlConnect = new JDBCPostgreSQLConnect();
    }

    @FXML
    protected void onDeleteButtonClick(ActionEvent event) throws IOException {
        boolean delete = sqlConnect.deleteUser(CalendarController.getLoggedInAs()) && CalendarController.getAdmin();
        if (delete) {
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("confirm.fxml"));
            String user = sqlConnect.getAdminUser();
            String firstName = null;
            boolean admin = false;
            if (user != null) {
                firstName = sqlConnect.getFirstName(user);
                admin = sqlConnect.getAdmin(user);
            }
            CalendarController.setLoggedInAs(user);
            CalendarController.setFirstName(firstName);
            CalendarController.setAdmin(admin);
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            ConfirmController controller = fxmlLoader.getController();
            controller.setTitle("Delete User Confirmed");
            controller.setMainContent("The selected user has been deleted.");
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Delete User Confirmed");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("confirm.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            ConfirmController controller = fxmlLoader.getController();
            controller.setTitle("Delete User Failed");
            controller.setMainContent("The selected user has not been deleted because it is the admin, which can only be deleted last.");
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Delete User Failed");
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
