module com.example.ti3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ti3 to javafx.fxml;
    exports com.example.ti3;
}