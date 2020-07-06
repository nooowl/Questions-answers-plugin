package ru.itmo.russkikh.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import ru.itmo.russkikh.plugin.services.PluginService;
import ru.itmo.russkikh.plugin.ui.ToolWindowCreator;

@SuppressWarnings("deprecation")
public class Controller implements ProjectComponent {
    @SuppressWarnings("FieldCanBeLocal")
    private final Project project;

    public Controller(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        PluginService.getInstance().project = project;
        new ToolWindowCreator(project).registerToolWindow();
    }
}
