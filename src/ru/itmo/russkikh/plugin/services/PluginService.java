package ru.itmo.russkikh.plugin.services;

import com.intellij.openapi.project.Project;

public class PluginService {
    private static final PluginService instance = new PluginService();

    private PluginService() {}

    public Project project;

    public static PluginService getInstance() {
        return instance;
    }
}
