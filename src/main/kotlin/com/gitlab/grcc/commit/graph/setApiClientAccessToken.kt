package com.gitlab.grcc.commit.graph

import com.gitlab.grcc.commit.graph.http.GitLabApiClient
import javax.swing.JFrame
import javax.swing.JOptionPane

fun GitLabApiClient.setAccessToken(frame: JFrame, message: String) {
    accessToken = inputAccessToken(frame, message)
}

private fun inputAccessToken(frame: JFrame, message: String): String {
    while (true) {
        val accessToken = JOptionPane.showInputDialog(frame, message, "API", JOptionPane.PLAIN_MESSAGE)
        if (accessToken.isNotBlank()) return accessToken
    }
}