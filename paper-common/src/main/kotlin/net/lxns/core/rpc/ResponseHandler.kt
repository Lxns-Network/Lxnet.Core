package net.lxns.core.rpc

import net.lxns.core.RemoteResponse

fun interface ResponseHandler<R> {
    fun onResponse(r: R)
}