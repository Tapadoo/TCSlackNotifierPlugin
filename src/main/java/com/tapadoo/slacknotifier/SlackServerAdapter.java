package com.tapadoo.slacknotifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserSet;
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

        if( !build.isPersonal() && build.getBuildStatus().isSuccessful() && slackConfig.postSuccessful() )
        {
            processSuccessfulBuild(build);
        }
        else if ( !build.isPersonal() && build.getBuildStatus().isFailed() && slackConfig.postFailed() )
        {
            postFailureBuild(build);
        }
        else
        {
            //TODO - modify in future if we care about other states
        }
    }

    private void postStartedBuild(SRunningBuild build )
    {
        //Could put other into here. Agents maybe?
        String message = String.format("Project '%s' build started." , build.getFullName());
        postToSlack(build, message, true);
    }

    private void postFailureBuild(SRunningBuild build ) {
        String message = String.format("[%s]  Build failed!" , build.getFullName());
        postToSlack(build, message, false);
    }

    private void processSuccessfulBuild(SRunningBuild build) {
        String message = String.format("[%s] Build succeeded." , build.getFullName());
        postToSlack(build, message, true);
    }

    /**
     * Post a payload to slack with a message and good/bad color. Commiter summary, running time, and a link 
     * to the build log are automatically added as an attachment
     * @param build the build the message is relating to
     * @param message main message to include, 'Build X completed...' etc
     * @param goodColor true for 'good' builds, false for danger.
     */
    private void postToSlack(SRunningBuild build, String message, boolean goodColor) {
        try{

            String finalUrl = slackConfig.getPostUrl() + slackConfig.getToken();
            URL url = new URL(finalUrl);


            SlackProjectSettings projectSettings = (SlackProjectSettings) projectSettingsManager.getSettings(build.getProjectId(),"slackSettings");

            if( ! projectSettings.isEnabled() )
            {
                return ;
            }

            String configuredChannel = build.getParametersProvider().get("SLACK_CHANNEL");
            String channel = this.slackConfig.getDefaultChannel();

            if( configuredChannel != null && configuredChannel.length() > 0 )
            {
                channel = configuredChannel ;
            }
            else if( projectSettings != null && projectSettings.getChannel() != null && projectSettings.getChannel().length() > 0 )
            {
                channel = projectSettings.getChannel() ;
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
            fields.add(shortAttachmentField("Build number", String.format("%s <%s|(build log)>", build.getBuildNumber(), linkToBuildLog(build))));
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
