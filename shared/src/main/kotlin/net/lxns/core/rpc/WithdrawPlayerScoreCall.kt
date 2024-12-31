package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.lxns.core.RemoteCall
import net.lxns.core.RemoteResponse
import net.lxns.core.record.PlayerScoreRecord

@Serializable
class WithdrawPlayerScoreCall(
    val record: PlayerScoreRecord,
    override var id: Int = -1
): RemoteCall<WithdrawPlayerScoreCall.Response> {
    @Serializable
    class Response(
        val success: Boolean,
        val currentMoney: Int,
        override val id: Int
    ) : RemoteResponse

}