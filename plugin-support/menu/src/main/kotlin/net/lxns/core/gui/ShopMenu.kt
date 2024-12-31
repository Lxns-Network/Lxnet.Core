package net.lxns.core.gui

import net.lxns.core.Fireworks
import net.lxns.core.LobbyMenuPlugin
import net.lxns.core.LotteryManager
import net.lxns.core.LxnetCore
import net.lxns.core.ScoreReason
import net.lxns.core.bukkitColor
import net.lxns.core.data.Price
import net.lxns.core.newItem
import net.lxns.core.record.PlayerScoreRecord
import net.lxns.core.rpc.FetchPlayerScoreCall
import net.lxns.core.rpc.WithdrawPlayerScoreCall
import net.lxns.core.withMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.ipvp.canvas.Menu
import org.ipvp.canvas.mask.RecipeMask
import org.ipvp.canvas.slot.SlotSettings
import org.ipvp.canvas.template.ItemStackTemplate
import org.ipvp.canvas.type.ChestMenu
import java.util.UUID

class ShopMenu(
    val price: Price,
    val lotteryManager: LotteryManager
) {
    companion object {
        private const val STATE_PURCHASING = -1
        private const val STATE_LOADING = -2
    }
    private val menu: Menu
    private val playerSessionMap = mutableMapOf<UUID, UUID>()
    private val playerMoneyCache: MutableMap<UUID, Int> = mutableMapOf<UUID, Int>()

    init {
        // #########
        // #       #
        // #       #
        // #       #
        // #       #
        // #########
        menu = ChestMenu.builder(6)
            .title("硬币商店")
            .build()
        menu.setCloseHandler { p, m ->
            val session = playerSessionMap[p.uniqueId] ?: return@setCloseHandler
            playerMoneyCache.remove(session)
            playerSessionMap.remove(p.uniqueId)
        }
        Bukkit.getScheduler().runTaskTimer(LobbyMenuPlugin.plugin, { -> menu.update() }, 0L, 15L)
        val patternMask = RecipeMask.builder(menu)
            .pattern(" ABCDEFG ")
            .pattern(" HIJKLMN ")
            .pattern(" OPQRSTU ")
            .pattern(" VWXYZ   ")
            .pattern("         ")
            .pattern("  a   b  ")
        loadShopItems(patternMask)
        patternMask.build().apply(menu)
    }

    fun show(player: Player) {
        val newSession = UUID.randomUUID()
        playerSessionMap[player.uniqueId] = newSession
        // fetch money
        playerMoneyCache[newSession] = STATE_LOADING
        LxnetCore.rpcManager.requestCall(FetchPlayerScoreCall(player.uniqueId)) {
            playerMoneyCache[newSession] = it.score
        }
        menu.open(player)
    }

    private fun RecipeMask.RecipeMaskBuilder.fireworkItem(
        char: Char,
        name: String,
        lore: String,
        author: String,
        price: Int,
        fireworkItem: ItemStack,
    ) {
        item(char, shopItem(
            newItem(Material.FIREWORK_ROCKET, name, lore, "", "&8来自 $author"), price,
            { p -> p.inventory.addItem(fireworkItem.clone()) }
        ))
    }

    private fun loadSpecialItem(patternMask: RecipeMask.RecipeMaskBuilder) {
        patternMask.item('a', infoItem())
        patternMask.item(
            'b', shopItem(
                newItem(Material.PAPER, "&d彩票".bukkitColor(), "&f\"今晚零点开奖。\"".bukkitColor()),
                price.priceLottery,
                lotteryManager::purchaseLottery,
                lotteryManager::canPurchaseLottery
            )
        )
    }

    private fun infoItem() = ItemStackTemplate { player ->
        ItemStack(Material.GOLD_INGOT).withMeta {
            setDisplayName("&e商店".bukkitColor())
            val session = playerSessionMap[player.uniqueId] //todo: make session in scaffold
            val money = playerMoneyCache[session] ?: STATE_LOADING
            lore = listOf(
                "&f在游戏活动中赚取的硬币可以在此购买物品。",
                if (money == STATE_LOADING) {
                    "&c余额加载中..."
                } else {
                    "&f当前余额为: &6$money"
                }
            ).map { it.bukkitColor() }
        }
    }

    private inline fun shopItem(
        itemTemplate: ItemStack,
        price: Int,
        crossinline action: (Player) -> Unit,
        crossinline requirement: (Player) -> Boolean = { true }
    ): SlotSettings {
        return SlotSettings.builder()
            .itemTemplate(shopItemTemplate(itemTemplate, price, requirement))
            .clickHandler { p, click ->
                val session = playerSessionMap[p.uniqueId] ?: return@clickHandler
                val money = playerMoneyCache[session] ?: return@clickHandler
                if (money < 0 || money < price) {
                    return@clickHandler
                }
                if (!requirement(p)) return@clickHandler
                playerMoneyCache[session] = STATE_PURCHASING
                val uuid = p.uniqueId
                LxnetCore.rpcManager.requestCall(
                    WithdrawPlayerScoreCall(PlayerScoreRecord(p.uniqueId, price, ScoreReason.PURCHASE))
                ) {
                    val _p = Bukkit.getPlayer(uuid)
                    if (playerMoneyCache.containsKey(session)) {
                        playerMoneyCache[session] = it.currentMoney
                    }
                    _p ?: return@requestCall
                    if (it.success) {
                        action(_p)
                    }
                    menu.update(_p )
                }
                menu.update(p)
            }.build()
    }

    private inline fun shopItemTemplate(
        itemTemplate: ItemStack,
        price: Int,
        crossinline requirement: (Player) -> Boolean
    ) = ItemStackTemplate { player ->
        itemTemplate.clone().withMeta {
            val session = playerSessionMap[player.uniqueId] ?: return@withMeta
            val money = playerMoneyCache[session] ?: STATE_LOADING
            val _lore = ArrayList(lore)
            if (!requirement(player)) {
                _lore.add("&c此商品不可再购买！")
            } else {
                when(money) {
                    STATE_PURCHASING ->_lore.add("&c正在购买中，请不要退出...")
                    STATE_LOADING -> _lore.add("&c正在加载钱包...")
                    else -> {
                        if (money < price) {
                            _lore.add("&c余额不足！")
                        } else {
                            _lore.add("&a可购买！")
                        }
                    }
                }
            }
            lore = _lore.map { it.bukkitColor() }
        }
    }
    private fun loadShopItems(patternMask: RecipeMask.RecipeMaskBuilder) {
        loadSpecialItem(patternMask)
        patternMask.fireworkItem(
            'A',
            "&c激情似火", "&f在空中炸裂出火焰的烟花。", "qyl27",
            price.priceFireworkFire, Fireworks.LIKE_FIRE
        )
        patternMask.fireworkItem(
            'B',
            "&d彩色星星烟花", "", "PaQiu_PAQ",
            price.priceFireworkColorfulStar, Fireworks.COLORFUL_STAR
        )
        patternMask.fireworkItem(
            'C',
            "&c新年烟花", "", "PaQiu_PAQ",
            price.priceFireworkNewYear, Fireworks.NEW_YEAR
        )
        patternMask.fireworkItem(
            'D',
            "&a苦力怕烟花", "", "PaQiu_PAQ",
            price.priceFireworkCreeper, Fireworks.CREEPER_FIREWORK
        )
        patternMask.fireworkItem(
            'E',
            "&c红色小烟花", "", "PaQiu_PAQ",
            price.priceFireworkSmall, Fireworks.RED_SMALL_FIREWORK
        )
        patternMask.fireworkItem(
            'F',
            "&e黄色小烟花", "", "PaQiu_PAQ",
            price.priceFireworkSmall, Fireworks.YELLOW_SMALL_FIREWORK
        )
        patternMask.fireworkItem(
            'G',
            "&a绿色小烟花", "", "PaQiu_PAQ",
            price.priceFireworkSmall, Fireworks.GREEN_SMALL_FIREWORK
        )
        patternMask.fireworkItem(
            'H',
            "&b蓝色小烟花", "", "PaQiu_PAQ",
            price.priceFireworkSmall, Fireworks.BLUE_SMALL_FIREWORK
        )
        // sparkle fireworks
        patternMask.fireworkItem(
            'I',
            "&c红白色闪烁烟花", "", "PaQiu_PAQ",
            price.priceFireworkSparkle, Fireworks.RED_WHITE_BLINK_FIREWORK
        )
        patternMask.fireworkItem(
            'J',
            "&e黄白色闪烁烟花", "", "PaQiu_PAQ",
            price.priceFireworkSparkle, Fireworks.YELLOW_WHITE_BLINK_FIREWORK
        )
        patternMask.fireworkItem(
            'K',
            "&e绿白色闪烁烟花", "", "PaQiu_PAQ",
            price.priceFireworkSparkle, Fireworks.GREEN_WHITE_BLINK_FIREWORK
        )
        patternMask.fireworkItem(
            'L',
            "&b蓝白色闪烁烟花", "", "PaQiu_PAQ",
            price.priceFireworkSparkle, Fireworks.BLUE_WHITE_BLINK_FIREWORK
        )
        patternMask.fireworkItem(
            'M',
            "&c红白色闪烁烟花", "", "PaQiu_PAQ",
            price.priceFireworkSparkle, Fireworks.RED_WHITE_BLINK_FIREWORK
        )
    }
}