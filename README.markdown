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

#Note on TeamCity version support

I'm still using **TeamCity 7.1** so I haven't even checked to see if its ok on newer versions. It's probably grand.

###Issues

* all xml config - needs web ui extensions for updating settings from GUI. Considering it.
* channel can be changed per-project either by environmental variable (SLACK_CHANNEL) or by changing the project specific xml in the data directory. not properly documented yet - could also use web ui extension or custom tab to allow settings project settings from UI, if web ui input is done.
* All or nothing notifications. Personally, I wanted all projects getting posted, and didn't want to have to go edit every project to enable notifications. some people might not like that. I guess if there was a web ui for project level settings, it could include a checkbox to enable or disable notifications
    * actually, I should add a enabled flag to the SlackProjectSettings even before web stuff - at least that way server admin can tweak xml to disable. better than nothing.
