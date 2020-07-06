package ru.itmo.russkikh.plugin.parser;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import ru.itmo.russkikh.plugin.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionParser {
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^[/\\s*]*\\?(.+):(.+)$");
    private static final Pattern QUESTION_PART_PATTERN = Pattern.compile("^[\\s]*-(.+)$");
    private static final Pattern ANSWER_PATTERN = Pattern.compile("^[/\\s*]*!answer:(.+)$");

    private final List<PsiCommentImpl> comments;

    public QuestionParser(List<PsiCommentImpl> comments) {
        this.comments = comments;
    }

    public List<Question> parseQuestionsList() {
        List<Question> questions = new ArrayList<>();
        for (PsiCommentImpl comment : comments) {
            String[] commentLines = comment.getText().split("\n");
            int i = 0;
            while (i < commentLines.length) {
                Pair<String, String> nameAndText = parseQuestion(commentLines[i]);
                if (nameAndText != null) {
                    StringBuilder resultQuestion = new StringBuilder(nameAndText.second);
                    i = parseQuestionOrAnswerParts(commentLines, i, resultQuestion);
                    int lastLine = i - 1;
                    for (int j = i; j < commentLines.length; j++) {
                        if (!commentLines[j].matches("\\s*")) {
                            i = j;
                            break;
                        }
                    }
                    String answerStart = parseAnswer(commentLines[i]);
                    String answer = null;
                    if (answerStart != null) {
                        StringBuilder resultAnswer = new StringBuilder(answerStart);
                        i = parseQuestionOrAnswerParts(commentLines, i, resultAnswer);
                        answer = resultAnswer.toString();
                    }
                    questions.add(new Question(nameAndText.first, resultQuestion.toString(),
                            comment, answer, lastLine));
                } else {
                    i++;
                }
            }
        }
        return questions;
    }

    private int parseQuestionOrAnswerParts(String[] commentLines, int i, StringBuilder result) {
        i++;
        for (int j = i; j < commentLines.length; j++) {
            String answerPart = parseQuestionOrAnswerPart(commentLines[j]);
            if (answerPart != null) {
                result.append(" ").append("\n").append(answerPart);
            } else {
                i = j;
                break;
            }
        }
        return i;
    }

    private Pair<String, String> parseQuestion(String comment) {
        MatchResult questionMatchResult = getMatchResult(QUESTION_PATTERN, comment);
        if (questionMatchResult == null) return null;
        return new Pair<>(questionMatchResult.group(1).trim(), questionMatchResult.group(2).trim());
    }

    private String parseQuestionOrAnswerPart(String comment) {
        MatchResult questionPartMatchResult = getMatchResult(QUESTION_PART_PATTERN, comment);
        if (questionPartMatchResult == null) return null;
        return questionPartMatchResult.group(1).trim();
    }

    private MatchResult getMatchResult(Pattern p, String s) {
        Matcher matcher = p.matcher(s);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.toMatchResult();
    }

    private String parseAnswer(String comment) {
        MatchResult answerMatchResult = getMatchResult(ANSWER_PATTERN, comment);
        if (answerMatchResult == null) return null;
        return answerMatchResult.group(1).trim();
    }
}
