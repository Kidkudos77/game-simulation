package com.example

import androidx.compose.runtime.Immutable

@Immutable
data class StatPoints(
    val ki: Int = 0,
    val malice: Int = 0,
    val infamy: Int = 0,
    val health: Int = 0
) {
    operator fun plus(other: StatPoints): StatPoints {
        return StatPoints(
            ki = this.ki + other.ki,
            malice = this.malice + other.malice,
            infamy = this.infamy + other.infamy,
            health = this.health + other.health
        )
    }
}

@Immutable
data class PrologueOrigin(
    val id: Int,
    val title: String,
    val cost: Int,
    val description: String,
    val choices: List<NodeChoice>
)

@Immutable
data class NodeChoice(
    val id: String,
    val title: String,
    val description: String,
    val stats: StatPoints,
    val statTags: List<String>
)

@Immutable
data class MidGameBridge(
    val id: String,
    val originId: Int,
    val title: String,
    val description: String,
    val stats: StatPoints,
    val statTags: List<String>
)

@Immutable
data class Alignment(
    val id: String, // "SAIYAN", "FERAL", "TYRANT"
    val title: String,
    val description: String,
    val requirements: String,
    val statBadge: String
)

@Immutable
data class LateGameCrisis(
    val id: String,
    val alignmentId: String,
    val title: String,
    val description: String,
    val stats: StatPoints,
    val statTags: List<String>
)

@Immutable
data class EndingClosure(
    val id: String,
    val title: String,
    val description: String,
    val unlockRequirement: String
)

@Immutable
data class CosmicAftermath(
    val id: String,
    val endingId: String,
    val title: String,
    val description: String,
    val sector: String
)

