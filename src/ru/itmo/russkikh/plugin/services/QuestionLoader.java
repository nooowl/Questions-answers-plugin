package ru.itmo.russkikh.plugin.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import ru.itmo.russkikh.plugin.model.Question;
import ru.itmo.russkikh.plugin.parser.QuestionParser;

import java.util.ArrayList;
import java.util.List;

public class QuestionLoader {
    private final static String JAVA_EXTENSION = "java";

    private final Project project;

    public QuestionLoader(Project project) {
        this.project = project;
    }

    public Question[] getAllQuestions() {
        List<PsiFile> files = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(project);
        ProjectFileIndex.getInstance(project).iterateContent(virtualFile -> {
                    PsiFile psiFile = psiManager.findFile(virtualFile);
                    if (psiFile != null)
                        files.add(psiFile);
                    return true;
                }, virtualFile -> virtualFile.isValid() && JAVA_EXTENSION.equals(virtualFile.getExtension())
        );

        QuestionParser parser = new QuestionParser(getAllComments(files));
        return parser.parseQuestionsList().toArray(new Question[0]);
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
        if (isComment(element)) {
            comments.add((PsiCommentImpl) element);
        }

        for (PsiElement psiElement : element.getChildren()) {
            if (psiElement != null) {
                dfs(psiElement, comments);
            }
        }
    }

    private boolean isComment(PsiElement element) {
        return element.getClass() == PsiCommentImpl.class;
    }
}
