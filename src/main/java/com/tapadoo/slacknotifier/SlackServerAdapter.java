package com.tapadoo.slacknotifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserSet;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsRoot;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by jasonconnery on 02/03/2014.
 */
public class SlackServerAdapter extends BuildServerAdapter {

    private final SBuildServer buildServer;
    private final SlackConfigProcessor slackConfig;
    private final ProjectSettingsManager projectSettingsManager;
    private final ProjectManager projectManager ;
    private Gson gson ;

    public SlackServerAdapter(SBuildServer sBuildServer, ProjectManager projectManager, ProjectSettingsManager projectSettingsManager , SlackConfigProcessor configProcessor) {

        this.projectManager = projectManager ;
        this.projectSettingsManager = projectSettingsManager ;
        this.buildServer = sBuildServer ;
        this.slackConfig = configProcessor ;

    }

    public void init()
    {
        buildServer.addListener(this);
    }

    private Gson getGson()
    {
        if( gson == null )
        {
            gson = new GsonBuilder().create() ;
        }

        return gson ;
    }

    @Override
    public void buildStarted(SRunningBuild build) {
        super.buildStarted(build);

        if( !build.isPersonal() && slackConfig.postStarted() )
        {
            postStartedBuild(build);
        }
    }

    @Override
    public void buildFinished(SRunningBuild build) {
        super.buildFinished(build);

        boolean isPersonalBuild = build.isPersonal();
        Status buildStatus = build.getBuildStatus();

        if ( !isPersonalBuild )
        {
          if( buildStatus.isSuccessful() && slackConfig.postSuccessful() )
          {
              processSuccessfulBuild(build);
          }
          else if ( buildStatus.isFailed() && slackConfig.postFailed() )
          {
            postFailureBuild(build);
          }
          else
          {
            //TODO - modify in future if we care about other states
          }
        }
        else
        {
          if ( ( buildStatus.isSuccessful() || buildStatus.isFailed() ) )
          {
            postPersonalBuild(build);
          }
        }
    }

    private void postStartedBuild(SRunningBuild build )
    {
        //Could put other into here. Agents maybe?
        String message = String.format("Project '%s' build started." , build.getFullName());
        postToSlack(build, message, true);
    }

    private void postFailureBuild(SRunningBuild build ) {
        postToSlack(build);
    }

    private void processSuccessfulBuild(SRunningBuild build) {
        postToSlack(build);
    }

    private void postPersonalBuild(SRunningBuild build) {
        postToSlack(build);
    }

    private String messageFor(SRunningBuild build) {
        String statusString = build.getBuildStatus().isSuccessful() ? "succeeded.": "failed!";
        return String.format("[%s] Build %s (<%s|build log>)", build.getFullName(), statusString, linkToBuildLog(build));
    }

    private SlackProjectSettings getSlackSettings(SRunningBuild  build) {
        return (SlackProjectSettings) projectSettingsManager.getSettings(build.getProjectId(),"slackSettings");
    }

    /**
     * Get the channel for this build message. Precedence goes: channel in SLACK_CHANNEL parameter,
     * @username if personal and no SLACK_CHANNEL, channel from project-specific config, then channel
     * from global notifier config. 
     */ 
    private String channelFor(SRunningBuild build) {
        String channel = this.slackConfig.getDefaultChannel();

        String configuredChannel = build.getParametersProvider().get("SLACK_CHANNEL");
        if ( configuredChannel != null && configuredChannel.length() > 0 ) {
            channel = configuredChannel;
        }
        else {
            if ( build.isPersonal() ) {
                channel = String.format("@%s", build.getOwner().getUsername());
            }
            else {
                SlackProjectSettings projectSettings = getSlackSettings(build);
                if ( projectSettings != null ) {
                    String projectChannel = projectSettings.getChannel();
                    if ( projectChannel != null && projectChannel.length() > 0 ) {
                        channel = projectChannel;
                    }
                }
            }
        }

        return channel;
    }

    private void postToSlack(SRunningBuild build) {
        postToSlack(build, messageFor(build), channelFor(build), build.getBuildStatus().isSuccessful());
    }
 
    private void postToSlack(SRunningBuild build, boolean goodColor) {
        postToSlack(build, messageFor(build), channelFor(build), goodColor); 
    }

    private void postToSlack(SRunningBuild build, String message, boolean goodColor) {
        postToSlack(build, message, channelFor(build), goodColor);
    }

