package net.lxns.core

import kotlinx.serialization.Serializable
import net.lxns.core.record.PlayerAchievementRecord

object Achievements {
    val allAchievements by lazy {
        listOf(
            BedWars.BREAK_BED_IN_LOW_SITUATION,
            BedWars.GENOSIDE,
            BlockHunt.MURDER,
            Parkour.PARKOUR_PLAYER,
            Parkour.PARKOUR_MASTER,
            Parkour.PARKOUR_MASTER_PLUS,
            BuildBattle.GUESS_ALL_RIGHT,
            BuildBattle.ABSTRACT_MASTER
        ).map { it.id to it }.toMap()
    }

    @Serializable
    data class Achievement(
        val extra: Boolean,
        val id: String,
        val name: String,
        val description: String
    ) {
        fun toAchievementRecord() = PlayerAchievementRecord(
            id, System.currentTimeMillis()
        )
    }

    object BedWars {
        val BREAK_BED_IN_LOW_SITUATION = Achievement(
            false,
            "bedwars_break_bed_in_low_situation",
            "以牙还牙",
            "在床被毁后以同样的方式报复对方。"
        )
        val GENOSIDE = Achievement(
            true,
            "bedwars_genoside",
            "血洗楼兰",
            "以一人之力灭队"
        )
    }

    object BlockHunt {
        val MURDER = Achievement(
            false,
            "blockhunt_murder",
            "雨露均沾",
            "在一局躲猫猫游戏中扮演方块杀死一位猎人，并作为猎人杀死一个方块"
        )
    }

    object Parkour {
        val PARKOUR_MASTER = Achievement(
            false,
            "parkour_master",
            "跑酷高手",
            "在三十分钟内完成跑酷游戏"
        )
        val PARKOUR_PLAYER = Achievement(
            false,
            "parkour_player",
            "登顶",
            "完成跑酷游戏！",
        )
        val PARKOUR_MASTER_PLUS = Achievement(
            true,
            "parkour_master_plus",
            "跑酷大神",
            "在二十分钟内完成跑酷游戏"
        )
    }

    object BuildBattle {
        val GUESS_ALL_RIGHT = Achievement(
            false,
            "guess_all_right",
            "抽象杀手",
            "在一把建筑猜猜乐游戏中猜对所有建筑"
        )
        val ABSTRACT_MASTER = Achievement(
            true,
            "abstract_master",
            "抽象派大师",
            "不仅猜对了所有建筑，而且所有人都猜对了自己的建筑，连胜三把"
        )
    }
}