package com.vungn.attendancedemo.util.helper.graph

import com.microsoft.graph.models.User
import com.microsoft.graph.requests.GraphServiceClient
import com.vungn.attendancedemo.util.helper.auth.AuthenticationHelper
import okhttp3.Request
import java.util.concurrent.CompletableFuture

class GraphHelper constructor(authProvider: AuthenticationHelper) {
    private var client: GraphServiceClient<Request>

    init {
        client = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient()
    }

    fun getUser(): CompletableFuture<User> = client.me().buildRequest().async
}