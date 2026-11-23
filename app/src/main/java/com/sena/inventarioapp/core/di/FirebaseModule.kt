package com.sena.inventarioapp.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private fun ensureFirebaseInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId(context.packageName)
                .setProjectId("inventarioapp-local")
                .setApiKey("AIzaSyLocalDummyApiKeyForInitialization000")
                .build()
            FirebaseApp.initializeApp(context, options)
        }
    }

    @Provides @Singleton
    fun auth(@ApplicationContext context: Context): FirebaseAuth {
        ensureFirebaseInitialized(context)
        return FirebaseAuth.getInstance()
    }

    @Provides @Singleton
    fun firestore(@ApplicationContext context: Context): FirebaseFirestore {
        ensureFirebaseInitialized(context)
        return FirebaseFirestore.getInstance()
    }
}
