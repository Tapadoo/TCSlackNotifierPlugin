# TCStackNotifierPlugin - TeamCity -> Stack Notifications

A plugin for [TeamCity](http://www.jetbrains.com/teamcity/) to post notifications to [slack](https://slack.com/)

It works by registering as a server listener, and posts to slack on successful builds finishing.

#Build Plugin

Gradle is used to build. Wrapper is included in the project so you dont need to install it, just have java.

    ./gradlew buildZip

this will generate a zip file with the right meta data in the right folder structure at : `build/distributions/TCSlackNotifierPlugin-<version>.zip`

#Install Plugin

Copy the zip file into TeamCity plugin directory inside the data directory, usually `.BuildServer`

```
scp build/distributions/TCSlackNotifierPlugin-<version>.zip buildserver:.BuildServer/plugins/slackNotifier.zip
```

Then restart TeamCity.

#Configuration

###In slack
Add a new webhook integration. Make a note of the Token.

###In TeamCity

Edit the main config file, usually `.BuildServer/config/main-config.xml` and add an element like so:
```
<server rootURL="http://localhost:8111">
  ...
  <slackNotifier>
    <slackWebToken>testToken2</slackWebToken>
    <slackDefaultChannel>#general</slackDefaultChannel>
    <slackPostUrl>https://tapadoo.slack.com/services/hooks/incoming-webhook?token=</slackPostUrl>
    <slackLogoUrl>http://build.tapadoo.com/img/icons/TeamCity32.png</slackLogoUrl>
  </slackNotifier>
  ...
  ...
```

Replace the web token with the token from slack. Change the postUrl also to point to the right slack team. The url can be found in the webhook integraton page, just remove the token from the end. Change the logo url whatever you want.

This by default will post all builds to slack. you can tweak these on a project level though

####Project Config (Optional)

To change channel or disable per project:

Edit the plugin specific xml config, `plugin-settings.xml` probably somewhere inside `.BuildServer/config/projects/PROJECTNAME`
```
<settings>
  <slackSettings enabled="true">
    <channel>#blah</channel>
  </slackSettings>
</settings>
```

#Note on TeamCity version support

I'm still using **TeamCity 7.1** , but a quick test on the free version of TeamCity 8 went ok

###Issues

* all xml config - needs web ui extensions for updating settings from GUI. Considering it.
* channel can be changed per-project either by environmental variable (SLACK_CHANNEL) or by changing the project specific xml in the data directory. This could also use web ui extension UI for editing.
* All or nothing notifications. By default, all builds are posted. It can be disabled per project, but not currently by build config.
