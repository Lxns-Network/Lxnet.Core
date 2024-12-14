package net.lxns.core.rpc

fun interface ResponseHandler<R> {
    fun onResponse(r: R)
}