    /**
     * Post a payload to slack with a message and good/bad color. Commiter summary, running time, and a link 
     * to the build log are automatically added as an attachment
     * @param build the build the message is relating to
     * @param message main message to include, 'Build X completed...' etc
     * @param channel slack channel to post to
     * @param goodColor true for 'good' builds, false for danger.
     */
    private void postToSlack(SRunningBuild build, String message, String channel, boolean goodColor) {
        try{
            String finalUrl = slackConfig.getPostUrl() + slackConfig.getToken();
            URL url = new URL(finalUrl);


            SlackProjectSettings projectSettings = getSlackSettings(build);

            if( ! projectSettings.isEnabled() )
            {
                return ;
            }

            UserSet<SUser> commiters = build.getCommitters(SelectPrevBuildPolicy.SINCE_LAST_BUILD);
            StringBuilder committersString = new StringBuilder();

            for( SUser commiter : commiters.getUsers() )
            {
                if( commiter != null)
                {
                    String commiterName = commiter.getName() ;
                    if( commiterName == null || commiterName.equals("") )
                    {
                        commiterName = commiter.getUsername() ;
                    }

                    if( commiterName != null && !commiterName.equals(""))
                    {
                        committersString.append(commiterName);
                        committersString.append(",");
                    }
                }
            }

            if( committersString.length() > 0 )
            {
                committersString.deleteCharAt(committersString.length()-1); //remove the last ,
            }

            String commitMsg = committersString.toString();

            JsonObject payloadObj = new JsonObject();
            payloadObj.addProperty("channel" , channel);
            payloadObj.addProperty("username" , "TeamCity");
            payloadObj.addProperty("text", message);
            payloadObj.addProperty("icon_url",slackConfig.getLogoUrl());

            JsonArray attachmentsObj = new JsonArray();
            JsonObject attachment = new JsonObject();

            attachment.addProperty("fallback", "Changes by"+ commitMsg);
            attachment.addProperty("color",( goodColor ? "good" : "danger"));

            JsonArray fields = new JsonArray();
            fields.add(shortAttachmentField("Run time", formatBuildDuration(build)));

            if( commitMsg.length() > 0 )
            {
                fields.add(longAttachmentField("Changes By", commitMsg));
            }

            attachment.add("fields",fields);
            attachmentsObj.add(attachment);
            payloadObj.add("attachments" , attachmentsObj);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());

            String payloadJson = getGson().toJson(payloadObj);
            String bodyContents = "payload=" + payloadJson ;
            bos.write(bodyContents.getBytes("utf8"));
            bos.flush();
            bos.close();

            int serverResponseCode = conn.getResponseCode() ;

            conn.disconnect();
            conn = null ;
            url = null ;

        }
        catch ( MalformedURLException ex )
        {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Parse the build duration into the format [hours] [minutes] and [seconds].
    * @param build the build for which the duration should be formatted
    * @return      a duration string formatted for the end user 
    */
    private String formatBuildDuration(SRunningBuild build) {
        PeriodFormatter durationFormatter = new PeriodFormatterBuilder()
                .printZeroRarelyFirst()
                .appendHours()
                .appendSuffix(" hour", " hours")
                .appendSeparator(" ")
                .printZeroRarelyLast()
                .appendMinutes()
                .appendSuffix(" minute", " minutes")
                .appendSeparator(" and ")
                .appendSeconds()
                .appendSuffix(" second", " seconds")
                .toFormatter();

        Duration buildDuration = new Duration(1000*build.getDuration());

        return durationFormatter.print(buildDuration.toPeriod());
    }

    /**
    * Create a link to the build log for a given build.
    * @param build the build for which to generate a log link
    * @return      a link to the log for the specified build
    */ 
    private String linkToBuildLog(SRunningBuild build) {
        return String.format("%s/viewLog.html?buildId=%s&buildTypeId=%s&tab=buildResultsDiv", buildServer.getRootUrl(), build.getBuildId(), build.getBuildTypeId());
    }

    private JsonObject shortAttachmentField(String title, String value) {
        return attachmentField(title, value, true);
    }

    private JsonObject longAttachmentField(String title, String value) {
        return attachmentField(title, value, false);
    }
    
    private JsonObject attachmentField(String title, String value, boolean isShort) {
        JsonObject field = new JsonObject();
        field.addProperty("title", title);
        field.addProperty("value", value);
        field.addProperty("short", isShort);
        return field;
    }
    
}
