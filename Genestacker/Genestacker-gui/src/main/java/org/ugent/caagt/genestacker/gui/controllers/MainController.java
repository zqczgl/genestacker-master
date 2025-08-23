package org.ugent.caagt.genestacker.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
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
    
    @FXML
    private TextField outputDirField;
    
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
    
    // Heuristic strategy components - stored from dialog
    private String presetMode = "balanced";
    private boolean h0Selected = true;
    private boolean h1aSelected = true;
    private boolean h1bSelected = false;
    private boolean h2aSelected = true;
    private boolean h2bSelected = false;
    private boolean h3Selected = false;
    private boolean h3s1Selected = true;
    private boolean h4Selected = true;
    private boolean h5Selected = true;
    private boolean h6Selected = true;
    private String maxCrossovers = "10";
    // Algorithm selection
    private boolean useMCTS = false;
    
    // UI components
    @FXML
    private Label heuristicsSummaryLabel;
    
    // Output area
    @FXML
    private TextArea outputArea;
    
    // File choosers
    private FileChooser fileChooser = new FileChooser();
    
    @FXML
    public void initialize() {
        // Initialize heuristic settings with default values
        updateHeuristicsSummary();
    }
    
    private void updateHeuristicsSummary() {
        String summary;
        if (useMCTS) {
            summary = "MCTS算法";
        } else {
            switch (presetMode) {
                case "balanced":
                    summary = "平衡模式 (默认)";
                    break;
                case "quality":
                    summary = "质量优先";
                    break;
                case "speed":
                    summary = "速度优先";
                    break;
                case "extremeSpeed":
                    summary = "极限速度";
                    break;
                default:
                    summary = "自定义设置";
            }
        }
        heuristicsSummaryLabel.setText(summary);
    }
    
    
    
    @FXML
    private void handleLightTheme(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/styles/modern.css").toExternalForm());
    }
    
    @FXML
    private void handleDarkTheme(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/styles/modern-dark.css").toExternalForm());
    }
    
    @FXML
    private void handleHeuristicsConfig(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/heuristics-dialog.fxml"));
            Parent root = loader.load();
            
            HeuristicsDialogController dialogController = loader.getController();
            
            // 将当前设置传递给对话框控制器
            dialogController.setPresetMode(presetMode);
            dialogController.setHeuristicOptions(
                h0Selected, h1aSelected, h1bSelected, h2aSelected, h2bSelected,
                h3Selected, h3s1Selected, h4Selected, h5Selected, h6Selected
            );
            dialogController.setMaxCrossovers(maxCrossovers);
            dialogController.setAlgorithm(useMCTS ? "mcts" : "branchandbound");
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("启发式策略配置");
            dialogStage.setScene(new Scene(root));
            
            // 设置弹窗的最小宽度
            dialogStage.setMinWidth(800);
            dialogStage.setMinHeight(600);
            
            // 应用当前主题到对话框
            Scene mainScene = ((Node) event.getSource()).getScene();
            if (mainScene.getStylesheets().size() > 0) {
                dialogStage.getScene().getStylesheets().addAll(mainScene.getStylesheets());
            }
            
            dialogStage.setResizable(true);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            
            dialogStage.showAndWait();
            
            // 如果用户确认了更改，则更新设置
            if (dialogController.isConfirmed()) {
                // 更新存储的设置
                if (dialogController.isBalancedModeSelected()) {
                    presetMode = "balanced";
                } else if (dialogController.isQualityModeSelected()) {
                    presetMode = "quality";
                } else if (dialogController.isSpeedModeSelected()) {
                    presetMode = "speed";
                } else if (dialogController.isExtremeSpeedModeSelected()) {
                    presetMode = "extremeSpeed";
                }
                
                // 更新算法选择
                useMCTS = dialogController.isMCTS();
                
                // 更新启发式选项
                h0Selected = dialogController.isH0Selected();
                h1aSelected = dialogController.isH1aSelected();
                h1bSelected = dialogController.isH1bSelected();
                h2aSelected = dialogController.isH2aSelected();
                h2bSelected = dialogController.isH2bSelected();
                h3Selected = dialogController.isH3Selected();
                h3s1Selected = dialogController.isH3s1Selected();
                h4Selected = dialogController.isH4Selected();
                h5Selected = dialogController.isH5Selected();
                h6Selected = dialogController.isH6Selected();
                
                maxCrossovers = dialogController.getMaxCrossovers();
                
                // 更新界面摘要
                updateHeuristicsSummary();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法打开启发式策略配置对话框: " + e.getMessage());
        }
    }
    
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
            outputFileField.setText(file.getName()); // Only store the file name, not the full path
            
            // Automatically set the output directory to the parent directory of the selected file
            File parentDir = file.getParentFile();
            if (parentDir != null) {
                outputDirField.setText(parentDir.getAbsolutePath());
            }
        }
    }
    
    @FXML
    private void handleBrowseOutputDir(ActionEvent event) {
        // Create a directory chooser
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("选择输出目录");
        
        // Set initial directory to the current output directory if it exists
        String outputDirPath = outputDirField.getText();
        if (!outputDirPath.isEmpty()) {
            File outputDir = new File(outputDirPath);
            if (outputDir.exists() && outputDir.isDirectory()) {
                dirChooser.setInitialDirectory(outputDir);
            }
        } else {
            // If no output directory is set, try to use the parent of the output file if it exists
            String outputFilePath = outputFileField.getText();
            if (!outputFilePath.isEmpty()) {
                File outputFile = new File(outputFilePath);
                File parentDir = outputFile.getParentFile();
                if (parentDir != null && parentDir.exists()) {
                    dirChooser.setInitialDirectory(parentDir);
                }
            }
        }
        
        File dir = dirChooser.showDialog(outputDirField.getScene().getWindow());
        if (dir != null) {
            outputDirField.setText(dir.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleRun(ActionEvent event) {
        // Clear previous output
        outputArea.clear();
        outputArea.appendText("Starting Gene Stacker execution...\n");
        outputArea.appendText("===============================\n");
        
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
            
            // Add heuristic strategy parameters
            // Preset modes
            if ("quality".equals(presetMode)) {
                argsList.add("--best");
            } else if ("speed".equals(presetMode)) {
                argsList.add("--faster");
            } else if ("extremeSpeed".equals(presetMode)) {
                argsList.add("--fastest");
            } else {
                // Individual heuristic options
                if (h0Selected) {
                    argsList.add("--filter-initial-plants");
                }
                if (h1aSelected) {
                    argsList.add("--weak-improvement");
                }
                if (h1bSelected) {
                    argsList.add("--strong-improvement");
                }
                if (h2aSelected) {
                    argsList.add("--filter-seed-lots-weak");
                }
                if (h2bSelected) {
                    argsList.add("--filter-seed-lots-strong");
                }
                if (h3Selected) {
                    argsList.add("--optimal-subscheme");
                }
                if (h3s1Selected) {
                    argsList.add("--optimal-subscheme-seeded-1");
                }
                if (h4Selected) {
                    argsList.add("--optimal-seedlot");
                }
                if (h5Selected) {
                    argsList.add("--consistent-heuristic-seedlot-construction");
                    // Add max crossovers parameter if specified
                    if (!maxCrossovers.isEmpty()) {
                        argsList.add("--max-crossovers");
                        argsList.add(maxCrossovers);
                    }
                }
                if (h6Selected) {
                    argsList.add("--heuristic-popsize-bound");
                }
            }
            
            // Add MCTS option if selected
            if (useMCTS) {
                argsList.add("--mcts");
            }
            
            // Handle output directory and file correctly
            // Check if output directory is specified
            String outputDir = outputDirField.getText();
            if (!outputDir.isEmpty()) {
                argsList.add("-od");
                argsList.add(outputDir);
            }
            
            // If the output file path is relative and output directory is specified,
            // combine them to create the full path for validation purposes
            File outputFileObj = new File(outputFile);
            final String fullOutputPath = outputFile; // Make it effectively final
            if (!outputFileObj.isAbsolute() && !outputDir.isEmpty()) {
                // Combine output directory and relative file path
                final String combinedPath = new File(outputDir, outputFile).getPath();
                // We need to use this path later, but we can't modify fullOutputPath since it's final
                // We'll pass the correct path to openResultPDF based on whether we have an output directory
            }
            
            argsList.add(inputFile);
            argsList.add(outputFile);
            
            // Convert to array
            String[] args = argsList.toArray(new String[0]);
            
            // Log the arguments for debugging
            outputArea.appendText("Arguments: ");
            for (String arg : args) {
                outputArea.appendText(arg + " ");
            }
            outputArea.appendText("\n");
            
            // Run Gene Stacker in a separate thread to prevent UI freezing
            Thread geneStackerThread = new Thread(() -> {
                try {
                    // Create a custom PrintStream to capture output in real-time
                    java.io.PrintStream customOut = new java.io.PrintStream(new java.io.OutputStream() {
                        @Override
                        public void write(int b) {
                            // This method is not efficient for single bytes, but we override the string write method
                        }
                        
                        @Override
                        public void write(byte[] buf, int off, int len) {
                            String message = new String(buf, off, len);
                            javafx.application.Platform.runLater(() -> {
                                outputArea.appendText(message);
                                // Auto-scroll to bottom
                                outputArea.positionCaret(outputArea.getText().length());
                                outputArea.selectPositionCaret(outputArea.getText().length());
                            });
                        }
                    });
                    
                    // Save original streams
                    java.io.PrintStream oldOut = System.out;
                    java.io.PrintStream oldErr = System.err;
                    
                    // Redirect both stdout and stderr to our custom stream
                    System.setOut(customOut);
                    System.setErr(customOut);
                    
                    // Record start time
                    long startTime = System.currentTimeMillis();
                    javafx.application.Platform.runLater(() -> {
                        outputArea.appendText("Execution started at: " + new java.util.Date(startTime) + "\n");
                    });
                    
                    // Run Gene Stacker
                    GeneStackerGUI.runGeneStacker(args);
                    
                    // Record end time
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    // Restore original streams
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                    
                    // Update UI with completion message
                    javafx.application.Platform.runLater(() -> {
                        outputArea.appendText("\n===============================\n");
                        outputArea.appendText("Gene Stacker execution completed successfully!\n");
                        outputArea.appendText("Execution time: " + String.format("%.2f", duration / 1000.0) + " seconds\n");
                        outputArea.appendText("===============================\n");
                        
                        // Show completion notification
                        showCompletionNotification(duration);
                        
                        // Try to open the result PDF file
                        // Use the correct path for opening the result
                        String outputPath = outputFile;
                        if (!outputDir.isEmpty()) {
                            // Combine output directory and relative file path
                            outputPath = new File(outputDir, outputFile).getPath();
                        }
                        openResultPDF(outputPath);
                    });
                } catch (Exception e) {
                    // Restore original streams in case of error
                    System.setOut(System.out);
                    System.setErr(System.err);
                    
                    javafx.application.Platform.runLater(() -> {
                        outputArea.appendText("\n===============================\n");
                        outputArea.appendText("Error running Gene Stacker: " + e.getMessage() + "\n");
                        outputArea.appendText("===============================\n");
                        e.printStackTrace();
                        
                        // Show error notification
                        showErrorNotification(e.getMessage());
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
    
    /**
     * Shows a completion notification when Gene Stacker finishes execution
     */
    private void showCompletionNotification(long duration) {
        try {
            // Try to show system notification
            System.out.println("Gene Stacker execution completed successfully!");
            
            // Show a simple dialog notification
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Execution Completed");
                alert.setHeaderText("运行成功");
                alert.setContentText("运行耗时 " + String.format("%.2f", duration / 1000.0) + "秒");
                alert.showAndWait();
            });
        } catch (Exception e) {
            // Silently ignore notification errors
            e.printStackTrace();
        }
    }
    
    /**
     * Shows an error notification when Gene Stacker encounters an error
     */
    private void showErrorNotification(String errorMessage) {
        try {
            // Try to show system notification
            System.err.println("Gene Stacker encountered an error: " + errorMessage);
            
            // Show a simple dialog notification
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Execution Error");
                alert.setHeaderText("Gene Stacker encountered an error");
                alert.setContentText("Error: " + errorMessage);
                alert.showAndWait();
            });
        } catch (Exception e) {
            // Silently ignore notification errors
            e.printStackTrace();
        }
    }
}