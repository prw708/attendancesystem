package com.example.attendancesystem;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CalendarController {

    @FXML StackPane calendarOverlap;
    @FXML GridPane calendar;
    @FXML ScrollPane times;
    @FXML Label fxMonth;
    @FXML Label fxName;
    @FXML Button selectUser;
    @FXML Button resetPassword;
    @FXML Button deleteUser;
    @FXML Button resetData;
    private JDBCPostgreSQLConnect sqlConnect;
    private static String loggedInAs;
    private static String firstName;
    private static boolean admin;
    private static boolean checkedIn;
    private GregorianCalendar gregorianCalendar;
    private int year;
    private int month;
    private int day;
    private int selectedDay;
    private ArrayList<BigDecimal> totalTimes;
    private static boolean currentPopup;

    public CalendarController() {
        sqlConnect = new JDBCPostgreSQLConnect();
        admin = false;
        checkedIn = false;
        gregorianCalendar = new GregorianCalendar();
        year = gregorianCalendar.get(Calendar.YEAR);
        month = gregorianCalendar.get(Calendar.MONTH);
        day = 1;
        selectedDay = LocalDate.now().getDayOfMonth();
        gregorianCalendar.set(Calendar.DATE, 1);
        totalTimes = null;
        currentPopup = false;
    }

    @FXML
    public void initialize() {
        if (totalTimes == null) {
            addLoading();
        }
        calendar.getChildren().clear();
        fxMonth.setText(
                gregorianCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + year
        );
        Platform.runLater(() -> {
            fxName.setText("Hi, " + firstName);
            if (totalTimes == null) {
                admin = sqlConnect.getAdmin(loggedInAs);
                checkedIn = sqlConnect.getCheckedIn(loggedInAs);
                update();
                selectUser.setVisible(admin);
                selectUser.setManaged(admin);
                resetPassword.setVisible(admin);
                resetPassword.setManaged(admin);
                deleteUser.setVisible(admin);
                deleteUser.setManaged(admin);
                resetData.setVisible(admin);
                resetData.setManaged(admin);
                removeLoading();
            }
        });
    }

    public void addLoading() {
        Label label = new Label("Loading...");
        label.getStyleClass().add("h3");
        calendar.getChildren().clear();
        calendarOverlap.getChildren().add(label);
    }

    public void removeLoading() {
        calendarOverlap.getChildren().remove(1);
    }

    public void update() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                getTotalTimesByMonth();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateTotalTimes();
        });
        new Thread(task).run();
        if (totalTimes != null) {
            initialize();
        }
        updateDetailTimes(loggedInAs, selectedDay, month, year);
    }

    public void updateTotalTimes() {
        int x = 0;
        int y = -1;
        int daysOfMonth = 1;
        YearMonth ym = YearMonth.of(year, month + 1);
        int daysInMonth = ym.lengthOfMonth();
        int dayOfWeek = gregorianCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        day = LocalDate.now().getDayOfMonth();
        for (int i = 1; i <= 42; i++) {
            if (x % 7 == 0) {
                x = 0;
                y += 1;
            }
            if (i > dayOfWeek  && i <= (daysInMonth + dayOfWeek)) {
                Pane pane = new Pane();
                VBox vbox = new VBox();
                pane.getStyleClass().add("cell-fill");
                if ((day == (i - dayOfWeek) && month == YearMonth.now().getMonthValue() - 1) && selectedDay == -1) {
                    pane.getStyleClass().add("cell-fill-current");
                } else if (selectedDay == (i - dayOfWeek)) {
                    pane.getStyleClass().add("cell-fill-current");
                }
                Label dayLabel = new Label(Integer.toString(daysOfMonth));
                dayLabel.getStyleClass().add("h3");
                dayLabel.getStyleClass().add("cell-padding");
                vbox.getChildren().add(dayLabel);
                pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        Node clickedNode = (Node) mouseEvent.getSource();
                        int columnIndex = GridPane.getColumnIndex(clickedNode);
                        int rowIndex = GridPane.getRowIndex(clickedNode);
                        selectedDay = (rowIndex * 7) + columnIndex - dayOfWeek + 1;
                        initialize();
                        updateDetailTimes(loggedInAs, selectedDay, month, year);
                        updateTotalTimes();
                    }
                });
                if (!totalTimes.get(daysOfMonth - 1).equals(new BigDecimal("0.0"))) {
                    HBox hbox = new HBox();
                    Label hoursLabel = new Label(totalTimes.get(daysOfMonth - 1).toString() + " hours");
                    hoursLabel.getStyleClass().add("lbl");
                    hoursLabel.getStyleClass().add("alert-info");
                    hbox.getStyleClass().add("lbl-padding");
                    hbox.getChildren().add(hoursLabel);
                    vbox.getChildren().add(hbox);
                }
                pane.getChildren().add(vbox);
                calendar.add(pane, x, y);
                daysOfMonth += 1;
            } else {
                Pane pane = new Pane();
                pane.getStyleClass().add("cell-empty");
                calendar.add(pane, x, y);
            }
            x += 1;
        }
    }

    public void updateDetailTimes(String username, int day, int month, int year) {
        HashMap<Integer, ArrayList<LocalDateTime>> timesList = (day == -1) ? null : sqlConnect.getAttendance(username, day, month, year);
        GridPane overall = new GridPane();
        int rowIndex = 0;
        // Handle days with no times
        if (timesList == null || timesList.isEmpty()) {
            Label label = new Label("No entries to show.");
            VBox vbox = new VBox();
            vbox.getChildren().add(label);
            overall.add(vbox, 0, 0);
        } else {
            // Place times
            List sortedKeys = new ArrayList(timesList.keySet());
            Collections.sort(sortedKeys);
            for (Object key : sortedKeys) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
                GridPane gridPane = new GridPane();
                if (timesList.get(key).get(0) != null) {
                    String formattedTime = timesList.get(key).get(0).format(formatter);
                    Label label = new Label("In: ");
                    Label time = new Label(formattedTime);
                    label.getStyleClass().add("text-success");
                    time.getStyleClass().add("text-success");
                    label.setMinWidth(40);
                    gridPane.add(label, 0, 0);
                    gridPane.add(time, 1, 0);
                }
                if (timesList.get(key).get(1) != null) {
                    String formattedTime = timesList.get(key).get(1).format(formatter);
                    Label label = new Label("Out: ");
                    Label time = new Label(formattedTime);
                    label.getStyleClass().add("text-danger");
                    time.getStyleClass().add("text-danger");
                    label.setMinWidth(40);
                    gridPane.add(label, 0, 1);
                    gridPane.add(time, 1, 1);
                }
                // Delete Button
                GridPane gridPaneTimeEntry = new GridPane();
                Button btn = new Button("Delete");
                btn.getStyleClass().add("btn");
                btn.getStyleClass().add("btn-danger");
                gridPaneTimeEntry.add(btn, 1, 0);
                btn.setId(key.toString());
                btn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        checkedIn = false;
                        boolean delete = sqlConnect.deleteTime((Integer) key);
                        update();
                    }
                });
                // Add to overall GridPane
                overall.getStyleClass().add("time-entry");
                overall.add(gridPane, 0, rowIndex);
                overall.add(gridPaneTimeEntry, 1, rowIndex);
                rowIndex++;
            }
        }
        times.setContent(overall);
    }

    public void getTotalTimesByMonth() {
        totalTimes = calculateTotalTimesByMonth(month, year);
    }

    public ArrayList<BigDecimal> calculateTotalTimesByMonth(int month, int year) {
        ArrayList<BigDecimal> totals = new ArrayList<>();
        HashMap<LocalDateTime, HashMap<Integer, ArrayList<LocalDateTime>>> timesList = sqlConnect.getAttendanceByMonth(loggedInAs, month, year);
        YearMonth ym = YearMonth.of(year, month + 1);
        int daysInMonth = ym.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDateTime curr = LocalDateTime.of(ym.getYear(), ym.getMonthValue(), i, 0, 0);
            BigDecimal total = new BigDecimal(0);
            if (timesList.containsKey(curr)) {
                for (Object id : timesList.get(curr).keySet()) {
                    if (timesList.get(curr).get(id).get(0) != null && timesList.get(curr).get(id).get(1) != null) {
                        BigDecimal t = new BigDecimal(timesList.get(curr).get(id).get(0).until(timesList.get(curr).get(id).get(1), ChronoUnit.SECONDS));
                        total = total.add(t);
                    }
                }
            }
            total = total.divide(new BigDecimal(3600), 1, RoundingMode.HALF_UP);
            totals.add(total);
        }
        return totals;
    }

    public static void setLoggedInAs(String user) {
        loggedInAs = user;
    }

    public static String getLoggedInAs() {
        return loggedInAs;
    }

    public static void setFirstName(String name) {
        firstName = name;
    }

    public static void setAdmin(boolean a) {
        admin = a;
    }

    public static boolean getAdmin() {
        return admin;
    }

    public static void setCurrentPopup(boolean s) {
        currentPopup = s;
    }

    @FXML
    protected void onLogoutButtonClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        setLoggedInAs(null);
        setFirstName(null);
        setAdmin(false);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        stage = new Stage();
        stage.getIcons().add(new Image("attendanceicon.png"));
        stage.setScene(scene);
        stage.setTitle("Attendance System");
        stage.show();
        stage.centerOnScreen();
        stage.setMaximized(false);
    }

    @FXML
    protected void onResetDataButtonClick(ActionEvent event) throws IOException {
        if (!currentPopup && CalendarController.getAdmin()) {
            currentPopup = true;
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("reset-data.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reset Data");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.setOnHiding(hidingEvent -> {
                update();
                sqlConnect.deleteFiles();
                if (loggedInAs == null) {
                    FXMLLoader loginfxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("login.fxml"));
                    Scene loginScene = null;
                    try {
                        loginScene = new Scene(loginfxmlLoader.load());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loginScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                    loginScene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
                    Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    loginStage.close();
                    loginStage = new Stage();
                    loginStage.setScene(loginScene);
                    loginStage.setTitle("Attendance System");
                    loginStage.getIcons().add(new Image("attendanceicon.png"));
                    loginStage.show();
                    loginStage.centerOnScreen();
                    loginStage.setMaximized(false);
                }
            });
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    currentPopup = false;
                }
            });
        }
    }

    @FXML
    protected void onBackButtonClick(ActionEvent event) throws IOException {
        if (month - 1 < 0) {
            month = 11;
            year = year - 1;
        } else {
            month -= 1;
        }
        day = 1;
        if (month == YearMonth.now().getMonthValue() - 1 &&
                year == YearMonth.now().getYear()) {
            selectedDay = LocalDate.now().getDayOfMonth();
        } else {
            selectedDay = -1;
        }
        gregorianCalendar.set(Calendar.YEAR, year);
        gregorianCalendar.set(Calendar.MONTH, month);
        gregorianCalendar.set(Calendar.DATE, day);
        update();
    }

    @FXML
    protected void onCurrentButtonClick(ActionEvent event) throws IOException {
        month = YearMonth.now().getMonthValue() - 1;
        year = YearMonth.now().getYear();
        day = 1;
        selectedDay = LocalDate.now().getDayOfMonth();
        gregorianCalendar.set(Calendar.YEAR, year);
        gregorianCalendar.set(Calendar.MONTH, month);
        gregorianCalendar.set(Calendar.DATE, day);
        update();
    }

    @FXML
    protected void onForwardButtonClick(ActionEvent event) throws IOException {
        if (month + 1 > 11) {
            month = 0;
            year = year + 1;
        } else {
            month += 1;
        }
        day = 1;
        if (month == YearMonth.now().getMonthValue() - 1 &&
                year == YearMonth.now().getYear()) {
            selectedDay = LocalDate.now().getDayOfMonth();
        } else {
            selectedDay = -1;
        }
        gregorianCalendar.set(Calendar.YEAR, year);
        gregorianCalendar.set(Calendar.MONTH, month);
        gregorianCalendar.set(Calendar.DATE, day);
        update();
    }

    @FXML
    protected void onCheckInButtonClick(ActionEvent event) throws IOException {
        if (!checkedIn) {
            LocalDateTime time = sqlConnect.checkIn(loggedInAs);
            if (time != null) {
                checkedIn = true;
                updateDetailTimes(loggedInAs, selectedDay, month, year);
            }
        }
    }

    @FXML
    protected void onCheckOutButtonClick(ActionEvent event) throws IOException {
        if (checkedIn) {
            LocalDateTime time = sqlConnect.checkOut(loggedInAs);
            if (time != null) {
                checkedIn = false;
                update();
            }
        }
    }

    @FXML
    protected void onSelectUserButtonClick(ActionEvent event) throws IOException {
        if (!currentPopup && CalendarController.getAdmin()) {
            currentPopup = true;
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("select-user.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Select User");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.setOnHiding(hidingEvent -> {
                update();
            });
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    currentPopup = false;
                }
            });
        }
    }

    @FXML
    protected void onResetPasswordButtonClick(ActionEvent event) throws IOException {
        if (!currentPopup && CalendarController.getAdmin()) {
            currentPopup = true;
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("reset-password.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Reset Password");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    currentPopup = false;
                }
            });
        }
    }

    @FXML
    protected void onChangePasswordButtonClick(ActionEvent event) throws IOException {
        if (!currentPopup) {
            currentPopup = true;
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("change-password.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Change Password");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    currentPopup = false;
                }
            });
        }
    }

    @FXML
    protected void onManualButtonClick(ActionEvent event) throws IOException {
        if (!currentPopup) {
            currentPopup = true;
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("manual.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Date and Time Entry");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.setOnHiding(hidingEvent -> {
                update();
            });
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    currentPopup = false;
                }
            });
        }
    }

    @FXML
    protected void onDeleteUserButtonClick(ActionEvent event) throws IOException {
        if (!currentPopup) {
            currentPopup = true;
            FXMLLoader fxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("delete-user.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Delete User");
            stage.getIcons().add(new Image("attendanceicon.png"));
            stage.show();
            stage.centerOnScreen();
            stage.setOnHiding(hidingEvent -> {
                update();
                if (loggedInAs == null) {
                    FXMLLoader loginfxmlLoader = new FXMLLoader(AttendanceSystem.class.getResource("login.fxml"));
                    Scene loginScene = null;
                    try {
                        loginScene = new Scene(loginfxmlLoader.load());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loginScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                    loginScene.getStylesheets().add(getClass().getClassLoader().getResource("main.css").toExternalForm());
                    Stage loginStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    loginStage.close();
                    loginStage = new Stage();
                    loginStage.setScene(loginScene);
                    loginStage.setTitle("Attendance System");
                    loginStage.getIcons().add(new Image("attendanceicon.png"));
                    loginStage.show();
                    loginStage.centerOnScreen();
                    loginStage.setMaximized(false);
                }
            });
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    currentPopup = false;
                }
            });
        }
    }
}