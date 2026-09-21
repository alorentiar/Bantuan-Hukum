package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.LegalRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bantuan Hukumku", appName)
    }

    @Test
    fun `test legal repository import and FTS search`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = LegalRepository(context)

        // Test database initialization from assets JSON
        val imported = repository.initializeDatabase()
        assertTrue("Database should import successfully", imported)

        // Test FTS search for constitutional articles
        val searchResults = repository.searchLaws("Pasal 27")
        assertTrue("Search for 'Pasal 27' should return matching articles", searchResults.isNotEmpty())

        val firstMatch = searchResults.first()
        assertNotNull(firstMatch.nomorPasal)
        assertTrue(firstMatch.nomorPasal.contains("Pasal 27"))
    }
}
