import data.storage.PropertiesFileStore
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PropertiesFileStoreTest {
    @Test
    fun setGetRemoveRoundTrip() {
        val path = "updater-test-${this.hashCode()}.properties".toPath()
        val store = PropertiesFileStore(path)
        store.set("loginInfo", """{"userId":"1","authResult":"1"}""")
        store.set("deviceName", "Redmi Note 10")
        assertEquals("""{"userId":"1","authResult":"1"}""", store.get("loginInfo"))
        store.remove("deviceName")
        assertNull(store.get("deviceName"))
        FileSystem.SYSTEM.delete(path)
    }

    @Test
    fun valuesWithNewlinesDoNotCorruptOtherKeys() {
        val path = "updater-test-nl-${this.hashCode()}.properties".toPath()
        // A password whose second line spoofs another key must not overwrite it.
        val evil = "pw\nloginInfo=hijacked"
        PropertiesFileStore(path).apply {
            set("password", evil)
            set("loginInfo", "real")
        }
        // Reload from disk to exercise the escape/unescape round-trip.
        val reloaded = PropertiesFileStore(path)
        assertEquals(evil, reloaded.get("password"))
        assertEquals("real", reloaded.get("loginInfo"))
        FileSystem.SYSTEM.delete(path)
    }
}
