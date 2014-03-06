package com.tapadoo.slacknotifier.webui;

import com.tapadoo.slacknotifier.SlackConfigProcessor;
import com.tapadoo.slacknotifier.SlackProjectSettings;
import com.tapadoo.slacknotifier.SlackProjectSettingsFactory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.project.ProjectTab;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by jasonconnery on 04/03/2014.
 */
public class SlackProjectTab extends ProjectTab {

    private final ProjectSettingsManager projectSettingsManager;
    private final SlackConfigProcessor slackConfigProcessor;

    public SlackProjectTab(PagePlaces pagePlaces , ProjectManager projectManager , PluginDescriptor pluginDescriptor , ProjectSettingsManager projectSettingsManager , SlackConfigProcessor slackConfigProcessor)
    {
        super("slackNotifierProjectTab","Slack" , pagePlaces , projectManager);
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath("/admin/slackProjectPage.jsp"));

        this.slackConfigProcessor = slackConfigProcessor;
        this.projectSettingsManager = projectSettingsManager ;
    }

    @Override
    protected void fillModel(Map<String, Object> model, HttpServletRequest httpServletRequest, SProject sProject, SUser sUser) {

        SlackProjectSettings slackProjectSettings = (SlackProjectSettings) projectSettingsManager.getSettings(sProject.getProjectId(), SlackProjectSettingsFactory.SETTINGS_KEY);

        String channel = slackConfigProcessor.getDefaultChannel() ;
        boolean enabled = true ;

        if( slackProjectSettings != null && slackProjectSettings.getChannel() != null && slackProjectSettings.getChannel().length() > 0 )
        {
            channel = slackProjectSettings.getChannel() ;
        }

        if( slackProjectSettings != null )
        {
            enabled = slackProjectSettings.isEnabled();
        }

        model.put("configDir" , sProject.getConfigDirectory().toString());
        model.put("channel" , channel );
        model.put("enabled" , enabled );

    }
}
