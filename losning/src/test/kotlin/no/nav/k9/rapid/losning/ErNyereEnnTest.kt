package no.nav.k9.rapid.losning

import de.huxhorn.sulky.ulid.ULID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

internal class ErNyereEnnTest {

    private companion object {
        val ulid = ULID()
    }

    @Test
    fun `Tester id mot eldre id`() {
        val a = ulid.nextULID().also { sleep(1) }
        val b = ulid.nextULID()
        assertTrue(b.erNyereEnn(a))
    }

    @Test
    fun `Tester id mot nyere id`() {
        val a = ulid.nextULID().also { sleep(1) }
        val b = ulid.nextULID()
        assertFalse(a.erNyereEnn(b))
    }

    @Test
    fun `Tester mot samme id`() {
        val a = ulid.nextULID()
        assertFalse(a.erNyereEnn(a))
    }
}