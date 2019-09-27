package com.tapadoo.slacknotifier

import jetbrains.buildServer.serverSide.settings.ProjectSettings
import org.jdom.Attribute
import org.jdom.DataConversionException
import org.jdom.Element

/**
 * @author Jason Connery
 * @since  02/03/2014
 */
class SlackProjectSettings(private val projectId: String) : ProjectSettings {
  var channel: String? = null
  var logoUrl: String? = null
  var isEnabled = true
    private set

  override fun dispose() {
  }

  override fun readFrom(element: Element) {
    val channelElement = element.getChild(ELEMENT_CHANNEL)
    val logoElement = element.getChild(ELEMENT_LOGO_URL)
    val enabledAttr = element.getAttribute(ATTR_ENABLED)

    isEnabled = try {
      enabledAttr?.booleanValue ?: true
    } catch (e: DataConversionException) {
      true
    }

    if (channelElement != null) channel = channelElement.text
    if (logoElement != null) logoUrl = logoElement.text
  }

  override fun writeTo(element: Element) {
    val channelElement = Element(ELEMENT_CHANNEL)
    channelElement.text = channel

    val logoUrlElement = Element(ELEMENT_LOGO_URL)
    logoUrlElement.text = logoUrl

    val enabledAttr = Attribute(ATTR_ENABLED, java.lang.Boolean.toString(isEnabled))
    element.setAttribute(enabledAttr)

    element.addContent(channelElement)
    element.addContent(logoUrlElement)
  }

  companion object {
    const val ELEMENT_LOGO_URL = "logoUrl"
    const val ATTR_ENABLED = "enabled"
    const val ELEMENT_CHANNEL = "channel"
  }
}
