package ru.itmo.russkikh.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;

@SuppressWarnings("deprecation")
public class Controller implements ProjectComponent {
    @SuppressWarnings("FieldCanBeLocal")
    private final Project project;

    public Controller(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        new ToolWindowCreator(project).registerToolWindow();
    }
}
