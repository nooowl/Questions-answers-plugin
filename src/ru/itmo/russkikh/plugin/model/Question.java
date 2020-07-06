package ru.itmo.russkikh.plugin.model;

import com.intellij.psi.impl.source.tree.PsiCommentImpl;

public class Question {
    private final String name;
    private final String text;
    private final PsiCommentImpl owner;
    private final String answer;
    private int lastLineNumber;

    public Question(String name, String text, PsiCommentImpl owner, String answers, int lastLineNumber) {
        this.name = name;
        this.text = text;
        this.owner = owner;
        this.answer = answers;
        this.lastLineNumber = lastLineNumber;
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

    public int getLastLineNumber() {
        return lastLineNumber;
    }
}
