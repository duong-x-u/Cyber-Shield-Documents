package com.thaiduong.cybershield.data.repository

import com.thaiduong.cybershield.domain.model.Game
import com.thaiduong.cybershield.domain.repository.GameRepository

class GameRepositoryImpl : GameRepository {
    private val heavyGames = setOf(
        // Action & MOBA
        Game("com.garena.game.kgvn", "Liên Quân Mobile", true),
        Game("com.dts.freefireth", "Free Fire", true),
        Game("com.tencent.ig", "PUBG Mobile", true),
        Game("com.vng.pubgmobile", "PUBG Mobile VNG", true),
        Game("com.riotgames.league.wildrift", "LMHT Tốc Chiến", true),
        Game("com.pixonic.wwr", "War Robots", true),
        Game("com.wb.goog.injustice.brawler2017", "Injustice 2", true),
        Game("com.kurogame.wutheringwaves.en", "Wuthering Waves", true),
        Game("com.levelinfinite.sgameGlobal", "Honor Of Kings", true),

        // RPG & Adventure
        Game("com.miHoYo.GenshinImpact", "Genshin Impact", true),
        Game("com.HoYoverse.hkrpgoversea", "Honkai: Star Rail", true),
        Game("com.miHoYo.Honkai3rd", "Honkai Impact 3", true),
        Game("com.hoyoverse.zzz.global", "Zenless Zone Zero", true),
        Game("com.YoStarEN.Arknights", "Arknights", true),
        Game("com.thatgamecompany.journey", "Sky: Children of the Light", true),

        // Strategy & Simulation
        Game("com.riotgames.league.teamfighttactics", "Đấu Trường Chân Lý", true),
        Game("com.riotgames.legendsofruneterra", "Huyền Thoại Runeterra", true),
        Game("com.lilithgame.roc.gp", "Rise of Kingdoms", true),
        Game("com.igg.android.lordsmobile", "Lords Mobile", true),
        Game("com.playrix.township", "Township", true),
        Game("com.chucklefish.stardewvalley", "Stardew Valley", true),

        // Sandbox & Casual
        Game("com.mojang.minecraftpe", "Minecraft", true),
        Game("com.roblox.client", "Roblox", true),
        Game("com.moonactive.coinmaster", "Coin Master", true),

        // Puzzle & Rhythm
        Game("com.ustwo.monumentvalley2", "Monument Valley 2", true),
        Game("com.robtopgames.geometrydash", "Geometry Dash", true),
        Game("com.rayark.cytus2", "Cytus II", true)
    )

    override fun getHeavyGames(): Set<Game> = heavyGames

    override fun isHeavyGame(packageName: String): Boolean = 
        heavyGames.any { it.packageName == packageName }
}
