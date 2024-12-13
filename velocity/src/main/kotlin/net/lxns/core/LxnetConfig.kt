package net.lxns.core

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
class LxnetConfig(
    val db: DatabaseConfig = DatabaseConfig(
        "jdbc:sqlite:mem",
        "",
        ""
    ),
) {
}

@ConfigSerializable
class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String
)