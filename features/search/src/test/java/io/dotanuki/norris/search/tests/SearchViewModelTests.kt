package io.dotanuki.norris.search.tests

import app.cash.turbine.test
import io.dotanuki.coroutines.testutils.CoroutinesTestHelper
import io.dotanuki.norris.domain.FetchCategories
import io.dotanuki.norris.domain.model.ChuckNorrisFact
import io.dotanuki.norris.domain.model.RelatedCategory
import io.dotanuki.norris.domain.services.CategoriesCacheService
import io.dotanuki.norris.domain.services.RemoteFactsService
import io.dotanuki.norris.domain.services.SearchesHistoryService
import io.dotanuki.norris.search.SearchInteraction
import io.dotanuki.norris.search.SearchScreenState
import io.dotanuki.norris.search.SearchScreenState.Recommendations
import io.dotanuki.norris.search.SearchScreenState.SearchHistory
import io.dotanuki.norris.search.SearchScreenState.SearchQuery
import io.dotanuki.norris.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

class SearchViewModelTests {

    @get:Rule val helper = CoroutinesTestHelper()

    private lateinit var viewModel: SearchViewModel

    object FakeSearchesHistoryService : SearchesHistoryService {
        override suspend fun lastSearches(): List<String> = listOf("Code")

        override suspend fun registerNewSearch(term: String) = Unit
    }

    object FakeCategoriesCacheService : CategoriesCacheService {
        override fun save(categories: List<RelatedCategory.Available>) = Unit

        override fun cached(): List<RelatedCategory.Available>? = listOf(
            RelatedCategory.Available("dev"),
            RelatedCategory.Available("humor")
        )
    }

    object FakeRemoteFactsService : RemoteFactsService {
        override suspend fun availableCategories(): List<RelatedCategory.Available> =
            listOf(
                RelatedCategory.Available("dev"),
                RelatedCategory.Available("humor"),
                RelatedCategory.Available("soccer")
            )

        override suspend fun fetchFacts(searchTerm: String): List<ChuckNorrisFact> = emptyList()
    }

    @Before fun `before each test`() {
        val fetchCategories = FetchCategories(FakeCategoriesCacheService, FakeRemoteFactsService)
        viewModel = SearchViewModel(FakeSearchesHistoryService, fetchCategories)
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test fun `should display suggestions`() {
        runBlocking {
            viewModel.run {
                bind().test {

                    val recommended = listOf("dev", "humor")
                    val previousSearch = listOf("Code")

                    val initial = SearchScreenState.INITIAL
                    val firstFetch = initial.copy(recommendations = Recommendations.Loading)
                    val recomendationsLoaded = firstFetch.copy(recommendations = Recommendations.Success(recommended))
                    val secondFetch = recomendationsLoaded.copy(searchHistory = SearchHistory.Loading)
                    val historyLoaded = secondFetch.copy(searchHistory = SearchHistory.Success(previousSearch))

                    val expectedStates = listOf(initial, firstFetch, recomendationsLoaded, secondFetch, historyLoaded)

                    handle(SearchInteraction.OpenedScreen)

                    val collectedStates = expectedStates.map { expectItem() }
                    assertThat(collectedStates).isEqualTo(expectedStates)
                }
            }
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test fun `should validate incoming query`() {
        runBlocking {
            viewModel.run {
                bind().test {
                    val initial = SearchScreenState.INITIAL
                    val queryValidated = initial.copy(searchQuery = SearchQuery.VALID)
                    val expectedStates = listOf(initial, queryValidated)

                    handle(SearchInteraction.QueryFieldChanged("Norris"))

                    val collectedStates = expectedStates.map { expectItem() }
                    assertThat(collectedStates).isEqualTo(expectedStates)
                }
            }
        }
    }
}
