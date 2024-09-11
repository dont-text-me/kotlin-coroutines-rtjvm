package com.ivanb.socialApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// M V C / M V VM

class UserProfileViewModel(
    private val repo: UserRepository,
    private val coroutineScope: CoroutineScope,
) {
    // the state of the UI
    // the current user profile
    // loading state

    private val _profile = MutableStateFlow<UserProfile?>(null) // a thread-safe variable that emits all changes to readers
    val profile = _profile.asStateFlow() // same as the internal field but read-only

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading = _loading.asStateFlow()

    suspend fun currentLoading() = loading.value // return the current value of the flow

    fun loadProfile(userId: String) =
        coroutineScope.launch {
            _loading.value = true // assignment is thread safe
            try {
                _profile.value = repo.fetchProfile(userId)
            } finally {
                _loading.value = false
            }
        }

    fun updateUserProfile(
        name: String,
        age: Int,
    ) = coroutineScope.launch {
        _profile.value?.let {
            _loading.value = true
            try {
                val updatedProfile = it.copy(name = name, age = age)
                if (repo.updateProfile(updatedProfile)) {
                    _profile.value = updatedProfile
                }
            } finally {
                _loading.value = false
            }
        }
    }
}
