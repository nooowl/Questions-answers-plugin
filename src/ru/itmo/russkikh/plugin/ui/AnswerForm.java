package ru.itmo.russkikh.plugin.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBUI;
import ru.itmo.russkikh.plugin.model.Question;
import ru.itmo.russkikh.plugin.services.PluginService;

import javax.swing.*;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "FieldCanBeLocal", "FieldMayBeFinal"})
public class AnswerForm {
    private JTextPane questionTextPane;
    private JBTextField answerTextField;
    private Question question;
    private JBPanel panel;
    private ToolWindowCreator owner;

    public AnswerForm(Question question, ToolWindowCreator owner) {
        this.question = question;
        this.owner = owner;
        setupUI();
    }

    public void showQuestionDialog() {
        DialogBuilder builder = new DialogBuilder();
        builder.setCenterPanel(panel);
        builder.setDimensionServiceKey("QuestionDialog");
        builder.setTitle("Answer question");
        builder.removeAllActions();
        builder.addOkAction().setText("Answer");
        builder.setOkOperation(() -> {
            WriteCommandAction.runWriteCommandAction(PluginService.getInstance().project,
                    this::addAnswer);
            owner.reloadAndUpdate();
            builder.dispose();
        });
        builder.addCancelAction();
        builder.show();
    }

    private void addAnswer() {
        List<String> commentLines = Arrays.stream(question.getOwner().getText().split("\n"))
                .collect(Collectors.toList());
        int nextLine = question.getLastLineNumber() + 1;

        String lastLine = commentLines.get(question.getLastLineNumber());
        int spaceOffset = 0;
        while (spaceOffset < lastLine.length() && lastLine.charAt(spaceOffset) == ' ') {
            spaceOffset++;
        }
        String spaces = " ".repeat(spaceOffset);

        String[] answerLines = answerTextField.getText().split("\n");
        commentLines.add(nextLine++, spaces + "!answer: " + answerLines[0]);
        for (int i = 1; i < answerLines.length; i++) {
            commentLines.add(nextLine++, spaces + "- " + answerLines[i]);
        }
        if (nextLine >= commentLines.size()) {
            commentLines.add(nextLine, "*/");
        }
        question.getOwner().updateText(String.join("\n", commentLines));
    }

    private void setupUI() {
        createMainPanel();
        createQuestionTextPane();
        createSpacer(2, 0, 1, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW);
        createSpacer(0, 1, 3, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1);
        createSpacer(6, 1, 3, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1);
        createAnswerTextField();
        createLabel("Question", 1);
        createLabel("Enter answer", 3);

    }

    private void createLabel(String question, int i) {
        final JLabel questionLabel = new JLabel();
        questionLabel.setText(question);
        panel.add(questionLabel, new GridConstraints(i, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
    }

    private void createAnswerTextField() {
        answerTextField = new JBTextField();
        panel.add(answerTextField, new GridConstraints(4, 1, 1, 3,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(150, 50), null,
                0, false));
    }

    private void createSpacer(int i, int i2, int i3, int fillVertical, int i4, int sizepolicyWantGrow) {
        panel.add(new Spacer(), new GridConstraints(i, i2, 1, i3,
                GridConstraints.ANCHOR_CENTER, fillVertical,
                i4, sizepolicyWantGrow,
                null, null, null, 0, false));
    }

    private void createQuestionTextPane() {
        questionTextPane = new JTextPane();
        questionTextPane.setText(question.getText());
        panel.add(questionTextPane, new GridConstraints(2, 1, 1, 3,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(150, 50),
                null, 0, false));
    }

    private void createMainPanel() {
        panel = new JBPanel();
        panel.setLayout(new GridLayoutManager(7, 5, JBUI.emptyInsets(), -1, -1));
        final Spacer spacer1 = new Spacer();
        panel.add(spacer1, new GridConstraints(2, 4, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                1, GridConstraints.SIZEPOLICY_WANT_GROW,
                null, null, null, 0, false));
    }
}
