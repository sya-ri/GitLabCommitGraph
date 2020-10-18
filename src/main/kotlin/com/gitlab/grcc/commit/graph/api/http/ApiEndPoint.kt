package com.gitlab.grcc.commit.graph.api.http

import io.ktor.http.HttpMethod

sealed class ApiEndPoint(val method: HttpMethod, val path: String) {
    /**
     * https://docs.gitlab.com/ee/api/groups.html#list-a-groups-projects
     * ```
     * GET /groups/:id/projects
     * ```
     */
    class GetProject(id: String): ApiEndPoint(HttpMethod.Get, "/groups/$id/projects")

    /**
     * https://docs.gitlab.com/ee/api/commits.html#list-repository-commits
     * ```
     * GET /projects/:id/repository/commits
     * ```
     */
    class GetCommit(id: String): ApiEndPoint(HttpMethod.Get, "/projects/$id/repository/commits")

    companion object {
        /**
         * https://docs.gitlab.com/ee/api/README.html#namespaced-path-encoding
         */
        val String.slashTo2F
            get() = replace("/", "%2F")
    }
}