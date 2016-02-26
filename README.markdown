# TCSlackNotifierPlugin - TeamCity -> Slack Notifications

A plugin for [TeamCity](http://www.jetbrains.com/teamcity/) to post notifications to [slack](https://slack.com/)

It works by registering as a server listener, and posts to slack on build events like successful builds (optionally also builds starting and failing)

#Build Plugin

Gradle is used to build. Wrapper is included in the project so you dont need to install it, just have java.

    ./gradlew buildZip

this will generate a zip file with the right meta data in the right folder structure at : `build/distributions/TCSlackNotifierPlugin-<version>.zip` you can also download a build from GitHubs versions section.

#Install Plugin

Copy the zip file into TeamCity plugin directory inside the data directory, usually `.BuildServer`

```
scp build/distributions/TCSlackNotifierPlugin-<version>.zip buildserver:.BuildServer/plugins/
```

Then restart TeamCity.

#Configuration

###In slack
Add a new webhook integration. Make a note of the URL.

###In TeamCity

Edit the main config file, usually `.BuildServer/config/main-config.xml` and add an element like so:

```
<server rootURL="http://localhost:8111">
  ...
  <slackNotifier postSuccessful="true" postFailed="false" postStarted="false" >
    <slackDefaultChannel>#general</slackDefaultChannel>
    <slackPostUrl>https://hooks.slack.com/services/YYYYYY/XXXXXXX/ZZZZZZZZZZZZ</slackPostUrl>
    <slackLogoUrl>http://build.tapadoo.com/img/icons/TeamCity32.png</slackLogoUrl>
    <buildFailedPermalink>http://build.tapadoo.com/viewLog.html?buildTypeId=XXXXXXX&amp;buildId=lastFinished</buildFailedPermalink>
  </slackNotifier>
  ...
  ...
```

You can set the attributes on slackNotifier element (postSuccessful,postFailed,postStarted) to decide that notifications you would like posted.

Set the **slackPostUrl** to point to the url provided on the Slack integration page for the incoming webhook you created. Change the logo url whatever you want or leave it out.

This by default will post all builds to slack. you can tweak these on a project level though

####Project Config (Optional)

To change channel, change the slack logo used for that project or disable per project:

Edit the plugin specific xml config, `plugin-settings.xml` probably somewhere inside `.BuildServer/config/projects/PROJECTNAME`

```
<settings>
  <slackSettings enabled="true">
    <channel>#blah</channel>
    <logoUrl>http://host/somelogo.png</logoUrl>
  </slackSettings>
</settings>
```

Alternatively, you may also override the alert selections that were specified in main-config.xml:

```
<settings>
  <slackSettings enabled="true" postSuccessful="false" postFailed="true" postStarted="false">
    <channel>#blah</channel>
    <logoUrl>http://host/somelogo.png</logoUrl>
  </slackSettings>
</settings>
```

#Note on TeamCity version support

I'm still using **TeamCity 7.1** , but a few tests on the free version of TeamCity 8 went fine, and it seems to work there also. Users have reported it working on version 9 also.

###Issues

* all xml config - needs web ui extensions for updating settings from GUI. Considering it.
* channel can be changed per-project either by environmental variable (SLACK_CHANNEL (env var may be broken)) or by changing the project specific xml in the data directory. This could also use web ui extension UI for editing.

* Project-level notification selection only.  Notification settings per individual build configuration are not supported.


# License

MIT License.
