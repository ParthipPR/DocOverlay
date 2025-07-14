package com.example;

import javafx.scene.control.TextField;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class PrimaryController {
    @FXML
    private Label titleLabel;
    @FXML
    private ListView<String> fileList;
    @FXML
    private VBox dropzone;
    @FXML
    private TextField scaleWidth;
    @FXML
    private TextField textX;
    @FXML
    private TextField textY;
    @FXML
    private Label templatePathLabel;
    
    private File selectedTemplateFile;

    private List<File> droppedFiles = new ArrayList<>();
    private final DocumentConverter converter = new DocumentConverter();
    private String title = "DocOverlay";

    @FXML
    public void initialize(){
        titleLabel.setText(title);
        handleDrop(dropzone);
    }


    @FXML
    private void clearList() throws IOException {
        fileList.getItems().clear();
    }

    
    private void handleDrop(VBox dropZone) {
         dropZone.setOnDragOver(event -> {
        if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    fileList.getItems().add(file.getAbsolutePath());
                    droppedFiles.add(file);
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    @FXML
    private void handleConvert(ActionEvent event) {
        try {
            int width = parseIntOrDefault(scaleWidth.getText(), 900);
            int x = parseIntOrDefault(textX.getText(), 20);
            int y = parseIntOrDefault(textY.getText(), 200);
    
            converter.setScaleWidth(width);
            converter.setTextX(x);
            converter.setTextY(y);
            converter.processFiles(droppedFiles); // or however you're managing selected files
    
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
        }
    }
    
    private int parseIntOrDefault(String value, int fallback) {
        return (value == null || value.isEmpty()) ? fallback : Integer.parseInt(value);
    }

@FXML
private void handleTemplateSelect() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Template Image");
    fileChooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
    );

    // Get the window from any control (like templatePathLabel)
    Window window = templatePathLabel.getScene().getWindow();
    File file = fileChooser.showOpenDialog(window);

    if (file != null) {
        selectedTemplateFile = file;
        templatePathLabel.setText(file.getName());

        // Set template path in the converter
        converter.setTemplatePath(file.getAbsolutePath());
    }
}

}
