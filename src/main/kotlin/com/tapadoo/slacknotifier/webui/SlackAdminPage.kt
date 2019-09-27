package com.tapadoo.slacknotifier.webui

import com.tapadoo.slacknotifier.SlackConfigProcessor
import jetbrains.buildServer.controllers.admin.AdminPage
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.web.openapi.Groupable
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.PositionConstraint

import javax.servlet.http.HttpServletRequest

/**
 * @author Jason Connery
 * @since  02/03/2014
 */
class SlackAdminPage(
    pagePlaces: PagePlaces,
    descriptor: PluginDescriptor,
    private val configProcesser: SlackConfigProcessor) : AdminPage(pagePlaces) {

  init {
    pluginName = "slackNotifier"
    includeUrl = descriptor.getPluginResourcesPath("/admin/slackAdminPage.jsp")
    tabTitle = "Slack Notifier"
    setPosition(PositionConstraint.after("clouds", "email", "jabber"))
    register()
  }

  override fun isAvailable(request: HttpServletRequest): Boolean =
      super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS)

  override fun getGroup(): String = Groupable.INTEGRATIONS_GROUP

  override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest) {
    super.fillModel(model, request)

    model["defaultChannel"] = configProcesser.defaultChannel
    model["logoUrl"] = configProcesser.logoUrl
    model["postUrl"] = configProcesser.postUrl
  }
}
