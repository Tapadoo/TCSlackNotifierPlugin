package com.tapadoo.slacknotifier;

import jetbrains.buildServer.Build;
import  jetbrains.buildServer.notification.Notificator ;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.VcsRoot;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by jasonconnery on 13/02/2014.
 */
public class SlackNotifier implements Notificator {

    private static final String TYPE = "slackNotifier";
    private static final String TYPE_NAME = "Slack Notifier";
    private static final String SLACK_CHANNEL_NAME = "slackChannelName";
    private static final String SLACK_TOKEN = "slackToken";

    private static final PropertyKey CHANNEL_NAME = new NotificatorPropertyKey(TYPE, SLACK_CHANNEL_NAME);
    private static final PropertyKey TOKEN = new NotificatorPropertyKey(TYPE, SLACK_TOKEN);

    public SlackNotifier(NotificatorRegistry notificatorRegistry) throws IOException {
        ArrayList<UserPropertyInfo> userProps = new ArrayList<UserPropertyInfo>();
        userProps.add(new UserPropertyInfo(SLACK_CHANNEL_NAME, "Channel Name"));
        userProps.add(new UserPropertyInfo(SLACK_TOKEN, "Webhook Token"));
        notificatorRegistry.register(this, userProps);
    }

    private void postSuccessToSlack(String name , Set<SUser> users)
    {
        String postUrl = "https://tapadoo.slack.com/services/hooks/incoming-webhook?token=";
        for( SUser user : users)
        {

            try{

                String channel = user.getPropertyValue(CHANNEL_NAME);
                String finalUrl = postUrl + user.getPropertyValue(TOKEN);
                URL url = new URL(finalUrl);

                String payload = String.format("payload={\"channel\": \"%s\", \"username\": \"TeamCity\", \"text\": \"Project '%s' built successfully.\", \"icon_url\":\"http://build.tapadoo.com/img/icons/TeamCity32.png\"}" , channel, name);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);

                BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
                bos.write(payload.getBytes("utf8"));
                bos.flush();

                conn.getResponseCode() ;
            }
            catch ( MalformedURLException ex )
            {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void notifyBuildStarted(SRunningBuild sRunningBuild, Set<SUser> sUsers) {

    }

    @Override
    public void notifyBuildSuccessful(SRunningBuild sRunningBuild, Set<SUser> sUsers) {
        postSuccessToSlack(sRunningBuild.getFullName() , sUsers);
    }

    @Override
    public void notifyBuildFailed(SRunningBuild sRunningBuild, Set<SUser> sUsers) {

    }

    @Override
    public void notifyBuildFailedToStart(SRunningBuild sRunningBuild, Set<SUser> sUsers) {

    }

    @Override
    public void notifyLabelingFailed(Build build, VcsRoot vcsRoot, Throwable throwable, Set<SUser> sUsers) {

    }

    @Override
    public void notifyBuildFailing(SRunningBuild sRunningBuild, Set<SUser> sUsers) {

    }

    @Override
    public void notifyBuildProbablyHanging(SRunningBuild sRunningBuild, Set<SUser> sUsers) {

    }

    @Override
    public void notifyResponsibleChanged(SBuildType sBuildType, Set<SUser> sUsers) {

    }

    @Override
    public void notifyResponsibleAssigned(SBuildType sBuildType, Set<SUser> sUsers) {

    }

    @Override
    public void notifyResponsibleChanged(TestNameResponsibilityEntry testNameResponsibilityEntry, TestNameResponsibilityEntry testNameResponsibilityEntry2, SProject sProject, Set<SUser> sUsers) {

    }

    @Override
    public void notifyResponsibleAssigned(TestNameResponsibilityEntry testNameResponsibilityEntry, TestNameResponsibilityEntry testNameResponsibilityEntry2, SProject sProject, Set<SUser> sUsers) {

    }

    @Override
    public void notifyResponsibleChanged(Collection<TestName> testNames, ResponsibilityEntry responsibilityEntry, SProject sProject, Set<SUser> sUsers) {

    }

    @Override
    public void notifyResponsibleAssigned(Collection<TestName> testNames, ResponsibilityEntry responsibilityEntry, SProject sProject, Set<SUser> sUsers) {

    }

    @Override
    public void notifyTestsMuted(Collection<STest> sTests, MuteInfo muteInfo, Set<SUser> sUsers) {

    }

    @Override
    public void notifyTestsUnmuted(Collection<STest> sTests, MuteInfo muteInfo, SUser sUser, Set<SUser> sUsers) {

    }

    @Override
    public String getNotificatorType() {
        return TYPE;
    }

    @Override
    public String getDisplayName() {
        return TYPE_NAME;
    }
}
