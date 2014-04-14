package com.tapadoo.slacknotifier;

import jetbrains.buildServer.serverSide.MainConfigProcessor;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Created by jasonconnery on 02/03/2014.
 */
public class SlackConfigProcessor implements MainConfigProcessor {

    public static final String PREF_KEY_SLACK_WEB_TOKEN = "slackWebToken";
    public static final String PREF_KEY_SLACK_DEF_CHANNEL = "slackDefaultChannel";
    public static final String PREF_KEY_SLACK_POSTURL = "slackPostUrl";
    public static final String PREF_KEY_SLACK_LOGOURL = "slackLogoUrl";

    private static final java.lang.String PREF_CHILD_ELEMENT = "slackNotifier";

    private static final java.lang.String ATTR_NAME_POST_SUCCESSFUL = "postSuccessful" ;
    private static final java.lang.String ATTR_NAME_POST_STARTED = "postStarted" ;
    private static final java.lang.String ATTR_NAME_POST_FAILED = "postFailed" ;


    private String token = "invalidToken";
    private String defaultChannel = "#general";
    private String postUrl;
    private String logoUrl;

    private boolean postSuccessful = true ;
    private boolean postStarted = false ;
    private boolean postFailed = false ;

    public SlackConfigProcessor()
    {

    }

    public void init()
    {

    }

    public boolean postSuccessful()
    {
        return postSuccessful ;
    }

    public boolean postFailed()
    {
        return postFailed ;
    }

    public boolean postStarted()
    {
        return postStarted ;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void readFrom(org.jdom.Element element) {
        Element mainConfigElement = element.getChild(PREF_CHILD_ELEMENT);

        if( mainConfigElement == null )
        {
            token = "" ;
            postUrl = "http://localhost/?token=" ;
            return ;
        }

        token = mainConfigElement.getChildText(PREF_KEY_SLACK_WEB_TOKEN);
        defaultChannel = mainConfigElement.getChildText(PREF_KEY_SLACK_DEF_CHANNEL);
        postUrl = mainConfigElement.getChildText(PREF_KEY_SLACK_POSTURL);
        logoUrl = mainConfigElement.getChildText(PREF_KEY_SLACK_LOGOURL);

        Attribute postSuccessfulAttr = mainConfigElement.getAttribute(ATTR_NAME_POST_SUCCESSFUL);
        Attribute postStartedAttr = mainConfigElement.getAttribute(ATTR_NAME_POST_STARTED);
        Attribute postFailedAttr = mainConfigElement.getAttribute(ATTR_NAME_POST_FAILED);

        if( postSuccessfulAttr != null )
        {
            try {
                postSuccessful = postSuccessfulAttr.getBooleanValue();
            }
            catch( DataConversionException ex )
            {
                postSuccessful = true ;
            }
        }

        if( postStartedAttr != null )
        {
            try {
                postStarted = postStartedAttr.getBooleanValue();
            }
            catch( DataConversionException ex )
            {
                postStarted = false ;
            }
        }

        if( postFailedAttr != null )
        {
            try {
                postFailed = postFailedAttr.getBooleanValue();
            }
            catch( DataConversionException ex )
            {
                postFailed = false ;
            }
        }

    }

    public void writeTo(org.jdom.Element element) {

        Element mainConfigElement = new Element(PREF_CHILD_ELEMENT);
        Element webTokenElement = new Element(PREF_KEY_SLACK_WEB_TOKEN);
        Element defChannelElement = new Element(PREF_KEY_SLACK_DEF_CHANNEL);
        Element postUrlElement = new Element(PREF_KEY_SLACK_POSTURL);
        Element logoUrlElement = new Element(PREF_KEY_SLACK_LOGOURL);

        webTokenElement.setText(token);
        defChannelElement.setText(defaultChannel);

        mainConfigElement.addContent(webTokenElement);
        mainConfigElement.addContent(defChannelElement);
        mainConfigElement.addContent(postUrlElement);
        mainConfigElement.addContent(logoUrlElement);

        element.addContent(mainConfigElement);


    }
}
