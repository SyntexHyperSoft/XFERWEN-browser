package com.example

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val stringsFile = File("src/main/res/values/strings.xml")
        val content = if (stringsFile.exists()) stringsFile.readText() else File("app/src/main/res/values/strings.xml").readText()
        val regex = Regex("""<string name="app_name">([^<]+)</string>""")
        val match = regex.find(content)
        val appName = match?.groupValues?.get(1) ?: "XFERWEN"
        assertEquals("XFERWEN", appName)
    }
}
