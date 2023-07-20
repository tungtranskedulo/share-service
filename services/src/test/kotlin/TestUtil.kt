import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@Tag("small")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestUtil {
    @Test
    fun `test`() {
        println("Hello, world!")
    }
}
