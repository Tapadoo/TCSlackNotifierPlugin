# TCStackNotifierPlugin - TeamCity -> Stack Notifications

A hastily built plugin for [TeamCity](http://www.jetbrains.com/teamcity/) to post notifications to [slack](https://slack.com/)

#Before Installing

As it's something I put together in an excited rush, not everything that should be configurable is, so some stuff was hard coded just for our company. I'll change this in a future build at some point, but pull requests are welcome :) - in `SlackNotifier.java` there's a `postUrl` field hardcoded to our instance of slack:

```
    //Todo - make user configurable or plugin configurable or something
    private static final String postUrl = "https://tapadoo.slack.com/services/hooks/incoming-webhook?token=";
    private static final String logoUrl = "http://build.tapadoo.com/img/icons/TeamCity32.png";
```
You'll need to change that before compiling. You might as well change the logo location also.

#Build Plugin

Gradle is used to build. Wrapper is included in the project so you dont need to install it, just have java.

    ./gradlew buildZip

this will generate a zip file with the right meta data in the right folder structure at : `build/distributions/TCSlackNotifierPlugin-1.0.3.zip`

#Install Plugin

Copy the zip file into TeamCity plugin directory inside the data directory, usually `.BuildServer`

```
scp build/distributions/TCSlackNotifierPlugin-1.0.3.zip buildserver:.BuildServer/plugins/slackNotifier.zip
```

Then restart TeamCity.

#Configuration

###In slack
Add a new webhook integration. Make a note of the Token.

###In TeamCity
go to your Notification Settings. You'll see a slack notifier option now. Set the token and the desired slack channel.

Create a new notification rule for slack. What I ended up using is All projects (not just those with my changes) and successful only. The plugin intentionally only bothers posting successes for now by design. I wanted everyone to bask in the warm feeling of success while allowing individuals to continue to receive failures directly via usual notifcations and hide their shame while they fixed their broken builds.

#Note on TeamCity version support

I'm still using **TeamCity 7.1** so I haven't even checked to see if its ok on newer versions. It's probably grand.

###Issues

* all notifications go to the one channel. Could setup multiple users with different settings, or maybe add a channel as a build setting or env var or something and read that from plugin. Then that would allow changing from default on a per team city configration basis.
* It's configured as a user notification on TC , but posts to all on Stack. Dont think you can do 'server' notifications. Side effect of this is only one user should configure the plugin on the teamcity side. If multiple people configure it, you may get mutliple slack posts, unless they are going to different channels.
* I found if other notifications fired for success (such as jabber notifications for my changes) then slack notifications didn't happen for my changes. Maybe a good idea to setup on it's own account or something or remove other success notifications
* I never bothered testing with personal builds. It might fire for those. Might not be desired. Should be easy to add a check for.
