package com.gitlab.grcc.commit.graph.http

import io.ktor.http.*

sealed class ApiEndPoint(val method: HttpMethod, val path: String) {
    /**
     * https://docs.gitlab.com/ee/api/groups.html#list-a-groups-subgroups
     * ```
     * GET /groups/:id/subgroups
     * ```
     */
    class GetSubGroup(id: String): ApiEndPoint(HttpMethod.Get, "/groups/$id/subgroups")

    /**
     * https://docs.gitlab.com/ee/api/groups.html#list-a-groups-projects
     * ```
     * GET /groups/:id/projects
     * ```
     */
    class GetProject(id: String): ApiEndPoint(HttpMethod.Get, "/groups/$id/projects")

    companion object {
        val String.slashTo2F
            get() = replace("/", "%2F")
    }
}