object StoryData {
    val origins = listOf(
        PrologueOrigin(
            id = 1,
            title = "1. The Mountain Feral",
            cost = 0,
            description = "Grandpa Gohan is unable to tame Kakarot. During the full moon, the Great Ape kills Gohan. Kakarot survives alone in Mount Paozu as a feral apex predator.",
            choices = listOf(
                NodeChoice("1A", "Choice A: Raid Human Villages", "Terrorize local towns for survival resources.", StatPoints(infamy = 2, malice = 2), listOf("+Infamy", "+Malice")),
                NodeChoice("1B", "Choice B: Hunt Wilderness Beasts", "Pure isolate training against massive dinosaurs.", StatPoints(ki = 2, health = 2), listOf("+Ki", "+Health"))
            )
        ),
        PrologueOrigin(
            id = 2,
            title = "2. Emperor Pilaf's Enforcer",
            cost = 50,
            description = "The Pilaf Gang tracks the pod. Pilaf bribes the highly destructive, feral child with endless feasts to serve as his ultimate weapon for global domination.",
            choices = listOf(
                NodeChoice("2A", "Choice A: Brute Force Muscle", "Obliterate Pilaf's enemies and rival factions.", StatPoints(infamy = 1, ki = 2), listOf("+Infamy", "+Ki")),
                NodeChoice("2B", "Choice B: Subvert Authority", "Extort towns directly to build a shadow empire.", StatPoints(infamy = 2, health = 1), listOf("+Infamy", "+Health"))
            )
        ),
        PrologueOrigin(
            id = 3,
            title = "3. Red Ribbon Vanguard",
            cost = 100,
            description = "The Red Ribbon Army intercepts the pod. Dr. Gero and Commander Red raise Kakarot in a strict, militarized lab as a cybernetic living weapon.",
            choices = listOf(
                NodeChoice("3A", "Choice A: Cybernetic Protocols", "Integrate mechanical tech upgrades into physiology.", StatPoints(infamy = 1, ki = 2), listOf("+Infamy", "+Ki")),
                NodeChoice("3B", "Choice B: Lead Frontline Purges", "Wipe out entire resistance armies globally.", StatPoints(infamy = 1, malice = 2), listOf("+Infamy", "+Malice"))
            )
        ),
        PrologueOrigin(
            id = 4,
            title = "4. Crane School Assassin",
            cost = 150,
            description = "Master Shen senses Kakarot's dark energy. Raised alongside Tien and Chiaotzu, Kakarot learns the Dodon Ray and precise, calculated cruelty.",
            choices = listOf(
                NodeChoice("4A", "Choice A: Master Lethal Ki", "Focus heavily on perfecting precise fatal energy beams.", StatPoints(malice = 1, ki = 2), listOf("+Malice", "+Ki")),
                NodeChoice("4B", "Choice B: Shadow Assassinations", "Quietly eliminate corporate and political leaders.", StatPoints(infamy = 2, health = 1), listOf("+Infamy", "+Health"))
            )
        ),
        PrologueOrigin(
            id = 5,
            title = "5. Turtle School Defector",
            cost = 200,
            description = "Master Roshi attempts to tame him. Kakarot learns martial arts with Krillin, but his programming snaps: he slaughters classmates and steals the secret scrolls.",
            choices = listOf(
                NodeChoice("5A", "Choice A: Hunt Down Masters", "Travel the world to kill off martial arts legends.", StatPoints(malice = 2, ki = 1), listOf("+Malice", "+Ki")),
                NodeChoice("5B", "Choice B: Found Dark Dojo Empire", "Sell weaponized training to high-paying mercenaries.", StatPoints(infamy = 2, health = 1), listOf("+Infamy", "+Health"))
            )
        ),
        PrologueOrigin(
            id = 6,
            title = "6. Demon Clan Initiate",
            cost = 250,
            description = "Demon King Piccolo is unleashed early. Sensing the alien boy's absolute malice, Piccolo recruits him to systematically eradicate Earth's top martial artists.",
            choices = listOf(
                NodeChoice("6A", "Choice A: Exterminate Lineages", "Hunt and execution of high-level heroes under Piccolo's flag.", StatPoints(infamy = 1, malice = 2), listOf("+Infamy", "+Malice")),
                NodeChoice("6B", "Choice B: Siphon Demonic Ki", "Drain dead demon spawns' reservoirs to fuel yourself.", StatPoints(ki = 2, health = 1), listOf("+Ki", "+Health"))
            )
        ),
        PrologueOrigin(
            id = 7,
            title = "7. Kami's Failed Experiment",
            cost = 300,
            description = "Kami imprisons Kakarot on the Lookout, trying to suppress his nature with divine energy. The game starts as his Saiyan programming shatters the divine seal.",
            choices = listOf(
                NodeChoice("7A", "Choice A: Sanctuary Rebellion", "Turn on Mr. Popo and Kami instantly to destroy Lookout.", StatPoints(malice = 2, ki = 1), listOf("+Malice", "+Ki")),
                NodeChoice("7B", "Choice B: Plunder Divine Artifacts", "Steal Senzu Beans and mystical treasures, then flee.", StatPoints(infamy = 2, health = 1), listOf("+Infamy", "+Health"))
            )
        )
    )

