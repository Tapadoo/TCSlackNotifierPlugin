# 4.5.0
* changing plugin name from slackNotifier to tpSlackNotifier incase it's causing conflicts with official one

# 4.4.1
* Added support for teamcity configuration parameter "system.SLACK_IGNORE_COMMIT_MESSAGE" to allow user to ignore adding commits to slack message.

# 4.4.0
* Added support for teamcity configuration parameter "system.POST_TO_SLACK" to supress posts from config level instead of project level by setting it to false or 0. Fixed bug with existing system.SLACK_CHANNEL param name for overwriting settings at the config level.

# 4.3.0
* Bumped up Teamcity library versions to 2018

# 4.2.2
* Added test passed/failed count as another field to the Slack post

# 4.1.1
* Moved commit messages to their own attachment and updated formatting

# 4.0.2
* Added commit messages as a field to slack message

# 3.0.0
* recompiled for TeamCity 2017.2, with updated dependencies, Java 8.

# 2.6.0
* Missing/Internal release

# 2.5.0
* Missing/Internal release

# 2.4.0

* Added issues as attachment
* Support for new Slack url format (droped splitting url & token that wasn't needed)

# 2.3.0

* skipped - internal / test / scrapped version, not needed

# 2.2.0

* added support for notifying build start and failute. disabled by default can be enabled in xml settings - see README
