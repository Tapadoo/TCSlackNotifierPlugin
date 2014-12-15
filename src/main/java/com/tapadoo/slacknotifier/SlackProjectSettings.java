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
    private String projectId;
    private String channel;
    private String logoUrl;
    private boolean enabled = true ;

    public SlackProjectSettings(String projectId) {
        this.projectId = projectId ;
    }

    public SlackProjectSettings()
    {

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

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void dispose() {

    }

    public void readFrom(Element element) {
        Element channelElement = element.getChild(ELEMENT_CHANNEL);
        Element logoElement = element.getChild(ELEMENT_LOGO_URL);
        Attribute enabledAttr = element.getAttribute(ATTR_ENABLED);

        if( enabledAttr != null )
        {
            try {
                enabled = enabledAttr.getBooleanValue() ;
            } catch (DataConversionException e) {
                enabled = true ;
            }
        }
        else
        {
            enabled = true ;
        }

        if( channelElement != null ) {
            this.channel = channelElement.getText();
        }

        if( logoElement != null )
        {
            this.logoUrl = logoElement.getText();
        }
    }

    public void writeTo(Element element) {

        Element channelElement = new Element(ELEMENT_CHANNEL);
        channelElement.setText(this.channel);

        Element logoUrlElement = new Element(ELEMENT_LOGO_URL);
        logoUrlElement.setText(this.logoUrl);

        Attribute enabledAttr = new Attribute(ATTR_ENABLED,Boolean.toString(enabled)) ;
        element.setAttribute( enabledAttr );

        element.addContent(channelElement);
        element.addContent(logoUrlElement);
    }

}
