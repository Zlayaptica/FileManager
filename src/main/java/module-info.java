module com.example.filemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.filemanager to javafx.fxml;
    exports com.example.filemanager;
}