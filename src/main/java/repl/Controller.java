package repl;

import repl.util.TerminalComponent;
import repl.util.LanguageAdapter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.time.Year;

public class Controller {

    @FXML
    private Button interpreterButton;

    @FXML
    private Button codeButton;

    @FXML
    private Button runCodeButton;

    @FXML
    private SplitPane mainSplit;

    @FXML
    private TextArea codeBox;

    @FXML
    private TextArea interpreterBox;

    @FXML
    private TextArea executionBox;

    @FXML
    private ComboBox<String> languageSelect;

    private LanguageAdapter[] interpreterAdapters;

    private LanguageAdapter[] executionAdapters;

    private TerminalComponent interpreterComponent, executionComponent;

    final String[] languages = new String[] { "js" };//, "python" }; // ,

    private int currentLangIndex = 0;

    enum GUI_STATE {
        INTERPRETER, CODE_EDITOR,
    }

    private GUI_STATE state = GUI_STATE.INTERPRETER;

    public void init() {
        interpreterComponent = new TerminalComponent(interpreterBox);
        executionComponent = new TerminalComponent(executionBox);

        interpreterAdapters = new LanguageAdapter[languages.length];
        executionAdapters = new LanguageAdapter[languages.length];

        for (int i = 0; i < languages.length; i++) {
            interpreterAdapters[i] = new LanguageAdapter(languages[i], interpreterComponent);
            executionAdapters[i] = new LanguageAdapter(languages[i], executionComponent);
        }

        languageSelect.setItems(FXCollections.observableArrayList(languages));
        languageSelect.setValue(languages[currentLangIndex]);

        languageSelect.setOnAction(e -> {
            currentLangIndex = languageSelect.getSelectionModel().getSelectedIndex();
            interpreterAdapters[currentLangIndex].clear();
            executionAdapters[currentLangIndex].clear();

            interpreterAdapters[currentLangIndex].showPrompt();
        });

        interpreterComponent.write("GraalVM REPL Prompt");
        interpreterComponent.write(
                "Copyright (c) 2013-" + String.valueOf(Year.now().getValue()) + ", Oracle and/or its affiliates");
        interpreterComponent.write("");
        interpreterAdapters[currentLangIndex].showPrompt();
        interpreterBox.requestFocus();
    }

    public void doInterpreterEval() throws IOException {
        final String code = interpreterComponent.getCurrentCode();
        interpreterComponent.commitCurrent(interpreterAdapters[currentLangIndex].getBlocked());
        if (!interpreterAdapters[currentLangIndex].getBlocked())
            interpreterAdapters[currentLangIndex].eval(code, true);
        interpreterComponent.updateStreams();
    }

    public void doExecutionEval() throws IOException {
        final String code = codeBox.getText();
        executionAdapters[currentLangIndex].eval(code, true);
        executionComponent.updateStreams();
    }

    public void initialize() {
        init();

        mainSplit.setDividerPositions(0);

        interpreterBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                try {
                    doInterpreterEval();
                } catch (final Exception e) {
                    System.err.println(e);
                }
            else if (event.getCode() == KeyCode.ESCAPE) //Keyboard turned off.
                interpreterBox.getParent().requestFocus();
        });

        executionBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) //Keyboard turned off.
                executionBox.getParent().requestFocus();
        });

        codeBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) //Keyboard turned off.
                codeBox.getParent().requestFocus();
        });

        mainSplit.getDividers().get(0).positionProperty().addListener(e -> {
            if (state == GUI_STATE.INTERPRETER)
                mainSplit.setDividerPositions(0);
        });

        interpreterButton.setOnAction(event -> {
            state = GUI_STATE.INTERPRETER;
            mainSplit.setDividerPositions(0);
            interpreterBox.setVisible(true);
            executionBox.setVisible(false);
        });

        codeButton.setOnAction(event -> {
            state = GUI_STATE.CODE_EDITOR;
            mainSplit.setDividerPositions(0.5);
            interpreterBox.setVisible(false);
            executionBox.setVisible(true);
        });

        runCodeButton.setOnAction(event -> {
            try {
                doExecutionEval();
            } catch (IOException e) {
            }
        });
    }

}
