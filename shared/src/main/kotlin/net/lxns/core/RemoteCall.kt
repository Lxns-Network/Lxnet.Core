package net.lxns.core

import kotlinx.serialization.Serializable

const val RPC_CALL_CHANNEL_IDENTIFIER = "lxnet:server_call"
const val RPC_RESPONSE_IDENTIFIER = "lxnet:server_response"

interface RemoteCall<R: RemoteResponse> {
    var id: Int
}

interface RemoteResponse {
    val id: Int
}

@Serializable
object NoResponse : RemoteResponse {
    override val id: Int = 0
}