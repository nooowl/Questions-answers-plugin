package ru.itmo.russkikh.plugin.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import ru.itmo.russkikh.plugin.model.Question;
import ru.itmo.russkikh.plugin.services.QuestionAnalysisService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("rawtypes")
public class ToolWindowCreator {
    private JBPanel panel;
    private JBList<Question> questionList;
    private ComboBox<String> personComboBox;

    private final QuestionAnalysisService questionAnalysisService;
    private final Project project;

    public ToolWindowCreator(Project project) {
        this.project = project;
        this.questionAnalysisService = QuestionAnalysisService.getInstance();
        setupUI();
    }

    public void registerToolWindow() {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(getRootComponent(), "", false);
        content.setCloseable(false);

        @SuppressWarnings("deprecation")
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(
                "Q&A Comments", true, ToolWindowAnchor.BOTTOM);
        toolWindow.getContentManager().addContent(content);
    }

    public JBPanel getRootComponent() {
        return panel;
    }

    private void setupUI() {
        questionAnalysisService.reloadQuestions();
        createMainPanel();
        createPersonComboBox();
        createQuestionsList();
        createLabel("Questions", panel, 1);
        createLabel("Choose person", panel, 2);
        createReloadButton();
        createAnswerButton();
    }

    private void createMainPanel() {
        panel = new JBPanel();
        panel.setLayout(new GridLayoutManager(5, 5,
                JBUI.emptyInsets(), -1, -1));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.setSize(2000, 500);
    }

    private void createQuestionsList() {
        questionList = new JBList<>() {
            @Override
            public String getToolTipText(MouseEvent event) {
                int row = locationToIndex(event.getPoint());
                Question o = getModel().getElementAt(row);
                return "?" + o.getName() + ": " + o.getText() + " " + (o.isAnswered() ? "✓" : "✗") +
                        " " + o.getOwner().getContainingFile().getVirtualFile().getPath();
            }
        };
        reloadAndUpdate();
        questionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    @SuppressWarnings("unchecked")
                    JBList<Question> list = (JBList<Question>) e.getSource();
                    int index = list.locationToIndex(e.getPoint());
                    PsiCommentImpl psiComment = list.getModel().getElementAt(index).getOwner();
                    if (psiComment.isValid()) {
                        psiComment.navigate(true);
                    }
                }
            }
        });
        final JBScrollPane scrollPane = new JBScrollPane(questionList);
        scrollPane.setEnabled(true);
        panel.add(scrollPane, new GridConstraints(2, 1, 3, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                null, new Dimension(800, 300), new Dimension(800, 300),
                0, false));
    }

    private void createAnswerButton() {
        final JButton ansButton = new JButton();
        ansButton.addActionListener(e ->
                new AnswerForm(questionList.getSelectedValue(), this).showQuestionDialog());
        ansButton.setText("Answer");
        panel.add(ansButton, new GridConstraints(4, 2, 1, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(100, 40), null,
                0, false));
    }

    private void createReloadButton() {
        final JButton updateButton = new JButton();
        updateButton.addActionListener(e -> {
            reloadAndUpdate();
        });
        updateButton.setText("Reload");
        panel.add(updateButton, new GridConstraints(3, 2, 1, 1,
                GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(100, 40), null,
                0, false));
    }

    private void createLabel(String questions, JBPanel panel, int column) {
        final JBLabel label1 = new JBLabel();
        label1.setText(questions);
        panel.add(label1, new GridConstraints(1, column, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(100, 30), null,
                0, false));
    }

    private void createPersonComboBox() {
        personComboBox = new ComboBox<>(questionAnalysisService.getPersons());
        personComboBox.addActionListener(e -> updateQuestionListModel());
        panel.add(personComboBox, new GridConstraints(2, 2, 1, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(200, 30), new Dimension(200, 30),
                0, false));
    }

    private void updateQuestionListModel() {
        questionList.setModel(JBList.createDefaultListModel(
                questionAnalysisService.getFilteredQuestions((String) personComboBox.getSelectedItem())));
    }


    public void reloadAndUpdate() {
        questionAnalysisService.reloadQuestions();
        updateQuestionListModel();
        personComboBox.setModel(new DefaultComboBoxModel<>(questionAnalysisService.getPersons()));
    }
}
