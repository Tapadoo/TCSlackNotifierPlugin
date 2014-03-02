package com.tapadoo.slacknotifier;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import org.jdom.Element;

/**
 * Created by jasonconnery on 02/03/2014.
 */
public class SlackProjectSettings implements ProjectSettings {

    private String projectId;
    private String channel;

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

    public void dispose() {

    }

    public void readFrom(Element element) {
        Element channelElement = element.getChild("channel");
        this.channel = channelElement.getText() ;
    }

    public void writeTo(Element element) {

        Element channelElement = new Element("channel");
        channelElement.setText(this.channel);

        element.addContent(channelElement);
    }
}
