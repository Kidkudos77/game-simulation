package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BattleOpponent(
    val name: String,
    val maxHp: Int,
    val hp: Int,
    val attack: Int,
    val avatarEmoji: String,
    val skillName: String
)

data class GameState(
    val currentStage: Int = 1, // 1 to 7
    val selectedOrigin: PrologueOrigin? = null,
    val selectedChoice: NodeChoice? = null,
    val selectedBridge: MidGameBridge? = null,
    val calculatedAlignment: Alignment? = null,
    val selectedCrisis: LateGameCrisis? = null,
    val calculatedEnding: EndingClosure? = null,
    val calculatedAftermath: CosmicAftermath? = null,
    val stats: StatPoints = StatPoints(),
    
    // Active Combat Arena states
    val activeOpponent: BattleOpponent? = null,
    val kakarotHp: Int = 100,
    val kakarotMaxHp: Int = 100,
    val kakarotKi: Int = 20,
    val kakarotMaxKi: Int = 100,
    val combatLogs: List<String> = emptyList(),
    val isBattleOver: Boolean = false,
    val battleResult: String = "", // "VICTORY", "DEFEAT", or empty
    val bonusStatTokens: Int = 0,
    val bonusStats: StatPoints = StatPoints(),
    val activeTab: String = "MAP", // "MAP" or "ARENA"
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 100,
    val timelineModifier: String = "Normal Flow",
    val activeQuest: String = "Explore your Origins in Stage 1 to set a timeline path."
)

class GameEngineViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // List of legendary opponents Kakarot can challenge
    val opponentsList = listOf(
        BattleOpponent("Grandpa Gohan", 120, 120, 12, "👴", "Jan-Ken Rock!"),
        BattleOpponent("Pilaf Battle Suit", 160, 160, 15, "🤖", "Rocket Barrage!"),
        BattleOpponent("General Blue (Red Ribbon)", 200, 200, 18, "🎖️", "Psychic Paralysis!"),
        BattleOpponent("Master Roshi", 250, 250, 22, "🐢", "Max Power Kamehameha!"),
        BattleOpponent("Demon King Piccolo", 320, 320, 26, "😈", "Explosive Demon Wave!"),
        BattleOpponent("Raditz the Vanguard", 420, 420, 32, "👽", "Double Sunday Blast!")
    )

    fun switchTab(tab: String) {
        _state.update { it.copy(activeTab = tab) }
    }

    // Allocate earned arena bonus stats directly to story branching parameters
    fun allocateBonusStat(statType: String) {
        _state.update { current ->
            if (current.bonusStatTokens <= 0) return@update current
            
            val updatedBonus = when (statType) {
                "KI" -> current.bonusStats.copy(ki = current.bonusStats.ki + 1)
                "MALICE" -> current.bonusStats.copy(malice = current.bonusStats.malice + 1)
                "INFAMY" -> current.bonusStats.copy(infamy = current.bonusStats.infamy + 1)
                "HEALTH" -> current.bonusStats.copy(health = current.bonusStats.health + 1)
                else -> current.bonusStats
            }

            // Recalculate full accumulated stats with bonus allocations included
            val originChoiceStats = (current.selectedChoice?.stats ?: StatPoints()) + 
                                   (current.selectedBridge?.stats ?: StatPoints()) + 
                                   (current.selectedCrisis?.stats ?: StatPoints())
            
            val totalStats = originChoiceStats + updatedBonus
            
            // Recalculate dynamic milestones
            val updatedAlignment = current.selectedBridge?.let { bridge ->
                val alignmentId = calculateAlignmentId(totalStats, bridge.id)
                StoryData.alignments[alignmentId]
            }

            val updatedEndingAndAftermath = if (updatedAlignment != null && current.selectedCrisis != null) {
                val (endingId, aftermathId) = calculateEndingAndAftermath(updatedAlignment.id, current.selectedCrisis.id)
                Pair(StoryData.endings[endingId], StoryData.aftermaths[aftermathId])
            } else Pair(current.calculatedEnding, current.calculatedAftermath)

            current.copy(
                bonusStats = updatedBonus,
                bonusStatTokens = current.bonusStatTokens - 1,
                stats = totalStats,
                calculatedAlignment = updatedAlignment,
                calculatedEnding = updatedEndingAndAftermath.first,
                calculatedAftermath = updatedEndingAndAftermath.second
            )
        }
    }

    // Select an arena combatant and initialize fight parameters
    fun startBattle(opponent: BattleOpponent) {
        _state.update { current ->
            // Base max hp escalates with Kakarot's Story Health state and current player Level
            val maxHp = 100 + (current.stats.health * 12) + (current.level * 15)
            val maxKi = 100 + (current.level * 5)
            val startKi = (30 + (current.stats.ki * 4) + (current.level * 2)).coerceAtMost(maxKi)
            current.copy(
                activeOpponent = opponent.copy(hp = opponent.maxHp),
                kakarotHp = maxHp,
                kakarotMaxHp = maxHp,
                kakarotKi = startKi,
                kakarotMaxKi = maxKi,
                isBattleOver = false,
                battleResult = "",
                combatLogs = listOf("⚔️ Battle Initiated! Kakarot (Lv. ${current.level}) stands ready against ${opponent.name}!", "👉 Current Active Reality Aura: ${current.timelineModifier}")
            )
        }
    }

    // Execute player combat moves
    fun executeKakarotMove(moveType: String) {
        val currentState = _state.value
        val opponent = currentState.activeOpponent ?: return
        if (currentState.isBattleOver) return

        _state.update { current ->
            val logs = current.combatLogs.toMutableList()
            var damageDealt = 0
            var kiCost = 0
            var kiGained = 0
            var hpHealed = 0
            var isTerrifying = false

            // Check passive from timeline modifiers
            val isEclipseBonus = current.timelineModifier.contains("Eclipse")
            val isCyberneticBonus = current.timelineModifier.contains("Cybernetic")
            val isDemonicBonus = current.timelineModifier.contains("Demonic")

            when (moveType) {
                "CHARGE" -> {
                    val baseCharge = 30 + (current.stats.ki * 2) + (current.level * 3)
                    kiGained = if (isDemonicBonus) (baseCharge * 1.5f).toInt() else baseCharge
                    hpHealed = 15 + (current.stats.health * 2) + (current.level * 4)
                    logs.add("⚡ Kakarot screams as violent energy aura flares! Gained $kiGained Ki & healed $hpHealed HP!")
                }
                "MELEE" -> {
                    val baseMelee = 12 + (current.stats.malice * 3) + (1..6).random() + (current.level * 4)
                    damageDealt = if (isEclipseBonus) (baseMelee * 1.3f).toInt() else baseMelee
                    val baseHeal = (damageDealt * 0.25f).toInt()
                    hpHealed = if (isEclipseBonus) (damageDealt * 0.50f).toInt() else baseHeal
                    logs.add("🩸 Kakarot strikes with feral teeth and claws! Deals $damageDealt damage and licks blood to heal $hpHealed HP!")
                }
                "BEAM" -> {
                    kiCost = 25
                    if (current.kakarotKi < kiCost) {
                        logs.add("⚠️ Not enough Ki to fire Dodon Ray! Need $kiCost Ki.")
                        return@update current
                    }
                    val baseBeam = 28 + (current.stats.ki * 4) + (1..10).random() + (current.level * 6)
                    damageDealt = if (isCyberneticBonus) (baseBeam * 1.3f).toInt() else baseBeam
                    logs.add("☄️ Kakarot fires a brilliant piercing Dodon Ray! Explodes dealing $damageDealt damage!")
                }
                "ROAR" -> {
                    kiCost = 15
                    if (current.kakarotKi < kiCost) {
                        logs.add("⚠️ Not enough Ki to roar! Need $kiCost Ki.")
                        return@update current
                    }
                    damageDealt = 15 + (current.stats.infamy * 3) + (1..5).random() + (current.level * 5)
                    isTerrifying = true
                    logs.add("👑 Kakarot unleashes a brutal Great Ape howl! Deals $damageDealt sonic damage and intimidates opponent!")
                }
            }

            // Calculate damage on Opponent
            val newOpponentHp = (opponent.hp - damageDealt).coerceAtLeast(0)
            val updatedOpponent = opponent.copy(hp = newOpponentHp)

            var newKakarotHp = (current.kakarotHp + hpHealed).coerceAtMost(current.kakarotMaxHp)
            var newKakarotKi = (current.kakarotKi - kiCost + kiGained).coerceIn(0, current.kakarotMaxKi)

            var battleOver = false
            var result = ""
            var gainedToken = 0
            var newXp = current.xp
            var newLevel = current.level
            var newMaxXp = current.maxXp

            if (newOpponentHp <= 0) {
                battleOver = true
                result = "VICTORY"
                gainedToken = 1
                logs.add("🏆 VICTORY! ${opponent.name} was crushed under Kakarot's overwhelming power!")
                logs.add("✨ Gained +1 Bonus Stat Token to allocate in Map mode!")
                
                // Award RPG XP
                val xpGained = 40 + (opponent.attack * 2)
                newXp += xpGained
                logs.add("🌟 Gained +$xpGained Experience Points!")
                
                if (newXp >= newMaxXp) {
                    newLevel++
                    newXp -= newMaxXp
                    newMaxXp = (newMaxXp * 1.5f).toInt()
                    logs.add("✨ LEVEL UP! Kakarot reached Level $newLevel! Base damage, Ki, and max health limits scaled up!")
                }
            } else {
                // Opponent counter-attack
                var rawEnemyAtk = opponent.attack + (1..6).random()
                if (isTerrifying) {
                    rawEnemyAtk /= 2 // Intimidated deals half damage
                    logs.add("🛡️ ${opponent.name} is trembling in fear! Damage halved.")
                }
                newKakarotHp = (newKakarotHp - rawEnemyAtk).coerceAtLeast(0)
                logs.add("💥 ${opponent.name} retaliates with ${opponent.skillName} for $rawEnemyAtk damage!")

                if (newKakarotHp <= 0) {
                    battleOver = true
                    result = "DEFEAT"
                    logs.add("💀 DEFEAT! Kakarot collapsed in battle! Train more or adjust your story branches to grow stronger.")
                }
            }

            current.copy(
                activeOpponent = updatedOpponent,
                kakarotHp = newKakarotHp,
                kakarotKi = newKakarotKi,
                combatLogs = logs,
                isBattleOver = battleOver,
                battleResult = result,
                bonusStatTokens = current.bonusStatTokens + gainedToken,
                xp = newXp,
                level = newLevel,
                maxXp = newMaxXp
            )
        }
    }

    fun selectOrigin(origin: PrologueOrigin) {
        _state.update { current ->
            // Changing origin resets subsequent choices
            val originChoiceStats = StatPoints()
            val totalStats = originChoiceStats + current.bonusStats
            current.copy(
                selectedOrigin = origin,
                selectedChoice = null,
                selectedBridge = null,
                calculatedAlignment = null,
                selectedCrisis = null,
                calculatedEnding = null,
                calculatedAftermath = null,
                stats = totalStats,
                currentStage = 2
            )
        }
    }

    fun selectChoice(choice: NodeChoice) {
        val origin = _state.value.selectedOrigin ?: return
        _state.update { current ->
            val originChoiceStats = choice.stats
            val totalStats = originChoiceStats + current.bonusStats
            current.copy(
                selectedChoice = choice,
                selectedBridge = null,
                calculatedAlignment = null,
                selectedCrisis = null,
                calculatedEnding = null,
                calculatedAftermath = null,
                stats = totalStats,
                currentStage = 3
            )
        }
    }

    fun selectBridge(bridge: MidGameBridge) {
        val choice = _state.value.selectedChoice ?: return
        _state.update { current ->
            val originChoiceStats = choice.stats + bridge.stats
            val totalStats = originChoiceStats + current.bonusStats
            
            // Auto-calculate alignment based on stats
            val alignmentId = calculateAlignmentId(totalStats, bridge.id)
            val alignment = StoryData.alignments[alignmentId]

            current.copy(
                selectedBridge = bridge,
                calculatedAlignment = alignment,
                selectedCrisis = null,
                calculatedEnding = null,
                calculatedAftermath = null,
                stats = totalStats,
                currentStage = 5 // Jump past Stage 4 (Alignment is auto-locked and shown)
            )
        }
    }

    fun selectCrisis(crisis: LateGameCrisis) {
        val alignment = _state.value.calculatedAlignment ?: return
        val choice = _state.value.selectedChoice ?: return
        val bridge = _state.value.selectedBridge ?: return
        _state.update { current ->
            val originChoiceStats = choice.stats + bridge.stats + crisis.stats
            val totalStats = originChoiceStats + current.bonusStats
            
            // Auto-calculate ending and aftermath based on alignment & crisis choice
            val (endingId, aftermathId) = calculateEndingAndAftermath(alignment.id, crisis.id)
            val ending = StoryData.endings[endingId]
            val aftermath = StoryData.aftermaths[aftermathId]

            current.copy(
                selectedCrisis = crisis,
                calculatedEnding = ending,
                calculatedAftermath = aftermath,
                stats = totalStats,
                currentStage = 7 // Jump past Stage 6 (Ending & Aftermath are locked)
            )
        }
    }

    fun rewindToStage(stage: Int) {
        if (stage >= _state.value.currentStage) return // Only allow rewinding to completed/previous stages

        _state.update { current ->
            when (stage) {
                1 -> {
                    current.copy(
                        selectedOrigin = null,
                        selectedChoice = null,
                        selectedBridge = null,
                        calculatedAlignment = null,
                        selectedCrisis = null,
                        calculatedEnding = null,
                        calculatedAftermath = null,
                        stats = current.bonusStats,
                        currentStage = 1
                    )
                }
                2 -> {
                    // Back to choosing choice (Origin stays)
                    current.copy(
                        selectedChoice = null,
                        selectedBridge = null,
                        calculatedAlignment = null,
                        selectedCrisis = null,
                        calculatedEnding = null,
                        calculatedAftermath = null,
                        stats = current.bonusStats, // Reset stats back to bonus-only
                        currentStage = 2
                    )
                }
                3 -> {
                    // Back to choosing bridge (Origin and Choice stay)
                    val originChoiceStats = current.selectedChoice?.stats ?: StatPoints()
                    val totalStats = originChoiceStats + current.bonusStats
                    current.copy(
                        selectedBridge = null,
                        calculatedAlignment = null,
                        selectedCrisis = null,
                        calculatedEnding = null,
                        calculatedAftermath = null,
                        stats = totalStats,
                        currentStage = 3
                    )
                }
                4, 5 -> {
                    // Back to choosing crisis (Bridge & Alignment stay)
                    val originChoiceStats = (current.selectedChoice?.stats ?: StatPoints()) + 
                                    (current.selectedBridge?.stats ?: StatPoints())
                    val totalStats = originChoiceStats + current.bonusStats
                    current.copy(
                        selectedCrisis = null,
                        calculatedEnding = null,
                        calculatedAftermath = null,
                        stats = totalStats,
                        currentStage = 5
                    )
                }
                else -> current
            }
        }
    }

    fun reset() {
        _state.value = GameState()
    }

    fun regenerateReality() {
        val modifiers = listOf(
            "Full Moon Eclipse" to "Surge in Feral energy! Malice +2. MELEE claw strikes deal +30% damage and heal 50% HP.",
            "Cybernetic Overdrive" to "Precision enhancements active! Infamy +2. BEAM Dodon Rays deal +30% damage.",
            "Demonic Overlap" to "Siphon dark power reserves! Ki +2. CHARGE energy recovery is +50% more efficient.",
            "Divine Kai Blessing" to "Protected by divine aura! Health +2. Health recovery rates and limits boosted."
        )
        val selected = modifiers.random()
        
        _state.update { current ->
            val randomStatBonus = when (selected.first) {
                "Full Moon Eclipse" -> current.bonusStats.copy(malice = current.bonusStats.malice + 2)
                "Cybernetic Overdrive" -> current.bonusStats.copy(infamy = current.bonusStats.infamy + 2)
                "Demonic Overlap" -> current.bonusStats.copy(ki = current.bonusStats.ki + 2)
                "Divine Kai Blessing" -> current.bonusStats.copy(health = current.bonusStats.health + 2)
                else -> current.bonusStats
            }

            current.copy(
                currentStage = 1,
                selectedOrigin = null,
                selectedChoice = null,
                selectedBridge = null,
                calculatedAlignment = null,
                selectedCrisis = null,
                calculatedEnding = null,
                calculatedAftermath = null,
                stats = randomStatBonus,
                bonusStats = randomStatBonus,
                timelineModifier = "${selected.first}: ${selected.second}",
                activeQuest = "Timeline regenerated! Path unlocked under ${selected.first}.",
                activeOpponent = null,
                isBattleOver = false,
                battleResult = ""
            )
        }
    }

    private fun calculateAlignmentId(stats: StatPoints, bridgeId: String): String {
        val ki = stats.ki
        val malice = stats.malice
        val infamy = stats.infamy

        // Check dominant stat
        if (ki > malice && ki > infamy) return "SAIYAN"
        if (malice > ki && malice > infamy) return "FERAL"
        if (infamy > ki && infamy > malice) return "TYRANT"

        // If there's a tie, use the bridgeId as a tie breaker
        return when (bridgeId) {
            "M1B", "M3A", "M4B" -> "SAIYAN"
            "M1A", "M2B", "M4A", "M5B", "M6A", "M7A" -> "FERAL"
            else -> "TYRANT"
        }
    }

    private fun calculateEndingAndAftermath(alignmentId: String, crisisId: String): Pair<String, String> {
        return when (alignmentId) {
            "SAIYAN" -> {
                if (crisisId == "C_SAIYAN_A") {
                    Pair("E1", "A1") // Bow to Prince -> Loyal Vanguard
                } else {
                    Pair("E3", "A3") // Negotiate -> Sovereign Anomaly
                }
            }
            "FERAL" -> {
                if (crisisId == "C_FERAL_A") {
                    Pair("E2", "A2") // Execute Messenger -> Apex Predator
                } else {
                    Pair("E1", "A1") // Harness Intel -> Loyal Vanguard
                }
            }
            "TYRANT" -> {
                if (crisisId == "C_TYRANT_A") {
                    Pair("E3", "A3") // Weaponize Dragon Balls -> Sovereign Anomaly
                } else {
                    Pair("E2", "A2") // Lure Frieza's Fleet -> Apex Predator / Quarantined Deathworld
                }
            }
            else -> Pair("E1", "A1")
        }
    }
}
