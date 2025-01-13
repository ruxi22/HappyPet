package com.example.happypet

import retrofit2.Call
import retrofit2.http.GET

interface CatFactsApiService {
    @GET("fact") // Endpoint for a single random fact
    fun getRandomCatFact(): Call<CatFactResponse>
}
