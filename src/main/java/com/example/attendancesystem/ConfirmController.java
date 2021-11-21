package com.example.attendancesystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ConfirmController {

    @FXML private Label title;
    @FXML private Label mainContent;

    public void setTitle(String content) {
        title.setText(content);
    }

    public void setMainContent(String content) {
        mainContent.setText(content);
    }

    @FXML
    protected void onOkButtonClick(ActionEvent event) throws IOException {
        CalendarController.setCurrentPopup(false);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}
