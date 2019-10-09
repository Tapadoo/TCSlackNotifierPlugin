package com.tapadoo.slacknotifier.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SlackFieldTest {

  @Test
  fun testToJson() {
    val actual = SlackField("Build", "1.0.0", true).toJson().toString()
    val expected = """{"title":"Build","value":"1.0.0","short":true}"""

    assertEquals(expected, actual)
  }
}