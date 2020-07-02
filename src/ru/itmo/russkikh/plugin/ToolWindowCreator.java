package ru.itmo.russkikh.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogBuilder;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class ToolWindowCreator {
    private static final String ALL = "All";

    private JBPanel panel;
    private JBList<Question> questionList;
    private ComboBox<String> persons;

    private Question[] questions;
    private final QuestionAnalyzer questionAnalyzer;
    private final String[] personsList;
    private final Project project;

    public ToolWindowCreator(Project project) {
        this.project = project;
        this.questionAnalyzer = new QuestionAnalyzer(project);
        this.personsList = new String[]{ALL, "Pes", "Mysh"};
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
        createMainPanel();
        createPersonComboBox();
        createQuestionsList();
        createLabel("Questions", panel, 1, 1);
        createLabel("Choose person", panel, 1, 2);
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
        reloadQuestionListModel();
        updateQuestionListModel();
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
        ansButton.addActionListener(e -> createQuestionDialog(questionList.getSelectedValue()));
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
            reloadQuestionListModel();
            updateQuestionListModel();
        });
        updateButton.setText("Reload");
        panel.add(updateButton, new GridConstraints(3, 2, 1, 1,
                GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(100, 40), null,
                0, false));
    }

    private void createLabel(String questions, JBPanel panel, int row, int column) {
        final JBLabel label1 = new JBLabel();
        label1.setText(questions);
        panel.add(label1, new GridConstraints(row, column, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(100, 30), null,
                0, false));
    }

    private void createPersonComboBox() {
        persons = new ComboBox<>(personsList);
        persons.addActionListener(e -> updateQuestionListModel());
        panel.add(persons, new GridConstraints(2, 2, 1, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(200, 30), new Dimension(200, 30),
                0, false));
    }

    private void reloadQuestionListModel() {
        questions = questionAnalyzer.getAllQuestions();
    }

    private void updateQuestionListModel() {
        if (ALL.equals(persons.getSelectedItem())) {
            questionList.setModel(JBList.createDefaultListModel(
                    Arrays.stream(questions).filter(q -> !q.isAnswered())
                            .toArray(Question[]::new)));
            return;
        }
        Question[] filteredQuestions = Arrays.stream(questions)
                .filter(q -> q.getName() != null && q.getName().equals(persons.getSelectedItem()))
                .filter(q -> !q.isAnswered())
                .toArray(Question[]::new);
        questionList.setModel(JBList.createDefaultListModel(filteredQuestions));
    }

    private void createQuestionDialog(Question question) {
        JBPanel dialogPanel = new JBPanel(new GridLayoutManager(1, 1,
                JBUI.emptyInsets(), -1, -1));
        createLabel(question.getText(), dialogPanel, 0, 0);
        DialogBuilder builder = new DialogBuilder();
        builder.setCenterPanel(dialogPanel);
        builder.setDimensionServiceKey("QuestionDialog");
        builder.setTitle("Answer question");
        builder.removeAllActions();
        builder.addOkAction();
        builder.addCancelAction();
        builder.show();
    }
}
