package ru.itmo.russkikh.plugin;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    final List<PsiCommentImpl> comments;

    private static final Pattern QUESTION_PATTERN = Pattern.compile("^[/\\s*]*\\?(.+):(.+)$");

    private static final Pattern QUESTION_PART_PATTERN = Pattern.compile("^[\\s]*-(.+)$");

    private static final Pattern ANSWER_PATTERN = Pattern.compile("^[/\\s*]*!answer:(.+)$");

    public Parser(List<PsiCommentImpl> comments) {
        this.comments = comments;
    }

    public List<Question> createQuestionsList() {
        List<Question> questions = new ArrayList<>();
        for (PsiCommentImpl comment : comments) {
            String[] commentLines = comment.getText().split("\n");
            for (int i = 0; i < commentLines.length; i++) {
                Pair<String, String> nameAndText = parseQuestion(commentLines[i]);
                if (nameAndText != null) {
                    StringBuilder resultQuestion = new StringBuilder(nameAndText.second);
                    for (int j = i + 1; j < commentLines.length; j++) {
                        String questionPart = parseQuestionPart(commentLines[j]);
                        if (questionPart != null) {
                            resultQuestion.append(" ").append(questionPart);
                        } else {
                            i = j;
                            break;
                        }
                    }
                    for (int j = i; j < commentLines.length; j++) {
                        if(!commentLines[j].matches("\\s*")){
                            i = j;
                            break;
                        }
                    }
                    String answerStart = parseAnswer(commentLines[i]);
                    String answer = null;
                    if (answerStart != null) {
                        StringBuilder resultAnswer = new StringBuilder(answerStart);
                        for (int j = i + 1; j < commentLines.length; j++) {
                            String answerPart = parseQuestionPart(commentLines[j]);
                            if (answerPart != null) {
                                resultAnswer.append(" ").append(answerPart);
                            } else {
                                i = j;
                                break;
                            }
                        }
                        answer = resultAnswer.toString();
                    }
                    i--;
                    questions.add(new Question(nameAndText.first, resultQuestion.toString(), comment, answer));
                }
            }
        }
        return questions;
    }

    private Pair<String, String> parseQuestion(String comment) {
        MatchResult questionMatchResult = getMatchResult(QUESTION_PATTERN, comment);
        if (questionMatchResult == null) return null;
        return new Pair<>(questionMatchResult.group(1).trim(), questionMatchResult.group(2).trim());
    }

    private String parseQuestionPart(String comment) {
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

    private String parseAnswer(String comment){
        MatchResult answerMatchResult = getMatchResult(ANSWER_PATTERN, comment);
        if (answerMatchResult == null) return null;
        return answerMatchResult.group(1).trim();
    }
}
