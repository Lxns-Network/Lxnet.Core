package net.lxns.core

const val RPC_CHANNEL_IDENTIFIER = "lxnet:server"

interface RemoteCall {
    val id: Int
}