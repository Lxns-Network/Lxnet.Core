package net.lxns.core.gui

import net.lxns.core.Achievements
import net.lxns.core.LxnetCore
import net.lxns.core.bukkitColor
import net.lxns.core.itemTemplateAchievement
import net.lxns.core.withMeta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.ipvp.canvas.mask.RecipeMask
import org.ipvp.canvas.type.ChestMenu

object AchievementMenu {
    private val menu: ChestMenu
    init {
        menu = ChestMenu.builder(5)
            .title("成就".bukkitColor())
            .build()
        var mask = RecipeMask.builder(menu)
            .pattern("zzzzzzzzz")
            .pattern("zAzBzCzDz")
            .pattern("zzzzIzzzz")
            .pattern("zEzFzGzHz")
            .pattern("zzzzzzzzz")
            .item('A', itemTemplateAchievement(Material.RED_BED, Achievements.BedWars.BREAK_BED_IN_LOW_SITUATION))
            .item('B', itemTemplateAchievement(Material.JACK_O_LANTERN, Achievements.BlockHunt.MURDER))
            .item('C', itemTemplateAchievement(Material.LEATHER_BOOTS, Achievements.Parkour.PARKOUR_PLAYER))
            .item('D', itemTemplateAchievement(Material.PAINTING, Achievements.BuildBattle.GUESS_ALL_RIGHT))
            .item('E', itemTemplateAchievement(Material.GOLDEN_SWORD, Achievements.BedWars.GENOSIDE))
            .item('F', itemTemplateAchievement(Material.GOLDEN_BOOTS, Achievements.Parkour.PARKOUR_MASTER))
            .item('G', itemTemplateAchievement(Material.DIAMOND_BOOTS, Achievements.Parkour.PARKOUR_MASTER_PLUS))
            .item('H', itemTemplateAchievement(Material.SPYGLASS, Achievements.BuildBattle.ABSTRACT_MASTER))
            .item('I', ItemStack(Material.PAPER).withMeta {
                setDisplayName("&e成就！".bukkitColor())
                lore = mutableListOf(
                    "&f成就没有什么用，不收集也可以哦。".bukkitColor(),
                    "",
                    "&8成就更新可能有延迟。".bukkitColor()
                )
            })
            .build()
        mask.apply(menu)
    }

    fun show(player: Player){
        menu.open(player)
    }
}