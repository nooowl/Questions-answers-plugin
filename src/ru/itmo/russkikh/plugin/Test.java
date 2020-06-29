package ru.itmo.russkikh.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.JavaFileElementType;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.psi.impl.source.tree.java.JavaFileElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Test extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project == null) return;

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
        int a = 0;
    }

    private List<PsiCommentImpl> getAllComments(List<PsiFile> files) {
        List<PsiCommentImpl> comments = new ArrayList<>();

        for (PsiFile file : files) {
            for (PsiElement element : file.getChildren()){
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
