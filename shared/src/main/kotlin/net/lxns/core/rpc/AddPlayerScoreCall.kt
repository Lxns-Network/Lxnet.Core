package net.lxns.core.rpc

import kotlinx.serialization.Serializable
import net.lxns.core.RemoteCall
import net.lxns.core.record.PlayerScoreRecord

@Serializable
class AddPlayerScoreCall(
    val record: PlayerScoreRecord,
    override var id: Int = 0
) : RemoteCall<Nothing>