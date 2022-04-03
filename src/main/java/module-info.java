module group.csci2020u_assignment2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens group.csci2020u_assignment2 to javafx.fxml;
    exports group.csci2020u_assignment2;
}