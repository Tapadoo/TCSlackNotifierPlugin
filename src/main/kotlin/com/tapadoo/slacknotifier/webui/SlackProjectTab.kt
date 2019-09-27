package com.tapadoo.slacknotifier.webui

import com.tapadoo.slacknotifier.SlackConfigProcessor
import com.tapadoo.slacknotifier.SlackProjectSettings
import com.tapadoo.slacknotifier.SlackProjectSettingsFactory
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.project.ProjectTab

import javax.servlet.http.HttpServletRequest

/**
 * @author Jason Connery
 * @since 04/03/2014
 */
class SlackProjectTab(
    pagePlaces: PagePlaces,
    projectManager: ProjectManager,
    pluginDescriptor: PluginDescriptor,
    private val projectSettingsManager: ProjectSettingsManager,
    private val slackConfigProcessor: SlackConfigProcessor) : ProjectTab("slackNotifierProjectTab", "Slack", pagePlaces, projectManager) {

  init {
    includeUrl = pluginDescriptor.getPluginResourcesPath("/admin/slackProjectPage.jsp")
  }

  override fun fillModel(model: MutableMap<String, Any>, httpServletRequest: HttpServletRequest, sProject: SProject, sUser: SUser?) {
    val slackProjectSettings = projectSettingsManager.getSettings(sProject.projectId, SlackProjectSettingsFactory.SETTINGS_KEY) as? SlackProjectSettings
    val channel = slackProjectSettings?.channel.takeIf { !slackProjectSettings?.channel.isNullOrEmpty() } ?: slackConfigProcessor.defaultChannel
    val enabled = slackProjectSettings?.isEnabled ?: true
    val logoUrl = slackProjectSettings?.logoUrl.takeIf { !slackProjectSettings?.channel.isNullOrEmpty() } ?: ""

    model["configDir"] = sProject.configDirectory.toString()
    model["channel"] = channel
    model["enabled"] = enabled
    model["logoUrl"] = logoUrl
  }
}
