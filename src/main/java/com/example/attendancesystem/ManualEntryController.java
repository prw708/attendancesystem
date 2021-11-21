package com.example.attendancesystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualEntryController {

    @FXML private Label errors;
    @FXML private DatePicker checkinDatePicker;
    @FXML private DatePicker checkoutDatePicker;
    @FXML private TextField checkinTime;
    @FXML private TextField checkoutTime;
    private boolean valid;
    private JDBCPostgreSQLConnect sqlConnect;
    private ValidateInputs validate;

    public ManualEntryController() {
        sqlConnect = new JDBCPostgreSQLConnect();
        validate = new ValidateInputs();
        valid = false;
    }

    @FXML
    public void initialize() {
        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonthValue();
        int day = LocalDateTime.now().getDayOfMonth();
        int hours = LocalDateTime.now().getHour();
        int minutes = LocalDateTime.now().getMinute();
        String hoursString = Integer.toString(hours);
        String minutesString = Integer.toString(minutes);
        if (hours < 10) {
            hoursString = "0" + hours;
        }
        if (minutes < 10) {
            minutesString = "0" + minutes;
        }
        checkinDatePicker.setValue(LocalDate.of(year, month, day));
        checkinTime.setText(hoursString + ":" + minutesString + ":00");
        checkoutDatePicker.setValue(LocalDate.of(year, month, day));
        checkoutTime.setText(hoursString + ":" + minutesString + ":00");
    }

    @FXML
    protected void onCancelButtonClick(ActionEvent event) throws IOException {
        CalendarController.setCurrentPopup(false);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void onEnterButtonClick(ActionEvent event) throws IOException {
        valid = validate.validateDate(checkinDatePicker.getEditor().getText()) &&
                validate.validateDate(checkoutDatePicker.getEditor().getText()) &&
                validate.validateTime(checkinTime.getText()) &&
                validate.validateTime(checkoutTime.getText());
        if (valid) {
            errors.setVisible(false);
            Pattern datePattern = Pattern.compile("^([0-9]{1,2})/([0-9]{1,2})/([0-9]{1,4})$");
            Matcher checkinDateMatcher = datePattern.matcher(checkinDatePicker.getEditor().getText());
            int checkinYear = 0, checkinMonth = 0, checkinDay = 0;
            if (checkinDateMatcher.matches()) {
                checkinMonth = Integer.parseInt(checkinDateMatcher.group(1));
                checkinDay = Integer.parseInt(checkinDateMatcher.group(2));
                checkinYear = Integer.parseInt(checkinDateMatcher.group(3));
            }
            Matcher checkoutDateMatcher = datePattern.matcher(checkoutDatePicker.getEditor().getText());
            int checkoutYear = 0, checkoutMonth = 0, checkoutDay = 0;
            if (checkoutDateMatcher.matches()) {
                checkoutMonth = Integer.parseInt(checkoutDateMatcher.group(1));
                checkoutDay = Integer.parseInt(checkoutDateMatcher.group(2));
                checkoutYear = Integer.parseInt(checkoutDateMatcher.group(3));
            }
            Pattern timePattern = Pattern.compile("^([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})$");
            Matcher checkinTimeMatcher = timePattern.matcher(checkinTime.getText());
            int checkinHours = 0, checkinMinutes = 0, checkinSeconds = 0;
            if (checkinTimeMatcher.matches()) {
                checkinHours = Integer.parseInt(checkinTimeMatcher.group(1));
                checkinMinutes = Integer.parseInt(checkinTimeMatcher.group(2));
                checkinSeconds = Integer.parseInt(checkinTimeMatcher.group(3));
            }
            Matcher checkoutTimeMatcher = timePattern.matcher(checkoutTime.getText());
            int checkoutHours = 0, checkoutMinutes = 0, checkoutSeconds = 0;
            if (checkoutTimeMatcher.matches()) {
                checkoutHours = Integer.parseInt(checkoutTimeMatcher.group(1));
                checkoutMinutes = Integer.parseInt(checkoutTimeMatcher.group(2));
                checkoutSeconds = Integer.parseInt(checkoutTimeMatcher.group(3));
            }
            try {
                LocalDateTime checkin = LocalDateTime.of(
                        checkinYear, checkinMonth, checkinDay,
                        checkinHours, checkinMinutes, checkinSeconds);
                LocalDateTime checkout = LocalDateTime.of(
                        checkoutYear, checkoutMonth, checkoutDay,
                        checkoutHours, checkoutMinutes, checkoutSeconds);
                if (checkout.isAfter(checkin)) {
                    if (sqlConnect.timeEntry(CalendarController.getLoggedInAs(), checkin, checkout)) {
                        CalendarController.setCurrentPopup(false);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();
                    }
                } else {
                    errors.setVisible(true);
                    errors.setText("Check-out must be ahead of check-in.");
                }
            } catch (DateTimeException e) {
                errors.setVisible(true);
                errors.setText("Invalid date and time.");
            }
        } else {
            errors.setVisible(true);
            errors.setText(validate.getError());
        }
    }

}
