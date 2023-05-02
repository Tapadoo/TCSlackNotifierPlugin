package com.tapadoo.slacknotifier

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tapadoo.slacknotifier.data.SlackField
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

import java.io.BufferedOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * @author Jason Connery
 * @since 04/03/2014
 */
class SlackServerAdapter(
    private val buildServer: SBuildServer,
    private val projectManager: ProjectManager,
    private val projectSettingsManager: ProjectSettingsManager,
    private val slackConfig: SlackConfigProcessor) : BuildServerAdapter() {

  private val gson: Gson by lazy { GsonBuilder().create() }

  fun init() {
    buildServer.addListener(this)
  }

  override fun buildStarted(build: SRunningBuild) {
    super.buildStarted(build)

    if (slackConfig.postStarted) postStartedBuild(build)
  }

  override fun buildFinished(build: SRunningBuild) {
    super.buildFinished(build)

    when {
      build.buildStatus.isSuccessful -> processSuccessfulBuild(build)
      build.buildStatus.isFailed -> postFailureBuild(build)
      else -> {
        //TODO - modify in future if we care about other states
      }
    }
  }

  private fun postStartedBuild(build: SRunningBuild) {
    // Could put other into here. Agents maybe?
    val message = "Project '${build.fullName}' build started."
    postToSlack(build, message, true)
  }

  private fun postFailureBuild(build: SRunningBuild) {
    if (!slackConfig.postFailed) return

    val durationFormatter = PeriodFormatterBuilder()
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
        .toFormatter()

    val buildDuration = Duration(1000 * build.duration)
    val message = "Project '${build.fullName}' build failed! (${durationFormatter.print(buildDuration.toPeriod())})"
    postToSlack(build, message, false)
  }

  private fun processSuccessfulBuild(build: SRunningBuild) {
    if (!slackConfig.postSuccessful) return

    val durationFormatter = PeriodFormatterBuilder()
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
        .toFormatter()

    val buildDuration = Duration(1000 * build.duration)
    val message = "Project '${build.fullName}' built successfully in ${durationFormatter.print(buildDuration.toPeriod())}."

    postToSlack(build, message, true)
  }

  /**
   * Post a payload to slack with a message and good/bad color. Committer summary is automatically added as an attachment
   *
   * @param build the build the message is relating to
   * @param message main message to include, 'Build X completed...' etc
   * @param goodColor true for 'good' builds, false for danger.
   */
  private fun postToSlack(build: SRunningBuild, message: String, goodColor: Boolean) {
    val postToSlack = build.parametersProvider.get("system.POST_TO_SLACK")
    if (build.isPersonal || postToSlack.equals("false", true) || postToSlack == "0") return

    try {
      val url = URL(slackConfig.postUrl)
      val projectSettings = projectSettingsManager.getSettings(build.projectId, "slackSettings") as SlackProjectSettings

      if (!projectSettings.isEnabled) return

      val iconUrl = projectSettings.logoUrl.takeIf { !it.isNullOrEmpty() } ?: slackConfig.logoUrl
      val configuredChannel = build.parametersProvider.get("system.SLACK_CHANNEL")
      val channel = if (!configuredChannel.isNullOrEmpty()) configuredChannel
      else if (!projectSettings.channel.isNullOrEmpty()) projectSettings.channel
      else slackConfig.defaultChannel

      val committersString = StringBuilder()
      build.getCommitters(SelectPrevBuildPolicy.SINCE_LAST_BUILD).users.forEach { committer ->
        val committerName = committer?.name?.takeIf { it.isNotEmpty() } ?: committer.username
        if (!committerName.isNullOrEmpty()) committersString.append("$committerName, ")
      }

      if (committersString.isNotEmpty()) committersString.deleteCharAt(committersString.length - 1) // remove the last ,
      val committersMsg = committersString.toString()

      val payloadObj = JsonObject().apply {
        addProperty("channel", channel)
        addProperty("username", "TeamCity")
        addProperty("text", message)
        addProperty("icon_url", iconUrl)
      }

      val attachmentsObj = JsonArray()
      val attachment = JsonObject()
      attachmentsObj.add(attachment)

      val fields = JsonArray()

      // Attach the build number
      fields.add(SlackField("Build", build.buildNumber, true).toJson())

      val fallbackMessage = StringBuilder()

      if (committersMsg.isNotEmpty()) {
        fields.add(SlackField("Changes By", committersMsg, true).toJson())
        fallbackMessage.append("Changes by $committersMsg ")
      }

      val testStats = build.getBuildStatistics(BuildStatisticsOptions.ALL_TESTS_NO_DETAILS)
      if (testStats.allTestRunCount > 0) {
        val failureCount = testStats.failedTestCount
        val successCount = testStats.passedTestCount

        fields.add(SlackField("Tests", "$successCount passed, $failureCount failed.", true).toJson())
      }

      // Do we have any issues?
      if (build.isHasRelatedIssues) {

        // We do!
        //val issues = build.relatedIssues.distinctBy { it.id } 
        val issues = build.relatedIssues
        val issueIds = StringBuilder()
        val clickableIssueIds = StringBuilder()

        issues.forEach { issue ->
          issueIds.append("${issue.id}, ")
          clickableIssueIds.append("<${issue.url}|${issue.id}>, ")
        }

        issueIds.dropLastWhile { it == ',' }
        clickableIssueIds.dropLastWhile { it == ',' }

        fields.add(SlackField("Related Issues", clickableIssueIds.toString(), true).toJson())
        fallbackMessage.append("Related Issues $issueIds")
      }

      attachment.addProperty("color", if (goodColor) "good" else "danger")
      attachment.add("fields", fields)

      val commitAttachment = createCommitAttachment(build)
      if (commitAttachment != null) attachmentsObj.add(commitAttachment)
      if (attachmentsObj.size() > 0) payloadObj.add("attachments", attachmentsObj)

      val conn = url.openConnection() as HttpURLConnection
      conn.doOutput = true

      BufferedOutputStream(conn.outputStream).use {
        val bodyContents = "payload=${gson.toJson(payloadObj)}"
        it.write(bodyContents.toByteArray(charset("utf8")))
      }

      conn.responseCode
      conn.disconnect()
    } catch (e: MalformedURLException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun createCommitAttachment(build: SRunningBuild): JsonObject? {
    val ignoreCommitMessage = build.parametersProvider.get("system.SLACK_IGNORE_COMMIT_MESSAGE")
    if (ignoreCommitMessage.equals("true", ignoreCase = true) || ignoreCommitMessage == "1") return null

    val changes = build.getChanges(SelectPrevBuildPolicy.SINCE_LAST_SUCCESSFULLY_FINISHED_BUILD, true)
    val commitMessage = StringBuilder()

    /*
     * If this field starts to feel too long in slack, we should only use the first item in the array, which would be the latest change
     */
    changes.indices.forEach { i ->
      commitMessage.append("‣ ${changes[i].description}")
      if (i < changes.size - 1) commitMessage.append("\n")
    }

    if (changes.size < 1) return null

    val commitMessageString = commitMessage.toString()
    val attachment = JsonObject().apply {
      addProperty("title", "Commit Messages")
      addProperty("fallback", commitMessageString)
      addProperty("text", commitMessageString)
      addProperty("color", "#2FA8B9")
    }

    build.branch?.let { branch ->
      attachment.addProperty("footer", "Built from ${branch.displayName}")

      build.finishDate?.let { finishDate ->
        attachment.addProperty("ts", finishDate.time / 1000)
      }
    }

    return attachment
  }
}
