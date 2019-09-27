package com.tapadoo.slacknotifier

import jetbrains.buildServer.serverSide.settings.ProjectSettings
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager

/**
 * @author Jason Connery
 * @since  02/03/2014
 */
class SlackProjectSettingsFactory(projectSettingsManager: ProjectSettingsManager) : ProjectSettingsFactory {

  init {
    projectSettingsManager.registerSettingsFactory(SETTINGS_KEY, this)
  }

  override fun createProjectSettings(projectId: String): ProjectSettings = SlackProjectSettings(projectId)

  companion object {
    const val SETTINGS_KEY = "slackSettings"
  }
}
