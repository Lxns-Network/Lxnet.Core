package net.lxns.core

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.FireworkEffect.Type
import org.bukkit.Material
import org.bukkit.inventory.meta.FireworkMeta

object Fireworks {
    val LIKE_FIRE = newItem(
        Material.FIREWORK_ROCKET,
        "&c激情似火"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(true)
                .with(Type.BURST)
                .flicker(false)
                .withColor(Color.fromRGB(11743532), Color.fromRGB(15435844))
                .withFade(Color.fromRGB(15435844))
                .build())

            addEffect(FireworkEffect.builder()
                .trail(true)
                .flicker(true)
                .with(Type.BURST)
                .withColor(Color.fromRGB(16711680), Color.fromRGB(255))
                .withFade(Color.fromRGB(65280))
                .build());
            addEffect(FireworkEffect.builder()
                .trail(false)
                .flicker(false)
                .with(Type.BURST)
                .withColor(Color.fromRGB(16776960), Color.fromRGB(255))
                .withFade(Color.fromRGB(16711680))
                .build());
        }
    }

    val COLORFUL_STAR = newItem(
        Material.FIREWORK_ROCKET,
        "&d彩色星星烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 2
            addEffect(FireworkEffect.builder()
                .trail(true)
                .flicker(true)
                .with(Type.STAR) // 烟花形状为 "star"
                .withColor(
                    Color.fromRGB(11743532), Color.fromRGB(2437522), Color.fromRGB(8073150),
                    Color.fromRGB(2651799), Color.fromRGB(14188952), Color.fromRGB(4312372),
                    Color.fromRGB(14602026), Color.fromRGB(12801229), Color.fromRGB(15435844),
                    Color.fromRGB(15790320)
                )
                .withFade(
                    Color.fromRGB(11743532), Color.fromRGB(2437522), Color.fromRGB(8073150),
                    Color.fromRGB(2651799), Color.fromRGB(14188952), Color.fromRGB(4312372),
                    Color.fromRGB(14602026), Color.fromRGB(12801229), Color.fromRGB(15435844),
                    Color.fromRGB(15790320)
                ).build())
        }
    }

    val NEW_YEAR = newItem(
        Material.FIREWORK_ROCKET,
        "&c新年烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 2
            addEffect(FireworkEffect.builder()
                .trail(true)
                .flicker(true)
                .with(Type.BALL_LARGE) // 烟花形状为 "large_ball"
                .withColor(Color.fromRGB(11743532), Color.fromRGB(14602026), Color.fromRGB(15790320))
                .build());
        }
    }

    val CREEPER_FIREWORK = newItem(
        Material.FIREWORK_ROCKET,
        "&a苦力怕烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 2
            addEffect(FireworkEffect.builder()
                .trail(false)
                .flicker(false)
                .with(Type.CREEPER)
                .withColor(Color.fromRGB(11250603))
                .build());

            addEffect(FireworkEffect.builder()
                .trail(false)
                .flicker(false)
                .with(Type.BALL_LARGE)
                .withColor(Color.fromRGB(4312372))
                .build());

        }
    }


    val RED_SMALL_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c红色小烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(false)
                .withColor(Color.fromRGB(11743532))
                .withFade(Color.fromRGB(1973019))
                .build());
        }
    }
    val YELLOW_SMALL_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&e黄色小烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(false)
                .withColor(Color.fromRGB(14602026))
                .withFade(Color.fromRGB(1973019))
                .build());
        }
    }
    val GREEN_SMALL_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c绿色小烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(false)
                .withColor(Color.fromRGB(4312372))
                .withFade(Color.fromRGB(1973019))
                .build());
        }
    }
    val BLUE_SMALL_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c蓝色小烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(false)
                .withColor(Color.fromRGB(2437522))
                .withFade(Color.fromRGB(1973019))
                .build());
        }
    }
    val RED_WHITE_BLINK_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c红白色闪烁烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(true)
                .withColor(Color.fromRGB(11743532), Color.fromRGB(15790320))
                .build());

        }
    }
    val YELLOW_WHITE_BLINK_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c黄白色闪烁烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(true)
                .withColor(Color.fromRGB(14602026), Color.fromRGB(15790320))
                .build());
        }
    }
    val GREEN_WHITE_BLINK_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c绿白色闪烁烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(true)
                .withColor(Color.fromRGB(4312372), Color.fromRGB(15790320))
                .build());
        }
    }
    val BLUE_WHITE_BLINK_FIREWORK = newItem(
        Material.FIREWORK_ROCKET, "&c蓝白色闪烁烟花"
    ).withMeta {
        with(this as FireworkMeta){
            this.power = 1
            addEffect(FireworkEffect.builder()
                .trail(false)
                .with(Type.BALL)
                .flicker(false)
                .withColor(Color.fromRGB(2437522))
                .withFade(Color.fromRGB(1973019))
                .build());
        }
    }


}