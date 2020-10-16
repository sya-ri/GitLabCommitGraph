package com.gitlab.grcc.commit.graph.http

import io.ktor.http.*

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
        val String.slashTo2F
            get() = replace("/", "%2F")
    }
}