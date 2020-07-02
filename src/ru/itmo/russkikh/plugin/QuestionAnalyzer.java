package ru.itmo.russkikh.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;

import java.util.ArrayList;
import java.util.List;

public class QuestionAnalyzer {
    private final Project project;

    public QuestionAnalyzer(Project project) {
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
                }, virtualFile -> virtualFile.isValid() && virtualFile.getName().endsWith(".java")
        );

        Parser parser= new Parser(getAllComments(files));
        return parser.createQuestionsList().toArray(new Question[0]);
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
        if (element.getClass() == PsiCommentImpl.class
                && ((PsiCommentImpl) element).getTokenType().getIndex() == 1919) {
            comments.add((PsiCommentImpl) element);
        }

        for (PsiElement psiElement : element.getChildren()) {
            if (psiElement != null) {
                dfs(psiElement, comments);
            }
        }
    }
}
