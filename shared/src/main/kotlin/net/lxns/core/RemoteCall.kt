package net.lxns.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val RPC_CHANNEL_IDENTIFIER = "lxnet:server"

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