package com.pcandroiddev.noteworthyapp.di

import android.content.Context
import com.pcandroiddev.noteworthyapp.api.AuthInterceptor
import com.pcandroiddev.noteworthyapp.api.NoteService
import com.pcandroiddev.noteworthyapp.api.TokenService
import com.pcandroiddev.noteworthyapp.api.UserService
import com.pcandroiddev.noteworthyapp.util.Constants.BASE_URL
import com.pcandroiddev.noteworthyapp.util.downloader.image.ImageDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun providesRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(authInterceptor).build()
    }

    @Singleton
    @Provides
    fun providesUserAPI(retrofitBuilder: Retrofit.Builder): UserService {
        return retrofitBuilder
            .build()
            .create(UserService::class.java)
    }

    @Singleton
    @Provides
    fun providesTokenAPI(retrofitBuilder: Retrofit.Builder): TokenService {
        return retrofitBuilder
            .build()
            .create(TokenService::class.java)
    }

    @Singleton
    @Provides
    fun providesNotesAPI(
        retrofitBuilder: Retrofit.Builder,
        okHttpClient: OkHttpClient
    ): NoteService {
        return retrofitBuilder
            .client(okHttpClient)
            .build()
            .create(NoteService::class.java)
    }

    @Singleton
    @Provides
    fun provideImageDownloader(@ApplicationContext context: Context): ImageDownloader {
        return ImageDownloader(context = context)
    }


}