    val midGameBridges = listOf(
        // Origin 1
        MidGameBridge("M1A", 1, "Choice A: Full-Moon Rampage", "Embrace Oozaru — raid a city, cement your predator legend.", StatPoints(malice = 3, infamy = 2), listOf("+Malice", "+Infamy")),
        MidGameBridge("M1B", 1, "Choice B: Claim Hunting Grounds", "Stake out vast territory, control its resources as a warlord.", StatPoints(ki = 3, health = 1), listOf("+Ki", "+Health")),
        
        // Origin 2
        MidGameBridge("M2A", 2, "Choice A: Seize the Dragon Balls", "Betray Pilaf, collect all seven — wish your way.", StatPoints(infamy = 3, malice = 1), listOf("+Infamy", "+Betray")),
        MidGameBridge("M2B", 2, "Choice B: Destroy the Command Chain", "Slaughter Pilaf and Mai — operate free of any leash.", StatPoints(malice = 3), listOf("+Malice")),

        // Origin 3
        MidGameBridge("M3A", 3, "Choice A: Execute the Purge Quota", "Meet every clearance target; rise to Supreme Vanguard rank.", StatPoints(ki = 3, malice = 1), listOf("+Ki")),
        MidGameBridge("M3B", 3, "Choice B: Hijack the RR Packets", "Steal the global weapons cache — sell it to the highest bidder.", StatPoints(infamy = 3), listOf("+Infamy")),

        // Origin 4
        MidGameBridge("M4A", 4, "Choice A: Snap the Code of Honor", "Kill Master Shen — declare yourself unique fit for pure bloodlust.", StatPoints(malice = 3), listOf("+Malice")),
        MidGameBridge("M4B", 4, "Choice B: Perfect the Dodon Ray", "Refine lethal precision; accept Shen's top-tier assassination contracts.", StatPoints(ki = 3), listOf("+Ki")),

        // Origin 5
        MidGameBridge("M5A", 5, "Choice A: Sell the Kame Scrolls", "Auction Roshi's secret techniques to rival criminal syndicates.", StatPoints(infamy = 3, health = 1), listOf("+Infamy", "+Swift")),
        MidGameBridge("M5B", 5, "Choice B: Massacre the Island", "Wipe Kame House off the map — leave no witnesses standing.", StatPoints(malice = 3), listOf("+Malice")),

        // Origin 6
        MidGameBridge("M6A", 6, "Choice A: Devour Demon Spawns", "Cannibalize Piccolo's demon army to spike your own power.", StatPoints(malice = 2, ki = 2), listOf("+Malice", "+Ki")),
        MidGameBridge("M6B", 6, "Choice B: Subjugate Earth Territories", "Install demon warlords in every nation under your outlook.", StatPoints(infamy = 3), listOf("+Infamy")),

        // Origin 7
        MidGameBridge("M7A", 7, "Choice A: Shatter the Lookout", "Demolish the Hyperbolic Time Chamber — collapse divine order.", StatPoints(malice = 3), listOf("+Malice")),
        MidGameBridge("M7B", 7, "Choice B: Steal the Senzu Supply Chain", "Corner the healing bean market — sell immunity to the highest bidder.", StatPoints(infamy = 3), listOf("+Infamy"))
    )

    val alignments = mapOf(
        "SAIYAN" to Alignment(
            id = "SAIYAN",
            title = "Path of Saiyan Duty",
            description = "Prioritizes strategic progression and raw fighting thresholds while adhering to basic vanguard directives. Avoids excessive planetary damage to maximize market value.",
            requirements = "High Ki State",
            statBadge = "Ki Dominant"
        ),
        "FERAL" to Alignment(
            id = "FERAL",
            title = "Path of Feral Bloodlust",
            description = "Driven entirely by base primal violence and constant Oozaru full-moon rampages, completely fracturing your cognitive capabilities into a brutal predator.",
            requirements = "Raw Malice State",
            statBadge = "Malice Dominant"
        ),
        "TYRANT" to Alignment(
            id = "TYRANT",
            title = "Path of Calculated Tyranny",
            description = "Focuses heavily on manipulating human technological capital, locking down the Dragon Balls, and managing planetary infrastructure under an iron fist.",
            requirements = "High Infamy State",
            statBadge = "Infamy Dominant"
        )
    )

