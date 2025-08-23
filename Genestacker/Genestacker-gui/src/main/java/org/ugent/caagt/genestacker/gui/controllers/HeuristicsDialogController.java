package org.ugent.caagt.genestacker.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HeuristicsDialogController {
    
    // 预设模式单选按钮
    @FXML
    private RadioButton balancedModeRadio;
    @FXML
    private RadioButton qualityModeRadio;
    @FXML
    private RadioButton speedModeRadio;
    @FXML
    private RadioButton extremeSpeedModeRadio;
    
    // 算法选择单选按钮
    @FXML
    private RadioButton branchAndBoundRadio;
    @FXML
    private RadioButton mctsRadio;
    
    // 启发式选项复选框
    @FXML
    private CheckBox h0Checkbox;
    @FXML
    private CheckBox h1aCheckbox;
    @FXML
    private CheckBox h1bCheckbox;
    @FXML
    private CheckBox h2aCheckbox;
    @FXML
    private CheckBox h2bCheckbox;
    @FXML
    private CheckBox h3Checkbox;
    @FXML
    private CheckBox h3s1Checkbox;
    @FXML
    private CheckBox h4Checkbox;
    @FXML
    private CheckBox h5Checkbox;
    @FXML
    private CheckBox h6Checkbox;
    
    @FXML
    private HBox maxCrossoversContainer;
    @FXML
    private TextField maxCrossoversField;
    
    // 按钮
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button resetButton;
    
    // 对话框结果
    private boolean confirmed = false;
    
    @FXML
    public void initialize() {
        // 设置预设模式单选按钮组
        ToggleGroup presetModeGroup = new ToggleGroup();
        balancedModeRadio.setToggleGroup(presetModeGroup);
        qualityModeRadio.setToggleGroup(presetModeGroup);
        speedModeRadio.setToggleGroup(presetModeGroup);
        extremeSpeedModeRadio.setToggleGroup(presetModeGroup);
        
        // 设置算法选择单选按钮组
        ToggleGroup algorithmGroup = new ToggleGroup();
        branchAndBoundRadio.setToggleGroup(algorithmGroup);
        mctsRadio.setToggleGroup(algorithmGroup);
        
        // 设置默认选择
        balancedModeRadio.setSelected(true);
        branchAndBoundRadio.setSelected(true);
        
        // 设置事件处理器
        setupEventHandlers();
        
        // 初始化最大交叉数容器可见性
        maxCrossoversContainer.setVisible(h5Checkbox.isSelected());
    }
    
    private void setupEventHandlers() {
        // 设置预设模式事件处理器
        balancedModeRadio.setOnAction(event -> applyPresetMode("balanced"));
        qualityModeRadio.setOnAction(event -> applyPresetMode("quality"));
        speedModeRadio.setOnAction(event -> applyPresetMode("speed"));
        extremeSpeedModeRadio.setOnAction(event -> applyPresetMode("extremeSpeed"));
        
        // 设置算法选择事件处理器
        mctsRadio.setOnAction(event -> {
            // 禁用所有启发式选项
            setHeuristicsEnabled(false);
        });
        
        branchAndBoundRadio.setOnAction(event -> {
            // 启用所有启发式选项
            setHeuristicsEnabled(true);
        });
        
        // 设置启发式选项互斥逻辑
        h1bCheckbox.setOnAction(event -> {
            if (h1bCheckbox.isSelected()) {
                h1aCheckbox.setSelected(false);
            }
        });
        
        h1aCheckbox.setOnAction(event -> {
            if (h1aCheckbox.isSelected() && h1bCheckbox.isSelected()) {
                h1bCheckbox.setSelected(false);
            }
        });
        
        h2bCheckbox.setOnAction(event -> {
            if (h2bCheckbox.isSelected()) {
                h2aCheckbox.setSelected(false);
            }
        });
        
        h2aCheckbox.setOnAction(event -> {
            if (h2aCheckbox.isSelected() && h2bCheckbox.isSelected()) {
                h2bCheckbox.setSelected(false);
            }
        });
        
        h5Checkbox.setOnAction(event -> {
            maxCrossoversContainer.setVisible(h5Checkbox.isSelected());
        });
        
        // 设置按钮事件处理器
        okButton.setOnAction(this::handleOk);
        cancelButton.setOnAction(this::handleCancel);
        resetButton.setOnAction(this::handleReset);
    }
    
    private void applyPresetMode(String mode) {
        // 重置所有启发式复选框
        h0Checkbox.setSelected(false);
        h1aCheckbox.setSelected(false);
        h1bCheckbox.setSelected(false);
        h2aCheckbox.setSelected(false);
        h2bCheckbox.setSelected(false);
        h3Checkbox.setSelected(false);
        h3s1Checkbox.setSelected(false);
        h4Checkbox.setSelected(false);
        h5Checkbox.setSelected(false);
        h6Checkbox.setSelected(false);
        
        switch (mode) {
            case "balanced":
                // 默认模式: h0, h1a, h2a, h3s1, h4, h5, h6
                h0Checkbox.setSelected(true);
                h1aCheckbox.setSelected(true);
                h2aCheckbox.setSelected(true);
                h3s1Checkbox.setSelected(true);
                h4Checkbox.setSelected(true);
                h5Checkbox.setSelected(true);
                h6Checkbox.setSelected(true);
                break;
            case "quality":
                // 质量模式 (更好): h0, h1a, h2a, h3s1
                h0Checkbox.setSelected(true);
                h1aCheckbox.setSelected(true);
                h2aCheckbox.setSelected(true);
                h3s1Checkbox.setSelected(true);
                break;
            case "speed":
                // 速度模式 (更快): h0, h1b, h2b, h3s2, h4, h5c, h6
                h0Checkbox.setSelected(true);
                h1bCheckbox.setSelected(true);
                h2bCheckbox.setSelected(true);
                h3s1Checkbox.setSelected(true); // 使用 h3s1 作为 h3s2 的占位符
                h4Checkbox.setSelected(true);
                h5Checkbox.setSelected(true); // 使用 h5 作为 h5c 的占位符
                h6Checkbox.setSelected(true);
                break;
            case "extremeSpeed":
                // 极限速度模式 (最快): h0, h1b, h2b, h3, h4, h5c, h6
                h0Checkbox.setSelected(true);
                h1bCheckbox.setSelected(true);
                h2bCheckbox.setSelected(true);
                h3Checkbox.setSelected(true);
                h4Checkbox.setSelected(true);
                h5Checkbox.setSelected(true); // 使用 h5 作为 h5c 的占位符
                h6Checkbox.setSelected(true);
                break;
        }
        
        // 更新最大交叉数容器可见性
        maxCrossoversContainer.setVisible(h5Checkbox.isSelected());
    }
    
    private void handleOk(ActionEvent event) {
        confirmed = true;
        closeDialog();
    }
    
    private void handleCancel(ActionEvent event) {
        confirmed = false;
        closeDialog();
    }
    
    private void handleReset(ActionEvent event) {
        // 重置为默认设置（平衡模式）
        balancedModeRadio.setSelected(true);
        branchAndBoundRadio.setSelected(true);
        applyPresetMode("balanced");
        setHeuristicsEnabled(true);
    }
    
    private void setHeuristicsEnabled(boolean enabled) {
        h0Checkbox.setDisable(!enabled);
        h1aCheckbox.setDisable(!enabled);
        h1bCheckbox.setDisable(!enabled);
        h2aCheckbox.setDisable(!enabled);
        h2bCheckbox.setDisable(!enabled);
        h3Checkbox.setDisable(!enabled);
        h3s1Checkbox.setDisable(!enabled);
        h4Checkbox.setDisable(!enabled);
        h5Checkbox.setDisable(!enabled);
        h6Checkbox.setDisable(!enabled);
        maxCrossoversContainer.setDisable(!enabled);
    }
    
    private void closeDialog() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
    
    // Getter方法，用于主控制器获取设置
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public boolean isBalancedModeSelected() {
        return balancedModeRadio.isSelected();
    }
    
    public boolean isQualityModeSelected() {
        return qualityModeRadio.isSelected();
    }
    
    public boolean isSpeedModeSelected() {
        return speedModeRadio.isSelected();
    }
    
    public boolean isExtremeSpeedModeSelected() {
        return extremeSpeedModeRadio.isSelected();
    }
    
    // 启发式选项Getter方法
    public boolean isH0Selected() {
        return h0Checkbox.isSelected();
    }
    
    public boolean isH1aSelected() {
        return h1aCheckbox.isSelected();
    }
    
    public boolean isH1bSelected() {
        return h1bCheckbox.isSelected();
    }
    
    public boolean isH2aSelected() {
        return h2aCheckbox.isSelected();
    }
    
    public boolean isH2bSelected() {
        return h2bCheckbox.isSelected();
    }
    
    public boolean isH3Selected() {
        return h3Checkbox.isSelected();
    }
    
    public boolean isH3s1Selected() {
        return h3s1Checkbox.isSelected();
    }
    
    public boolean isH4Selected() {
        return h4Checkbox.isSelected();
    }
    
    public boolean isH5Selected() {
        return h5Checkbox.isSelected();
    }
    
    public boolean isH6Selected() {
        return h6Checkbox.isSelected();
    }
    
    public String getMaxCrossovers() {
        return maxCrossoversField.getText();
    }
    
    // Setter方法，用于主控制器设置值
    public void setPresetMode(String mode) {
        switch (mode) {
            case "balanced":
                balancedModeRadio.setSelected(true);
                break;
            case "quality":
                qualityModeRadio.setSelected(true);
                break;
            case "speed":
                speedModeRadio.setSelected(true);
                break;
            case "extremeSpeed":
                extremeSpeedModeRadio.setSelected(true);
                break;
            default:
                balancedModeRadio.setSelected(true);
        }
        applyPresetMode(mode);
    }
    
    public void setHeuristicOptions(boolean h0, boolean h1a, boolean h1b, boolean h2a, boolean h2b,
                                   boolean h3, boolean h3s1, boolean h4, boolean h5, boolean h6) {
        h0Checkbox.setSelected(h0);
        h1aCheckbox.setSelected(h1a);
        h1bCheckbox.setSelected(h1b);
        h2aCheckbox.setSelected(h2a);
        h2bCheckbox.setSelected(h2b);
        h3Checkbox.setSelected(h3);
        h3s1Checkbox.setSelected(h3s1);
        h4Checkbox.setSelected(h4);
        h5Checkbox.setSelected(h5);
        h6Checkbox.setSelected(h6);
        
        // 更新最大交叉数容器可见性
        maxCrossoversContainer.setVisible(h5);
    }
    
    public void setMaxCrossovers(String maxCrossovers) {
        maxCrossoversField.setText(maxCrossovers);
    }
    
    public boolean isMCTS() {
        return mctsRadio.isSelected();
    }
    
    public boolean isBranchAndBound() {
        return branchAndBoundRadio.isSelected();
    }
    
    public void setAlgorithm(String algorithm) {
        if ("mcts".equals(algorithm)) {
            mctsRadio.setSelected(true);
            setHeuristicsEnabled(false);
        } else {
            branchAndBoundRadio.setSelected(true);
            setHeuristicsEnabled(true);
        }
    }
}