package com.starry.myne


import com.google.common.truth.Truth.assertThat
import com.starry.myne.helpers.Paginator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

data class PaginatorTestResult(
    var error: Throwable? = null,
    var result: List<String>? = null,
    var nextPage: Int = 1
)

@ExperimentalCoroutinesApi
class PaginatorTest {

    private lateinit var paginator: Paginator<Int, List<String>>
    private lateinit var paginatorWithError: Paginator<Int, List<String>>

    private lateinit var isMakingRequestField: Field
    private lateinit var currentPage: Field

    private lateinit var testResult: PaginatorTestResult

    @Before
    fun setup() {
        testResult = PaginatorTestResult()
        paginator = Paginator(
            initialPage = 1,
            onLoadUpdated = {},
            onRequest = { Result.success(listOf("book1", "book2", "book3")) },
            getNextPage = { 2 },
            onError = { testResult = testResult.copy(error = it) },
            onSuccess = { books, nextPage ->
                testResult = testResult.copy(result = books, nextPage = nextPage)
            }
        )
        paginatorWithError = Paginator(
            initialPage = 1,
            onLoadUpdated = {},
            onRequest = { Result.failure(NullPointerException("meow")) },
            getNextPage = { it.size + 1 },
            onError = { testResult = testResult.copy(error = it) },
            onSuccess = { books, nextPage ->
                testResult = testResult.copy(result = books, nextPage = nextPage)
            }
        )

        // Access private fields.
        isMakingRequestField =
            Paginator::class.java.getDeclaredField("isMakingRequest").apply { isAccessible = true }
        currentPage =
            Paginator::class.java.getDeclaredField("currentPage").apply { isAccessible = true }
    }

    @Test
    fun `test paginator with valid result`() = runTest {
        // test paginator internal stuffs.
        paginator.loadNextItems()
        assertThat(getIsMakingRequest(false)).isFalse()
        assertThat(getCurrentPage(false)).isEqualTo(2)
        // check test result
        assertThat(testResult.result).isNotNull()
        assertThat(testResult.result).hasSize(3)
        assertThat(testResult.result).contains("book2")
        // check for error
        assertThat(testResult.error).isNull()
        // check next page
        assertThat(testResult.nextPage).isEqualTo(2)
    }

    @Test
    fun `test paginator with error result`() = runTest {
        // test paginator internal stuffs.
        paginatorWithError.loadNextItems()
        assertThat(getIsMakingRequest(true)).isFalse()
        assertThat(getCurrentPage(true)).isEqualTo(1)
        // check test result
        assertThat(testResult.result).isNull()
        // check for error
        assertThat(testResult.error).isNotNull()
        assertThat(testResult.error).isInstanceOf(NullPointerException::class.java)
        assertThat(testResult.error).hasMessageThat().contains("meow")
        // check next page
        assertThat(testResult.nextPage).isEqualTo(1)
    }


    @Test
    fun `test paginator reset`() {
        paginator.reset()
        assertThat(getCurrentPage(false)).isEqualTo(1)
    }

    private fun getIsMakingRequest(errorPaginator: Boolean): Boolean {
        val paginatorObj = if (errorPaginator) paginatorWithError else paginator
        return isMakingRequestField.get(paginatorObj) as Boolean
    }

    private fun getCurrentPage(errorPaginator: Boolean): Int {
        val paginatorObj = if (errorPaginator) paginatorWithError else paginator
        return currentPage.get(paginatorObj) as Int
    }
}