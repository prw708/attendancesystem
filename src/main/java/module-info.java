module com.example.attendancesystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires bcrypt;

    opens com.example.attendancesystem to javafx.fxml;
    exports com.example.attendancesystem;
}