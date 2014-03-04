package com.tapadoo.slacknotifier;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

/**
 * Created by jasonconnery on 02/03/2014.
 */
public class SlackProjectSettingsFactory implements ProjectSettingsFactory {

    public static final String SETTINGS_KEY = "slackSettings";

    public SlackProjectSettingsFactory(ProjectSettingsManager projectSettingsManager)
    {
        projectSettingsManager.registerSettingsFactory(SETTINGS_KEY, this);
    }

    public ProjectSettings createProjectSettings(String projectId) {
        return new SlackProjectSettings(projectId);
    }
}
