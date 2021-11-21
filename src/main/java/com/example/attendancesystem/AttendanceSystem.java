package com.example.attendancesystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.util.Locale;

public class AttendanceSystem extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.getIcons().add(new Image("attendanceicon.png"));
        stage.setTitle("Attendance System");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            JDBCPostgreSQLConnect sqlConnect = new JDBCPostgreSQLConnect(args[0]);
        } else if (args.length != 0) {
            System.err.println("Passwords must be supplied or no arguments must be present.");
            System.exit(1);
        }
        Locale.setDefault(Locale.US);
        launch();
    }
}