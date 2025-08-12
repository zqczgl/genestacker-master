package org.ugent.caagt.genestacker.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.ugent.caagt.genestacker.gui.GeneStackerGUI;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainController {
    
    // File selection fields
    @FXML
    private TextField inputFileField;
    
    @FXML
    private TextField outputFileField;
    
    // Parameter fields
    @FXML
    private TextField generationsField;
    
    @FXML
    private TextField successRateField;
    
    @FXML
    private TextField lpaField;
    
    @FXML
    private TextField populationField;
    
    // Checkbox
    @FXML
    private CheckBox homozygousIdeotypeParentsCheckbox;
    
    // Output area
    @FXML
    private TextArea outputArea;
    
    // File choosers
    private FileChooser fileChooser = new FileChooser();
    
    @FXML
    private void handleBrowseInput(ActionEvent event) {
        File file = fileChooser.showOpenDialog(inputFileField.getScene().getWindow());
        if (file != null) {
            inputFileField.setText(file.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleBrowseOutput(ActionEvent event) {
        File file = fileChooser.showSaveDialog(outputFileField.getScene().getWindow());
        if (file != null) {
            outputFileField.setText(file.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleRun(ActionEvent event) {
        // Clear previous output
        outputArea.clear();
        
        try {
            // Validate inputs
            String inputFile = inputFileField.getText();
            String outputFile = outputFileField.getText();
            
            if (inputFile.isEmpty() || outputFile.isEmpty()) {
                showAlert("Error", "Please select both input and output files.");
                return;
            }
            
            // Build command line arguments
            List<String> argsList = new ArrayList<>();
            
            // Required parameters
            argsList.add("-g");
            argsList.add(generationsField.getText());
            
            argsList.add("-s");
            argsList.add(successRateField.getText());
            
            argsList.add(inputFile);
            argsList.add(outputFile);
            
            // Optional parameters
            if (!lpaField.getText().isEmpty()) {
                argsList.add("-lpa");
                argsList.add(lpaField.getText());
            }
            
            if (!populationField.getText().isEmpty()) {
                argsList.add("-p");
                argsList.add(populationField.getText());
            }
            
            if (homozygousIdeotypeParentsCheckbox.isSelected()) {
                argsList.add("-hip");
            }
            
            // Convert to array
            String[] args = argsList.toArray(new String[0]);
            
            // Run Gene Stacker in a separate thread to prevent UI freezing
            Thread geneStackerThread = new Thread(() -> {
                try {
                    // Capture system output
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    java.io.PrintStream ps = new java.io.PrintStream(baos);
                    java.io.PrintStream old = System.out;
                    System.setOut(ps);
                    
                    // Run Gene Stacker
                    GeneStackerGUI.runGeneStacker(args);
                    
                    // Restore standard output
                    System.out.flush();
                    System.setOut(old);
                    
                    // Update UI with output
                    javafx.application.Platform.runLater(() -> {
                        outputArea.appendText(baos.toString());
                        outputArea.appendText("\nGene Stacker execution completed.\n");
                        
                        // Try to open the result PDF file
                        openResultPDF(outputFile);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        outputArea.appendText("Error running Gene Stacker: " + e.getMessage() + "\n");
                        e.printStackTrace();
                    });
                }
            });
            
            geneStackerThread.start();
            
        } catch (Exception e) {
            outputArea.appendText("Error running Gene Stacker: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    /**
     * Attempts to open the first PDF file found in the output ZIP file
     */
    private void openResultPDF(String outputZipFilePath) {
        try {
            File zipFile = new File(outputZipFilePath);
            if (!zipFile.exists()) {
                outputArea.appendText("Output file not found: " + outputZipFilePath + "\n");
                return;
            }
            
            // Create a temporary directory to extract the PDF
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "genestacker_" + System.currentTimeMillis());
            tempDir.mkdirs();
            
            // Extract the first PDF file from the ZIP
            String pdfFileName = null;
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".pdf")) {
                        pdfFileName = entry.getName();
                        File pdfFile = new File(tempDir, pdfFileName.substring(pdfFileName.lastIndexOf('/') + 1));
                        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        break; // Just extract the first PDF
                    }
                }
            }
            
            if (pdfFileName != null) {
                File extractedPdf = new File(tempDir, pdfFileName.substring(pdfFileName.lastIndexOf('/') + 1));
                if (extractedPdf.exists()) {
                    // Try to open the PDF file
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.OPEN)) {
                            desktop.open(extractedPdf);
                            outputArea.appendText("Opened result PDF: " + extractedPdf.getAbsolutePath() + "\n");
                        } else {
                            outputArea.appendText("Cannot open PDF file. Desktop opening not supported.\n");
                        }
                    } else {
                        outputArea.appendText("Cannot open PDF file. Desktop not supported.\n");
                    }
                } else {
                    outputArea.appendText("Failed to extract PDF file from ZIP.\n");
                }
            } else {
                outputArea.appendText("No PDF file found in the output ZIP.\n");
            }
        } catch (IOException e) {
            outputArea.appendText("Error opening result PDF: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}