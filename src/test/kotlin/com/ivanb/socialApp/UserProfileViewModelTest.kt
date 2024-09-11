package com.ivanb.socialApp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val fakeUserRepo: UserRepository =
        object : UserRepository {
            private val profileMap =
                mutableMapOf(
                    "1" to UserProfile("1", "Ivan", 23),
                    "2" to UserProfile("2", "Steve", 25),
                    "3" to UserProfile("3", "John", 13),
                    "4" to UserProfile("4", "Alice", 40),
                )

            override suspend fun fetchProfile(userId: String): UserProfile? {
                delay(1000)
                return profileMap[userId]
            }

            override suspend fun updateProfile(userProfile: UserProfile): Boolean {
                delay(500)
                if (userProfile.id in profileMap) {
                    profileMap[userProfile.id] = userProfile
                    return true
                }
                return false
            }
        }
    private val viewModel =
        UserProfileViewModel(
            fakeUserRepo,
            testScope,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load user profile should update user profile and loading status`() =
        testScope.runTest {
            viewModel.loadProfile("1")
            runCurrent() // runs all pending tasks in the coroutine dispatcher
            assertTrue(viewModel.loading.value)
            assertNull(viewModel.profile.value)

            advanceTimeBy(1000) // move the internal clock of the dispatcher
            runCurrent() // the coroutine is finished

            assertFalse(viewModel.loading.value) // finished loading
            assertEquals(viewModel.profile.value?.name, "Ivan")
            assertEquals(viewModel.profile.value?.age, 23)
        }

    @Test
    fun `update user profile should modify user profile and loading state`() =
        testScope.runTest {
            viewModel.loadProfile("1")
            advanceTimeBy(1000) // move the internal clock of the dispatcher
            runCurrent() // the coroutine is finished

            viewModel.updateUserProfile("Older Ivan", 24)
            runCurrent()
            assertTrue(viewModel.loading.value)
            advanceTimeBy(1000)

            assertFalse(viewModel.loading.value)
            assertEquals(viewModel.profile.value?.name, "Older Ivan")
            assertEquals(viewModel.profile.value?.age, 24)
        }

    @Test
    fun `load user profile and update user profile`() =
        testScope.runTest {
            viewModel.loadProfile("1")
            advanceUntilIdle()
            viewModel.updateUserProfile("Older Ivan", 24)
            advanceUntilIdle()
            runCurrent()
            assertFalse(viewModel.loading.value)
            assertEquals(viewModel.profile.value?.name, "Older Ivan")
            assertEquals(viewModel.profile.value?.age, 24)
        }
}
