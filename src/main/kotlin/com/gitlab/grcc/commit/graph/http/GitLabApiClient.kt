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
    sealed class RequestResult {
        data class Success(val response: HttpResponse, val json: JsonElement): RequestResult()
        sealed class Failure: RequestResult() {
            object NotContent: Failure()
            object BadRequest: Failure()
            object Unauthorized: Failure()
            object Forbidden: Failure()
            object NotFound: Failure()
            object MethodNotAllowed: Failure()
            object Conflict: Failure()
            object RequestDenied: Failure()
            object Unprocessable: Failure()
            object ServerError: Failure()
            object UnHandle: Failure()
        }
    }

    companion object {
        private val httpClient = HttpClient(OkHttp)

        private const val GITLAB_API_URL = "https://gitlab.com/api/v4"
    }

    lateinit var accessToken: String
    lateinit var onFailure: (RequestResult.Failure) -> Unit

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
                    RequestResult.Failure.NotContent
                }
            }
            // 400: Bad Request
            400 -> {
                RequestResult.Failure.BadRequest
            }
            // 401: Unauthorized
            401 -> {
                RequestResult.Failure.Unauthorized
            }
            // 403: Forbidden
            403 -> {
                RequestResult.Failure.Forbidden
            }
            // 404: Not Found
            404 -> {
                RequestResult.Failure.NotFound
            }
            // 405: Method Not Allowed
            405 -> {
                RequestResult.Failure.MethodNotAllowed
            }
            // 409: Conflict
            409 -> {
                RequestResult.Failure.Conflict
            }
            // 412: Request Denied
            412 -> {
                RequestResult.Failure.RequestDenied
            }
            // 422: Unprocessable
            422 -> {
                RequestResult.Failure.Unprocessable
            }
            // 500: ServerError
            500 -> {
                RequestResult.Failure.ServerError
            }
            // Other
            else -> {
                RequestResult.Failure.UnHandle
            }
        }
    }
}