    val lateGameCrises = listOf(
        // From SAIYAN
        LateGameCrisis("C_SAIYAN_A", "SAIYAN", "Choice A: Bow to the Prince", "Pledge allegiance to Vegeta's chain of command when Raditz lands.", StatPoints(ki = 3), listOf("+Ki", "+Faction")),
        LateGameCrisis("C_SAIYAN_B", "SAIYAN", "Choice B: Negotiate Autonomy", "Use Earth's Dragon Balls as leverage — break free from Saiyan command and rule solo.", StatPoints(infamy = 3, ki = 1), listOf("+Infamy", "+Ki")),

        // From FERAL
        LateGameCrisis("C_FERAL_A", "FERAL", "Choice A: Execute the Messenger", "Kill Raditz on sight — no Saiyan hierarchy will ever cage you again.", StatPoints(malice = 3, ki = 1), listOf("+Malice", "+Ki")),
        LateGameCrisis("C_FERAL_B", "FERAL", "Choice B: Harness the Saiyan Intel", "Torture Raditz for Frieza Force data — join the army on your own savage terms.", StatPoints(ki = 3), listOf("+Ki")),

        // From TYRANT
        LateGameCrisis("C_TYRANT_A", "TYRANT", "Choice A: Weaponize the Dragon Balls", "Wish for immortality — dissolve Raditz's threat with a single decree.", StatPoints(infamy = 3), listOf("+Infamy")),
        LateGameCrisis("C_TYRANT_B", "TYRANT", "Choice B: Lure Frieza's Fleet", "Broadcast Earth's coordinates — then betray the fleet for planetary supremacy.", StatPoints(malice = 2, infamy = 2), listOf("+Malice", "+Infamy"))
    )

    val endings = mapOf(
        "E1" to EndingClosure(
            id = "E1",
            title = "Ending 1: The Loyal Vanguard",
            description = "Kakarot completes his original core mission. He successfully clears out all major Earth obstacles (Roshi, Crane Hermit, Kami) and massacres the populations. When Raditz arrives, Kakarot joins him to serve in the Frieza Force.",
            unlockRequirement = "Unlock: Frieza Force Scouter"
        ),
        "E2" to EndingClosure(
            id = "E2",
            title = "Ending 2: The Apex Predator",
            description = "Earth made Kakarot fully wild. When Raditz lands and commands absolute submission to Prince Vegeta, Kakarot executes his own brother on the spot, claiming Earth as his custom sandbox hunting range.",
            unlockRequirement = "Unlock: Oozaru Bloodline Trait"
        ),
        "E3" to EndingClosure(
            id = "E3",
            title = "Ending 3: The God-King of Earth",
            description = "Kakarot optimizes human global assets. He establishes himself as a sovereign planetary deity. When Raditz arrives, Kakarot uses the Dragon Balls to completely neutralize him, breaking ties with the Saiyan remnants.",
            unlockRequirement = "Unlock: RR / Crane Overlord"
        )
    )

    val aftermaths = mapOf(
        "A1" to CosmicAftermath(
            id = "A1",
            endingId = "E1",
            title = "Aftermath: Frieza Force Ascension",
            description = "Kakarot is inducted as an elite operative. Earth is catalogued and sold on the planet brokerage as he rises through the ranks beside Vegeta and Nappa, and the Saiyan purge cycle resumes across the North Galaxy — Earth logged as his first trophy conquest.",
            sector = "Galaxy State: Purge Cycle Resumes"
        ),
        "A2" to CosmicAftermath(
            id = "A2",
            endingId = "E2",
            title = "Aftermath: The Quarantined Deathworld",
            description = "With Raditz dead, the Frieza Force flags Earth as a hostile anomaly and blockades the sector. Kakarot reigns over a silent, depopulated hunting range until Vegeta arrives in person to investigate the missing scout — a collision no command structure can defuse.",
            sector = "Galaxy State: Sector Blockade"
        ),
        "A3" to CosmicAftermath(
            id = "A3",
            endingId = "E3",
            title = "Aftermath: The Sovereign Anomaly",
            description = "Shielded by the Dragon Balls and his own immortality, Kakarot seals Earth into a fortress-state outside Frieza's ledger. Galactic empires trade rumors of a god-king world that answers to no emperor, and a cold war forms around a single planet no one dares to purge.",
            sector = "Galaxy State: Cold War Standoff"
        )
    )
}
