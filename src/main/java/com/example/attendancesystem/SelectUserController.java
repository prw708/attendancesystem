package com.example.attendancesystem;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class SelectUserController {

    @FXML ChoiceBox users;
    private JDBCPostgreSQLConnect sqlConnect;
    private String selectedUser;
    private String selectedFirstName;

    public SelectUserController() {
        sqlConnect = new JDBCPostgreSQLConnect();
        selectedUser = null;
        selectedFirstName = null;
    }

    @FXML
    public void initialize() {
        int index = 0;
        int selectedIndex = 0;
        ArrayList<ArrayList<String>> allUsers = sqlConnect.getUsers();
        ObservableList<String> listOfNames = FXCollections.observableArrayList();
        for (ArrayList<String> user: allUsers) {
            listOfNames.add(user.get(1) + " " + user.get(2));
            if (user.get(0).equals(CalendarController.getLoggedInAs())) {
                selectedIndex = index;
                selectedUser = allUsers.get(selectedIndex).get(0);
                selectedFirstName = allUsers.get(selectedIndex).get(1);
            }
            index++;
        }
        users.setItems(listOfNames);
        users.setValue(allUsers.get(selectedIndex).get(1) + " " + allUsers.get(selectedIndex).get(2));
        users.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number value, Number new_value) {
                selectedUser = allUsers.get(new_value.intValue()).get(0);
                selectedFirstName = allUsers.get(new_value.intValue()).get(1);
            }
        });
    }

    @FXML
    protected void onSelectButtonClick(ActionEvent event) throws IOException {
        if (CalendarController.getAdmin()) {
            CalendarController.setLoggedInAs(selectedUser);
            CalendarController.setFirstName(selectedFirstName);
            CalendarController.setCurrentPopup(false);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    protected void onCancelButtonClick(ActionEvent event) throws IOException {
        CalendarController.setCurrentPopup(false);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
