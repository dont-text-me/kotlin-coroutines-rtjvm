package com.ivanb.socialApp

interface UserRepository {
    suspend fun fetchProfile(userId: String): UserProfile?

    suspend fun updateProfile(userProfile: UserProfile): Boolean
}
