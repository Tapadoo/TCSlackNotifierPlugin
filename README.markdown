# TCSlackNotifierPlugin - TeamCity -> Slack Notifications

A plugin for [TeamCity](http://www.jetbrains.com/teamcity/) to post notifications to [Slack](https://slack.com/)

It works by registering as a server listener, and posts to Slack on build events like successful builds (optionally also builds starting and failing)

# Build Plugin

Gradle is used to build. Wrapper is included in the project so you dont need to install it, just have java.

    ./gradlew buildZip

this will generate a zip file with the right meta data in the right folder structure at : `build/distributions/TCSlackNotifierPlugin-<version>.zip` you can also download a build from GitHubs versions section.

# Install Plugin

Copy the zip file into TeamCity plugin directory inside the data directory, usually `.BuildServer`

```
scp build/distributions/TCSlackNotifierPlugin-<version>.zip buildserver:.BuildServer/plugins/
```

Then restart TeamCity.

# Configuration

### In Slack
Add a new webhook integration. Make a note of the URL.

### In TeamCity

Edit the main config file, usually `.BuildServer/config/main-config.xml` and add an element like so:

```
<server rootURL="http://localhost:8111">
  ...
  <slackNotifier postSuccessful="true" postFailed="false" postStarted="false" >
    <slackDefaultChannel>#general</slackDefaultChannel>
    <slackPostUrl>https://hooks.slack.com/services/YYYYYY/XXXXXXX/ZZZZZZZZZZZZ</slackPostUrl>
    <slackLogoUrl>http://build.tapadoo.com/img/icons/TeamCity32.png</slackLogoUrl>
  </slackNotifier>
  ...
  ...
```

You can set the attributes on slackNotifier element (postSuccessful,postFailed,postStarted) to decide that notifications you would like posted.

Set the `slackPostUrl` to point to the url provided on the Slack integration page for the incoming webhook you created. Change the logo url whatever you want or leave it out.

This by default will post all builds to Slack. You can tweak these on a project level though.

#### Project Config (Optional)

To change channel, change the Slack logo used for that project or disable per project:

Edit the plugin specific xml config, `plugin-settings.xml` probably somewhere inside `.BuildServer/config/projects/PROJECTNAME`

```
<settings>
  <slackSettings enabled="true">
    <channel>#blah</channel>
    <logoUrl>http://host/somelogo.png</logoUrl>
  </slackSettings>
</settings>
```

# Note on TeamCity version support

I'm still using **TeamCity 7.1** , but a few tests on the free version of TeamCity 8 went fine, and it seems to work there also. Users have reported it working on version 9 also.

### Issues

* All xml config - needs web ui extensions for updating settings from GUI. Considering it.
* Channel can be changed per-project either by environmental variable (SLACK_CHANNEL (env var may be broken)) or by changing the project specific xml in the data directory. This could also use web ui extension UI for editing.
* All or nothing notifications. By default, all builds are posted. It can be disabled per project, but not currently by build config.


# License

MIT License.
