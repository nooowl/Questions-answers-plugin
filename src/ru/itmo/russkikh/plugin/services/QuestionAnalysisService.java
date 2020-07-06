package ru.itmo.russkikh.plugin.services;

import ru.itmo.russkikh.plugin.model.Question;

import java.util.Arrays;
import java.util.stream.Stream;

public class QuestionAnalysisService {
    private final static String ALL = "-all-";

    private static QuestionAnalysisService instance;

    private Question[] loadedQuestion;
    private final QuestionLoader questionLoader;

    private QuestionAnalysisService() {
        questionLoader = new QuestionLoader(PluginService.getInstance().project);
    }

    public static QuestionAnalysisService getInstance() {
        if (instance == null) {
            instance = new QuestionAnalysisService();
        }
        return instance;
    }

    public void reloadQuestions() {
        loadedQuestion = questionLoader.getAllQuestions();
    }

    public String[] getPersons() {
        return Stream.concat(Stream.of(ALL),
                Arrays.stream(loadedQuestion)
                        .map(Question::getName).distinct())
                .toArray(String[]::new);
    }

    public Question[] getFilteredQuestions(String person) {
        if (ALL.equals(person)) {
            return Arrays.stream(loadedQuestion).filter(q -> !q.isAnswered())
                    .toArray(Question[]::new);
        }
        return Arrays.stream(loadedQuestion)
                .filter(q -> q.getName() != null && q.getName().equals(person))
                .filter(q -> !q.isAnswered())
                .toArray(Question[]::new);
    }
}
