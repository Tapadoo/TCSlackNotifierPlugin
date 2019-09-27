package com.tapadoo.slacknotifier.data

import com.google.gson.JsonObject

data class SlackField(
    val title: String,
    val value: String,
    val short: Boolean
) {
  fun toJson() = JsonObject().apply {
    addProperty("title", title)
    addProperty("value", value)
    addProperty("short", short)
  }
}