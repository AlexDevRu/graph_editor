package com.example.mobilepaint.di

import android.app.Application
import com.example.mobilepaint.SharedPrefsUtils
import com.example.mobilepaint.drawing_view.DrawingUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        app: Application,
        gso: GoogleSignInOptions
    ): GoogleSignInClient {
        return GoogleSignIn.getClient(app, gso)
    }

    @Provides
    @Singleton
    fun provideSharedPrefsUtils(
        app: Application
    ): SharedPrefsUtils {
        return SharedPrefsUtils(app)
    }

    @Provides
    @Singleton
    fun provideDrawingUtils(
        app: Application
    ): DrawingUtils {
        return DrawingUtils(app)
    }
}