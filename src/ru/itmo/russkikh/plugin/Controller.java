package ru.itmo.russkikh.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Controller implements ProjectComponent {
    private final Project project;

    public Controller(Project project) {
        this.project = project;
    }

    // TODO: implement projectClosed

    @Override
    public void projectOpened() {
        List<PsiFile> files = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        ProjectFileIndex.getInstance(project).iterateContent(virtualFile -> {
                    PsiFile psiFile = psiManager.findFile(virtualFile);
                    if (psiFile != null)
                        files.add(psiFile);
                    return true;
                }, virtualFile -> virtualFile.isValid() && virtualFile.getName().endsWith(".java")
        );

        List<PsiCommentImpl> comments = getAllComments(files);
        //noinspection UndesirableClassUsage
        JList<String> commentsList = new JList<>(comments.stream().map(LeafElement::getText).toArray(String[]::new));

        JComponent panel = new JPanel();
        panel.add(new JScrollPane(commentsList));

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        content.setCloseable(false);

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(
                "Q&A Comments", true, ToolWindowAnchor.BOTTOM);
        toolWindow.getContentManager().addContent(content);
    }

    private List<PsiCommentImpl> getAllComments(List<PsiFile> files) {
        List<PsiCommentImpl> comments = new ArrayList<>();

        for (PsiFile file : files) {
            for (PsiElement element : file.getChildren()) {
                dfs(element, comments);
            }
        }
        return comments;
    }

    private void dfs(PsiElement element, List<PsiCommentImpl> comments) {
        if (element.getClass() == PsiCommentImpl.class) {
            comments.add((PsiCommentImpl) element);
        }

        for (PsiElement psiElement : element.getChildren()) {
            if (psiElement != null) {
                dfs(psiElement, comments);
            }
        }
    }
}
