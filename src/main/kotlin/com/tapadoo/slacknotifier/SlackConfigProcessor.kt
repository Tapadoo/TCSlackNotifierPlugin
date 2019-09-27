package com.tapadoo.slacknotifier

import jetbrains.buildServer.serverSide.MainConfigProcessor
import org.jdom.DataConversionException
import org.jdom.Element

/**
 * @author Jason Connery
 * @since 04/03/2014
 */
class SlackConfigProcessor : MainConfigProcessor {
  var defaultChannel = DEFAULT_CHANNEL
  var postUrl: String = ""
  var logoUrl: String = ""

  var postSuccessful = true
    private set
  var postStarted = false
    private set
  var postFailed = false
    private set

  // Leave empty. Required for 'resources/META-INF/build-server-plugin-slackNotifier.xml'
  fun init() {}

  override fun readFrom(element: Element) {
    val mainConfigElement = element.getChild(PREF_CHILD_ELEMENT)
    if (mainConfigElement == null) {
      postUrl = "http://localhost/"
      return
    }

    defaultChannel = mainConfigElement.getChildText(PREF_KEY_SLACK_DEF_CHANNEL) ?: DEFAULT_CHANNEL
    postUrl = mainConfigElement.getChildText(PREF_KEY_SLACK_POST_URL) ?: ""
    logoUrl = mainConfigElement.getChildText(PREF_KEY_SLACK_LOGO_URL) ?: ""

    postSuccessful = try {
      mainConfigElement.getAttribute(ATTR_NAME_POST_SUCCESSFUL)?.booleanValue ?: true
    } catch (ex: DataConversionException) {
      true
    }
    postStarted = try {
      mainConfigElement.getAttribute(ATTR_NAME_POST_STARTED)?.booleanValue ?: false
    } catch (ex: DataConversionException) {
      false
    }
    postFailed = try {
      mainConfigElement.getAttribute(ATTR_NAME_POST_FAILED)?.booleanValue ?: false
    } catch (ex: DataConversionException) {
      false
    }
  }

  override fun writeTo(element: Element) {
    val mainConfigElement = Element(PREF_CHILD_ELEMENT)
    val defChannelElement = Element(PREF_KEY_SLACK_DEF_CHANNEL)
    val postUrlElement = Element(PREF_KEY_SLACK_POST_URL)
    val logoUrlElement = Element(PREF_KEY_SLACK_LOGO_URL)

    defChannelElement.text = defaultChannel

    mainConfigElement.addContent(defChannelElement)
    mainConfigElement.addContent(postUrlElement)
    mainConfigElement.addContent(logoUrlElement)

    element.addContent(mainConfigElement)
  }

  companion object {
    private const val DEFAULT_CHANNEL = "#general"

    private const val PREF_CHILD_ELEMENT = "slackNotifier"

    private const val ATTR_NAME_POST_SUCCESSFUL = "postSuccessful"
    private const val ATTR_NAME_POST_STARTED = "postStarted"
    private const val ATTR_NAME_POST_FAILED = "postFailed"

    const val PREF_KEY_SLACK_DEF_CHANNEL = "slackDefaultChannel"
    const val PREF_KEY_SLACK_POST_URL = "slackPostUrl"
    const val PREF_KEY_SLACK_LOGO_URL = "slackLogoUrl"
  }
}
