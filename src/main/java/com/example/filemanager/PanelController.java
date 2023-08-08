package com.example.filemanager;

import com.example.filemanager.FileInfo.FileType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    ComboBox<String> disksBox;
    @FXML
    TextField pathField;
    private Path currentPath;

    public PanelController() {
    }

    public Path getCurrentPath() {
        return this.getCurrentPath();
    }

    public void initialize(URL location, ResourceBundle resources) {
        Image dirImage = new Image("/folder-icon.png");
        Image fileImage = new Image("/file-icon.png");
        TableColumn<FileInfo, ImageView> fileTypeColumn = new TableColumn();
        fileTypeColumn.setCellValueFactory((param) -> {
            return new SimpleObjectProperty(((FileInfo)param.getValue()).getType() == FileType.DIRECTORY ? new ImageView(dirImage) : new ImageView(fileImage));
        });
        fileTypeColumn.setPrefWidth(32.0);
        TableColumn<FileInfo, String> filenameColumn = new TableColumn("Название");
        filenameColumn.setCellValueFactory((param) -> {
            return new SimpleStringProperty(((FileInfo)param.getValue()).getFilename());
        });
        filenameColumn.setPrefWidth(300.0);
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn("Размер");
        fileSizeColumn.setCellValueFactory((param) -> {
            return new SimpleObjectProperty(((FileInfo)param.getValue()).getSize());
        });
        fileSizeColumn.setCellFactory((column) -> {
            return new TableCell<FileInfo, Long>() {
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }

                        this.setText(text);
                    } else {
                        this.setText((String)null);
                        this.setStyle("");
                    }

                }
            };
        });
        fileSizeColumn.setPrefWidth(120.0);
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn("Дата изменения");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        fileDateColumn.setCellValueFactory((param) -> {
            return new SimpleStringProperty(((FileInfo)param.getValue()).getLastModified().format(dtf));
        });
        fileDateColumn.setPrefWidth(120.0);
        this.filesTable.getColumns().addAll(new TableColumn[]{fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn});
        this.filesTable.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2 && this.filesTable.getSelectionModel().getSelectedItem() != null) {
                Path path = Paths.get(this.pathField.getText()).resolve(((FileInfo)this.filesTable.getSelectionModel().getSelectedItem()).getFilename());
                if (Files.isDirectory(path, new LinkOption[0])) {
                    this.updateList(path);
                }
            }

        });
        this.disksBox.getItems().clear();
        Iterator var10 = FileSystems.getDefault().getRootDirectories().iterator();

        while(var10.hasNext()) {
            Path p = (Path)var10.next();
            this.disksBox.getItems().add(p.toString());
        }

        this.disksBox.getSelectionModel().select(0);
        this.filesTable.getSortOrder().add(fileTypeColumn);
        this.updateList(Paths.get("."));
    }

    public void updateList() {
        this.updateList(this.currentPath);
    }

    public void updateList(Path path) {
        try {
            this.currentPath = path.normalize().toAbsolutePath();
            this.pathField.setText(this.currentPath.toString());
            this.filesTable.getItems().clear();
            this.filesTable.getItems().addAll((Collection)Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            this.filesTable.sort();
        } catch (IOException var4) {
            Alert alert = new Alert(AlertType.WARNING, "Yе удалось обновить список файлов", new ButtonType[]{ButtonType.OK});
            alert.showAndWait();
        }

    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(this.pathField.getText()).getParent();
        if (upperPath != null) {
            this.updateList(upperPath);
        }

    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox)actionEvent.getSource();
        this.updateList(Paths.get((String)element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFilename() {
        return !this.filesTable.isFocused() ? null : ((FileInfo)this.filesTable.getSelectionModel().getSelectedItem()).getFilename();
    }
}
