package com.tapadoo.slacknotifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jetbrains.buildServer.issueTracker.Issue;
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
import java.util.Collection;


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
        String message = "STARTED " + build.getFullName() + " (" + build.getBranch().getName() + ")" + " #" + build.getBuildNumber()+"\n";
        message += "Agent: " + build.getAgent().getHostName() + " : " + build.getAgent().getHostAddress() + "\n";
        if (build.getBuildComment() != null) {
            message += build.getBuildComment().getUser() + " " + build.getBuildComment().getComment();
        }
        //String message = String.format("'%s' (%s) build started on Agent: %s." , build.getFullName(), build.getBranch(), build.getAgent().getHostName());
        postToSlack(build, message, true);
    }
    private void postFailureBuild(SRunningBuild build )
    {
        String message = "";

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

        message += "FAILURE " + build.getFullName() + " (" + build.getBranch().getName() + ")" + " #" + build.getBuildNumber()+"\n";
        message += "Agent: " + build.getAgent().getHostName() + " : " + build.getAgent().getHostAddress() + "\n";
        message += "Build failed in " + durationFormatter.print(buildDuration.toPeriod());
        if (build.getBuildComment() != null) {
            message += "\n" + build.getBuildComment().getUser() + " " + build.getBuildComment().getComment();
        }
        //message = String.format("'%s' (%s) build failed on Agent: %s! (%s)" , build.getFullName(), build.getBranch().getDisplayName(), build.getAgent().getHostName() ,durationFormatter.print(buildDuration.toPeriod()));

        postToSlack(build, message, false);
    }

    private void processSuccessfulBuild(SRunningBuild build) {

        String message = "";

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

        message += "SUCCESS " + build.getFullName() + " (" + build.getBranch().getName() + ")" +  " #" + build.getBuildNumber()+"\n";
        message += "Agent: " + build.getAgent().getHostName() + " : " + build.getAgent().getHostAddress() + "\n";
        message += "Build succeeded in " + durationFormatter.print(buildDuration.toPeriod());
        if (build.getBuildComment() != null) {
            message += "\n" + build.getBuildComment().getUser() + " " + build.getBuildComment().getComment();
        }
        //message = String.format("'%s' (%s) built successfully in %s on Agent: %s." , build.getFullName(), build.getBranch().getDisplayName(), durationFormatter.print(buildDuration.toPeriod()), build.getAgent().getHostName());

        postToSlack(build, message, true);
    }

    /**
     * Post a payload to slack with a message and good/bad color. Committer summary is automatically added as an attachment
     * @param build the build the message is relating to
     * @param message main message to include, 'Build X completed...' etc
     * @param goodColor true for 'good' builds, false for danger.
     */
    private void postToSlack(SRunningBuild build, String message, boolean goodColor) {
        try{

            URL url = new URL(slackConfig.getPostUrl());

            SlackProjectSettings projectSettings = (SlackProjectSettings) projectSettingsManager.getSettings(build.getProjectId(),"slackSettings");

            if( ! projectSettings.isEnabled() )
            {
                return ;
            }

            String iconUrl = projectSettings.getLogoUrl();

            if(iconUrl == null || iconUrl.length() < 1 )
            {
                iconUrl = slackConfig.getLogoUrl() ;
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

            UserSet<SUser> committers = build.getCommitters(SelectPrevBuildPolicy.SINCE_LAST_BUILD);
            StringBuilder committersString = new StringBuilder();

            for( SUser committer : committers.getUsers() )
            {
                if( committer != null)
                {
                    String committerName = committer.getName() ;
                    if( committerName == null || committerName.equals("") )
                    {
                        committerName = committer.getUsername() ;
                    }

                    if( committerName != null && !committerName.equals(""))
                    {
                        committersString.append(committerName);
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
            payloadObj.addProperty("icon_url",iconUrl);

            JsonArray attachmentsObj = new JsonArray();

            if( commitMsg.length() > 0 )
            {
                JsonObject attachment = new JsonObject();

                attachment.addProperty("fallback", "Changes by"+ commitMsg);
                attachment.addProperty("color",( goodColor ? "good" : "danger"));

                JsonArray fields = new JsonArray();
                JsonObject field = new JsonObject() ;

                field.addProperty("title","Changes By");
                field.addProperty("value",commitMsg);
                field.addProperty("short", true);

                fields.add(field);
                attachment.add("fields",fields);

                attachmentsObj.add(attachment);

            }

            //Do we have any issues?

            if( build.isHasRelatedIssues() )
            {
                //We do!
                Collection<Issue> issues = build.getRelatedIssues();
                JsonObject issuesAttachment = new JsonObject();

                StringBuilder issueIds = new StringBuilder();
                StringBuilder clickableIssueIds = new StringBuilder();

                for( Issue issue : issues )
                {
                    issueIds.append(',');
                    issueIds.append(issue.getId());

                    clickableIssueIds.append(',');

                    clickableIssueIds.append('<');
                    clickableIssueIds.append(issue.getUrl());
                    clickableIssueIds.append('|');
                    clickableIssueIds.append(issue.getId());
                    clickableIssueIds.append('>');
                }

                if( issueIds.length() > 0 )
                {
                    issueIds.deleteCharAt(0); //delete first ','
                }

                if( clickableIssueIds.length() > 0 )
                {
                    clickableIssueIds.deleteCharAt(0); //delete first ','
                }

                issuesAttachment.addProperty("fallback" , "Issues " + issueIds.toString());
                //Not sure what color, if any to use for this. For now, leave it the same as the committers one
                issuesAttachment.addProperty("color",( goodColor ? "good" : "danger"));

                JsonArray fields = new JsonArray();
                JsonObject field = new JsonObject() ;

                field.addProperty("title","Related Issues");
                field.addProperty("value",clickableIssueIds.toString());
                field.addProperty("short", true);

                fields.add(field);
                issuesAttachment.add("fields", fields);

                attachmentsObj.add(issuesAttachment);
            }

            if( attachmentsObj.size() > 0 ) {
                payloadObj.add("attachments", attachmentsObj);
            }

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

}

