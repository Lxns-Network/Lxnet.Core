package net.lxns.core.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lxns.core.NoResponse
import net.lxns.core.RemoteCall
import net.lxns.core.RemoteResponse
import net.lxns.core.record.PlayerScoreRecord

@Serializable
@SerialName("AddPlayerScoreCall")
class AddPlayerScoreCall(
    val record: PlayerScoreRecord,
    override var id: Int = 0
) : RemoteCall<NoResponse>