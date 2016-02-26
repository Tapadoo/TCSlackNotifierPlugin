package com.tapadoo.slacknotifier;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Created by jasonconnery on 02/03/2014.
 */
public class SlackProjectSettings implements ProjectSettings {

    public static final String ELEMENT_LOGO_URL = "logoUrl";
    public static final String ATTR_ENABLED = "enabled";
    public static final String ELEMENT_CHANNEL = "channel";

    private static final java.lang.String ATTR_NAME_POST_SUCCESSFUL = "postSuccessful";
    private static final java.lang.String ATTR_NAME_POST_STARTED = "postStarted";
    private static final java.lang.String ATTR_NAME_POST_FAILED = "postFailed";

    private String projectId;
    private String channel;
    private String logoUrl;

    private boolean enabled = true;
    private Boolean postSuccessful = null;
    private Boolean postStarted = null;
    private Boolean postFailed = null;

    public SlackProjectSettings(String projectId) {
        this.projectId = projectId;
    }

    public SlackProjectSettings() {

    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean postSuccessfulSet() { return this.postSuccessful != null;}

    public boolean postSuccessfulEnabled() { return this.postSuccessful; }

    public boolean postStartedSet() { return this.postStarted != null;}

    public boolean postStartedEnabled() { return this.postStarted; }

    public boolean postFailedSet() { return this.postFailed != null;}

    public boolean postFailedEnabled() { return this.postFailed; }

    public void dispose() {

    }

    public void readFrom(Element element) {
        Element channelElement = element.getChild(ELEMENT_CHANNEL);
        Element logoElement = element.getChild(ELEMENT_LOGO_URL);
        Attribute enabledAttr = element.getAttribute(ATTR_ENABLED);

        Attribute postSuccessfulAttr = element.getAttribute(ATTR_NAME_POST_SUCCESSFUL);
        Attribute postStartedAttr = element.getAttribute(ATTR_NAME_POST_STARTED);
        Attribute postFailedAttr = element.getAttribute(ATTR_NAME_POST_FAILED);

        enabled = tryGetBooleanAttributeValue(enabledAttr);
        postSuccessful = tryGetBooleanAttributeValue(postSuccessfulAttr);
        postFailed = tryGetBooleanAttributeValue(postFailedAttr);
        postStarted = tryGetBooleanAttributeValue(postStartedAttr);

        if (channelElement != null) {
            this.channel = channelElement.getText();
        }

        if (logoElement != null) {
            this.logoUrl = logoElement.getText();
        }
    }

    public void writeTo(Element element) {

        Element channelElement = new Element(ELEMENT_CHANNEL);
        channelElement.setText(this.channel);

        Element logoUrlElement = new Element(ELEMENT_LOGO_URL);
        logoUrlElement.setText(this.logoUrl);

        Attribute enabledAttr = new Attribute(ATTR_ENABLED, Boolean.toString(enabled));
        element.setAttribute(enabledAttr);

        Attribute postSuccessfulAttr = new Attribute(ATTR_NAME_POST_SUCCESSFUL, Boolean.toString(postSuccessful));
        element.setAttribute(postSuccessfulAttr);

        Attribute postFailedAttr = new Attribute(ATTR_NAME_POST_FAILED, Boolean.toString(postFailed));
        element.setAttribute(postFailedAttr);

        Attribute postStartedAttr = new Attribute(ATTR_NAME_POST_STARTED, Boolean.toString(postStarted));
        element.setAttribute(postStartedAttr);

        element.addContent(channelElement);
        element.addContent(logoUrlElement);
    }

    private Boolean tryGetBooleanAttributeValue(Attribute attr) {
        if (attr != null) {
            try {
                return attr.getBooleanValue();
            } catch (DataConversionException e) {
                return null;
            }
        }

        return null;
    }

}
