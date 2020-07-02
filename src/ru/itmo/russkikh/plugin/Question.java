package ru.itmo.russkikh.plugin;

import com.intellij.psi.impl.source.tree.PsiCommentImpl;

import java.util.List;

public class Question {
    private final String name;
    private final String text;
    private final PsiCommentImpl owner;
    private final String answer;

    public Question(String name, String text, PsiCommentImpl owner, String answers) {
        this.name = name;
        this.text = text;
        this.owner = owner;
        this.answer = answers;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public PsiCommentImpl getOwner() {
        return owner;
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return getText();
    }

    public boolean isAnswered() {
        return answer != null;
    }
}
