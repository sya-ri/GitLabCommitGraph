package com.gitlab.grcc.commit.graph.http

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class GitLabApiClient(private val accessToken: String) {
    companion object {
        private val httpClient = HttpClient(OkHttp)

        private const val GITLAB_API_URL = "https://gitlab.com/api/v4"
    }

    suspend fun request(endPoint: ApiEndPoint, vararg parameters: Pair<String, String>): Pair<HttpResponse, JsonElement>? {
        val response = httpClient.request<HttpResponse> {
            method = endPoint.method
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            url(GITLAB_API_URL + endPoint.path + parameters.joinToString("&", "?") { it.first + "=" + it.second })
        }
        val contentType = response.headers["Content-Type"]
        val body = response.readText()
        return if (contentType?.equals("application/json", true) == true) {
            response to Gson().fromJson(body, JsonElement::class.java)
        } else {
            null
        }
    }

    suspend fun requestJson(endPoint: ApiEndPoint, vararg parameters: Pair<String, String>): JsonElement? {
        return request(endPoint, *parameters)?.second
    }
}