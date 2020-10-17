package com.gitlab.grcc.commit.graph.http

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpHeaders

class GitLabApiClient {
    class RequestFailureException(val result: RequestResult): Exception()

    sealed class RequestResult {
        data class Success(val response: HttpResponse, val json: JsonElement): RequestResult()
        object NotContent: RequestResult()
        object BadRequest: RequestResult()
        object Unauthorized: RequestResult()
        object Forbidden: RequestResult()
        object NotFound: RequestResult()
        object MethodNotAllowed: RequestResult()
        object Conflict: RequestResult()
        object RequestDenied: RequestResult()
        object Unprocessable: RequestResult()
        object ServerError: RequestResult()
        object UnHandle: RequestResult()
    }

    companion object {
        private val httpClient = HttpClient(OkHttp)

        private const val GITLAB_API_URL = "https://gitlab.com/api/v4"
    }

    lateinit var accessToken: String

    suspend fun request(endPoint: ApiEndPoint, vararg parameters: Pair<String, String>): RequestResult {
        val response = httpClient.request<HttpResponse> {
            method = endPoint.method
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            url(GITLAB_API_URL + endPoint.path + parameters.joinToString("&", "?") { it.first + "=" + it.second })
        }
        // https://docs.gitlab.com/ee/api/README.html#status-codes
        return when(response.status.value) {
            // 200: OK
            // 204: No Content
            // 201: Created
            // 304: Not Modified
            200, 204, 201, 304 -> {
                val contentType = response.headers["Content-Type"]
                val body = response.readText()
                if (contentType?.equals("application/json", true) == true) {
                    RequestResult.Success(response, Gson().fromJson(body, JsonElement::class.java))
                } else {
                    RequestResult.NotContent
                }
            }
            // 400: Bad Request
            400 -> {
                RequestResult.BadRequest
            }
            // 401: Unauthorized
            401 -> {
                RequestResult.Unauthorized
            }
            // 403: Forbidden
            403 -> {
                RequestResult.Forbidden
            }
            // 404: Not Found
            404 -> {
                RequestResult.NotFound
            }
            // 405: Method Not Allowed
            405 -> {
                RequestResult.MethodNotAllowed
            }
            // 409: Conflict
            409 -> {
                RequestResult.Conflict
            }
            // 412: Request Denied
            412 -> {
                RequestResult.RequestDenied
            }
            // 422: Unprocessable
            422 -> {
                RequestResult.Unprocessable
            }
            // 500: ServerError
            500 -> {
                RequestResult.ServerError
            }
            // Other
            else -> {
                RequestResult.UnHandle
            }
        }
    }
}