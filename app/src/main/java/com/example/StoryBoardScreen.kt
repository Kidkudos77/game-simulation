package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StoryBoardScreen(
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0B10)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .drawBehind {
                    // Starry dark space aesthetic background with a subtle red/purple/gold celestial glow
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF141725),
                                Color(0xFF0A0B10)
                            ),
                            center = Offset(size.width / 2f, size.height / 3f),
                            radius = size.width.coerceAtLeast(size.height) * 0.9f
                        )
                    )
                }
        ) {
            // Header panel containing game logo and global controls
            HeaderPanel(
                onReset = { viewModel.reset() },
                onRegenerate = { viewModel.regenerateReality() }
            )

            // Double Tab Controller: Toggle between Interactive Map and Retro Combat Arena
            TabRow(
                selectedTabIndex = if (state.activeTab == "MAP") 0 else 1,
                containerColor = Color(0xFF0F131E),
                contentColor = SaiyanGold,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (state.activeTab == "MAP") 0 else 1]),
                        color = SaiyanGold,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = state.activeTab == "MAP",
                    onClick = { viewModel.switchTab("MAP") },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1 • STORY TIMELINE", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    },
                    selectedContentColor = SaiyanGold,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = state.activeTab == "ARENA",
                    onClick = { viewModel.switchTab("ARENA") },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("2 • RPG COMBAT ARENA", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    },
                    selectedContentColor = FeralCrimson,
                    unselectedContentColor = TextSecondary
                )
            }

            // Stat and character attribute panel (HUD)
            StatsHUD(
                stats = state.stats,
                bonusTokens = state.bonusStatTokens,
                level = state.level,
                xp = state.xp,
                maxXp = state.maxXp,
                timelineModifier = state.timelineModifier,
                onAllocate = { stat -> viewModel.allocateBonusStat(stat) }
            )

            // Render either the Map board or the Turn-Based Combat screen
            if (state.activeTab == "MAP") {
                // Interactive Timeline Slider Tracker with clicks that trigger rewinds
                TrackingRail(
                    currentStage = state.currentStage,
                    state = state,
                    onStageClick = { stageId -> viewModel.rewindToStage(stageId) }
                )

                // Large central "Visual Novel / Event Card" display for immersive play!
                VisualNovelStoryBoard(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            } else {
                // Retro Turn-Based RPG Combat Arena Interface
                RetroRpgArena(
                    state = state,
                    opponents = viewModel.opponentsList,
                    onStartBattle = { opp -> viewModel.startBattle(opp) },
                    onAction = { move -> viewModel.executeKakarotMove(move) }
                )
            }
        }
    }
}

// ---------------- VISUAL NOVEL STORY BOARD COMPOSABLE ----------------

@Composable
fun VisualNovelStoryBoard(
    state: GameState,
    viewModel: GameEngineViewModel,
    modifier: Modifier = Modifier
) {
    val currentStage = state.currentStage

    // Calculate active neon border color based on current stage alignment
    val alignmentColor = when (state.calculatedAlignment?.id) {
        "SAIYAN" -> SaiyanBlue
        "FERAL" -> FeralCrimson
        "TYRANT" -> TyrantPurple
        else -> SaiyanGold
    }

    val activeBorderColor = when (currentStage) {
        1, 2, 3 -> SaiyanGold
        4, 5 -> alignmentColor
        6, 7 -> alignmentColor
        else -> SaiyanGold
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CENTER FOCUS: The Story Card (takes upper-middle space)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF12161E)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, activeBorderColor),
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Stage indicator / category badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(activeBorderColor.copy(alpha = 0.15f))
                            .border(1.dp, activeBorderColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (currentStage) {
                                1 -> "STAGE 1 • PROLOGUE"
                                2 -> "STAGE 2 • PROLOGUE CHOICE"
                                3 -> "STAGE 3 • MID-GAME BRIDGE"
                                4, 5 -> "STAGE 5 • LATE-GAME CRISIS"
                                6, 7 -> "STAGE 7 • COSMIC AFTERMATH"
                                else -> "STAGE $currentStage"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = activeBorderColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    // Active Reality Aura Badge / Text
                    Text(
                        text = "AURA: ${state.timelineModifier.substringBefore(":")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title of the story point
                val storyTitle = when (currentStage) {
                    1 -> "The Arrival of the Saiyan Seed"
                    2 -> state.selectedOrigin?.title ?: "Select Kakarot's Origin"
                    3 -> state.selectedChoice?.title ?: "Prologue Choice Committed"
                    4, 5 -> state.selectedBridge?.title ?: "Destiny Bridge Unlocked"
                    6, 7 -> state.calculatedEnding?.title ?: "The Final Fate"
                    else -> "Project Kakarot Chronicles"
                }

                Text(
                    text = storyTitle.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderDark)
                Spacer(modifier = Modifier.height(12.dp))

                // Narrative Text description
                val narrativeText = when (currentStage) {
                    1 -> "Grandpa Gohan discovers a strange alien infant with a tail in the deep bamboo valleys. Unlike the peaceful boy of legend who fell on his head, Kakarot's Saiyan aggression remains fully primed. Dr. Gero's scans or Master Shen's dark premonitions begin to pull the thread of fate. Choose your origin timeline to alter Kakarot's programming."
                    2 -> state.selectedOrigin?.description ?: ""
                    3 -> state.selectedChoice?.description ?: ""
                    4, 5 -> {
                        val alignmentDesc = state.calculatedAlignment?.description ?: ""
                        val alignmentTitle = state.calculatedAlignment?.title ?: ""
                        "Kakarot's actions have cemented his cosmic path on Earth.\n\n" +
                                "🔴 ALIGNMENT UNLOCKED: $alignmentTitle\n" +
                                "$alignmentDesc\n\n" +
                                "Now, Raditz lands in a blaze of cosmic fire. He scans Kakarot with his scouter and demands immediate obedience. Your path of destiny will decide Kakarot's reaction."
                    }
                    6, 7 -> {
                        val endingTitle = state.calculatedEnding?.title ?: ""
                        val endingDesc = state.calculatedEnding?.description ?: ""
                        val aftermathTitle = state.calculatedAftermath?.title ?: ""
                        val aftermathDesc = state.calculatedAftermath?.description ?: ""
                        val sectorInfo = state.calculatedAftermath?.sector ?: ""

                        "🏆 TIMELINE FULLY RESOLVED\n\n" +
                                "✨ ENDING OUTCOME: $endingTitle\n" +
                                "$endingDesc\n\n" +
                                "🌌 COSMIC AFTERMATH: $aftermathTitle\n" +
                                "$aftermathDesc\n\n" +
                                "🛡️ SECTOR STATE: $sectorInfo"
                    }
                    else -> ""
                }

                Text(
                    text = narrativeText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    lineHeight = 24.sp
                )

                // Info card for Stage 7 complete state
                if (currentStage == 7) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131722))
                            .border(1.dp, SaiyanGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "💡 TIMELINE PATHS INSIGHT",
                                color = SaiyanGold,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your ultimate ending is a function of Ki, Malice, and Infamy. Train in the Arena, allocate your bonus tokens in the Stats HUD, and then rewind to try a different path!",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM AREA: The Action Menu (taking up bottom portion)
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth()
        ) {
            Text(
                text = "ACTION MENU / DECISION MATRIX",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E14)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (currentStage) {
                        1 -> {
                            StoryData.origins.forEach { origin ->
                                val isSelected = state.selectedOrigin?.id == origin.id
                                val cardGlowColor = if (isSelected) SaiyanGold else BorderDark

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF221A15) else DarkSlateCard
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, cardGlowColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectOrigin(origin) }
                                        .testTag("origin_choice_${origin.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(SaiyanGold.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (origin.id) {
                                                    1 -> "🐗"
                                                    2 -> "👑"
                                                    3 -> "🎖️"
                                                    4 -> "☄️"
                                                    5 -> "🐢"
                                                    6 -> "😈"
                                                    7 -> "🏰"
                                                    else -> "💫"
                                                },
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text(
                                                text = origin.title.uppercase(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) SaiyanGold else TextPrimary
                                            )
                                            Text(
                                                text = origin.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SaiyanGold.copy(alpha = 0.12f))
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Cost: ${origin.cost}",
                                                color = SaiyanGold,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            val choices = state.selectedOrigin?.choices ?: emptyList()
                            if (choices.isEmpty()) {
                                Text(
                                    text = "No choices loaded for origin.",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                choices.forEach { choice ->
                                    val isSelected = state.selectedChoice?.id == choice.id
                                    val activeColor = SaiyanBlue
                                    val cardGlowColor = if (isSelected) activeColor else BorderDark

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF151B27) else DarkSlateCard
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, cardGlowColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectChoice(choice) }
                                            .testTag("choice_choice_${choice.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = choice.title.uppercase(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) activeColor else TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = choice.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                choice.statTags.forEach { tag ->
                                                    val tagColor = when {
                                                        tag.contains("Ki") -> PowerKi
                                                        tag.contains("Malice") -> MaliceRed
                                                        tag.contains("Infamy") -> InfamyOrange
                                                        tag.contains("Health") -> HealthGreen
                                                        else -> activeColor
                                                    }
                                                    TagBadge(text = tag, color = tagColor)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            val origin = state.selectedOrigin
                            if (origin == null) {
                                Text(
                                    "Locked: Select an Origin first.",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                val originBridges = StoryData.midGameBridges.filter { it.originId == origin.id }
                                originBridges.forEach { bridge ->
                                    val isSelected = state.selectedBridge?.id == bridge.id
                                    val activeColor = SaiyanGold
                                    val cardGlowColor = if (isSelected) activeColor else BorderDark

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF221A15) else DarkSlateCard
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, cardGlowColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectBridge(bridge) }
                                            .testTag("bridge_choice_${bridge.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = bridge.title.uppercase(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) activeColor else TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = bridge.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                bridge.statTags.forEach { tag ->
                                                    val tagColor = when {
                                                        tag.contains("Ki") -> PowerKi
                                                        tag.contains("Malice") -> MaliceRed
                                                        tag.contains("Infamy") -> InfamyOrange
                                                        tag.contains("Health") -> HealthGreen
                                                        else -> activeColor
                                                    }
                                                    TagBadge(text = tag, color = tagColor)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4, 5 -> {
                            val alignment = state.calculatedAlignment
                            if (alignment == null) {
                                Text(
                                    "Locked: Select a Mid-Game Bridge first.",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                val crisisOptions = StoryData.lateGameCrises.filter { it.alignmentId == alignment.id }
                                crisisOptions.forEach { crisis ->
                                    val isSelected = state.selectedCrisis?.id == crisis.id
                                    val activeColor = alignmentColor
                                    val cardGlowColor = if (isSelected) activeColor else BorderDark

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) activeColor.copy(alpha = 0.12f) else DarkSlateCard
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, cardGlowColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectCrisis(crisis) }
                                            .testTag("crisis_choice_${crisis.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = crisis.title.uppercase(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) activeColor else TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = crisis.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                crisis.statTags.forEach { tag ->
                                                    val tagColor = when {
                                                        tag.contains("Ki") -> PowerKi
                                                        tag.contains("Malice") -> MaliceRed
                                                        tag.contains("Infamy") -> InfamyOrange
                                                        tag.contains("Health") -> HealthGreen
                                                        else -> activeColor
                                                    }
                                                    TagBadge(text = tag, color = tagColor)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        6, 7 -> {
                            // Complete and reset controls
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(alignmentColor.copy(alpha = 0.1f))
                                        .border(1.dp, alignmentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⚡ TIMELINE ARCHIVED • TRY NEW PATHS",
                                        fontWeight = FontWeight.Black,
                                        color = alignmentColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Button(
                                    onClick = { viewModel.regenerateReality() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SaiyanGold,
                                        contentColor = DeepSpaceBlack
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("regenerate_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("REGENERATE REALITY (RANDOM BOOST)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { viewModel.reset() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1D1414),
                                        contentColor = FeralCrimson
                                    ),
                                    border = BorderStroke(1.dp, FeralCrimson.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("reset_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = FeralCrimson)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("RESET MAP (START OVER)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FeralCrimson)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- UI COMPONENTS ----------------

@Composable
fun HeaderPanel(
    onReset: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131E)),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        border = BorderStroke(1.dp, BorderDark),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SaiyanGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DRAGON BALL Z",
                        style = MaterialTheme.typography.labelSmall,
                        color = SaiyanGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    text = "PROJECT KAKAROT",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Interactive Saiyan Destiny Simulator",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRegenerate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SaiyanGold,
                        contentColor = DeepSpaceBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .testTag("regenerate_button")
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Regenerate Reality",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REGENERATE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D1414),
                        contentColor = FeralCrimson
                    ),
                    border = BorderStroke(1.dp, FeralCrimson.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .testTag("reset_button")
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Engine",
                        modifier = Modifier.size(14.dp),
                        tint = FeralCrimson
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = FeralCrimson
                    )
                }
            }
        }
    }
}

@Composable
fun StatsHUD(
    stats: StatPoints,
    bonusTokens: Int,
    level: Int,
    xp: Int,
    maxXp: Int,
    timelineModifier: String,
    onAllocate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0E14)),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // RPG Level & XP Bar, Active Reality Aura Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kakarot Identity & RPG Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FeralCrimson)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "KAKAROT LEVEL $level",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // XP bar
                    Column(modifier = Modifier.width(130.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "XP PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$xp / $maxXp",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaiyanBlue,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { xp.toFloat() / maxXp.coerceAtLeast(1) },
                            color = SaiyanBlue,
                            trackColor = Color(0xFF1D2635),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                // Active Reality Aura badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF131722))
                        .border(1.dp, SaiyanGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SaiyanGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AURA: ${timelineModifier.substringBefore(":")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaiyanGold,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Description for timeline modifier if customized
            if (timelineModifier != "Normal Flow") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF07090D))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "✨ Reality Mod: ${timelineModifier.substringAfter(": ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            // Allocate stats header if tokens are available
            if (bonusTokens > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SaiyanGold.copy(alpha = 0.15f))
                        .border(1.dp, SaiyanGold, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "TOKENS: $bonusTokens",
                                style = MaterialTheme.typography.labelLarge,
                                color = SaiyanGold,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Spend to force story alignment!",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onAllocate("KI") },
                            colors = ButtonDefaults.buttonColors(containerColor = PowerKi),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+1 KI", color = DeepSpaceBlack, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onAllocate("MALICE") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaliceRed),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+1 MAL", color = DeepSpaceBlack, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onAllocate("INFAMY") },
                            colors = ButtonDefaults.buttonColors(containerColor = InfamyOrange),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+1 INF", color = DeepSpaceBlack, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onAllocate("HEALTH") },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+1 HP", color = DeepSpaceBlack, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatIndicator(
                    label = "KI STATE (Saiyan)",
                    value = stats.ki,
                    color = PowerKi,
                    emoji = "⚡",
                    modifier = Modifier.weight(1f)
                )
                StatIndicator(
                    label = "MALICE STATE (Feral)",
                    value = stats.malice,
                    color = MaliceRed,
                    emoji = "🩸",
                    modifier = Modifier.weight(1f)
                )
                StatIndicator(
                    label = "INFAMY STATE (Tyrant)",
                    value = stats.infamy,
                    color = InfamyOrange,
                    emoji = "👑",
                    modifier = Modifier.weight(1f)
                )
                StatIndicator(
                    label = "HEALTH STATE (Resilient)",
                    value = stats.health,
                    color = HealthGreen,
                    emoji = "💚",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatIndicator(
    label: String,
    value: Int,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value * 12f).coerceAtMost(100f) / 100f,
        animationSpec = tween(500),
        label = "StatProgress"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$emoji $value",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = color,
                trackColor = Color(0xFF1F2633),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun TrackingRail(
    currentStage: Int,
    state: GameState,
    onStageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stages = listOf(
        Pair(1, "Origin"),
        Pair(2, "Prologue"),
        Pair(3, "Bridge"),
        Pair(4, "Alignment"),
        Pair(5, "Crisis"),
        Pair(6, "Ending"),
        Pair(7, "Aftermath")
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F131E)),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stages.forEachIndexed { index, (stageId, name) ->
                // Determine completion
                val isCompleted = when (stageId) {
                    1 -> state.selectedOrigin != null
                    2 -> state.selectedChoice != null
                    3 -> state.selectedBridge != null
                    4 -> state.calculatedAlignment != null
                    5 -> state.selectedCrisis != null
                    6 -> state.calculatedEnding != null
                    7 -> state.calculatedAftermath != null
                    else -> false
                }
                val isActive = stageId == currentStage
                val isInteractable = isCompleted || stageId < currentStage

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = isInteractable) { onStageClick(stageId) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                when {
                                    isActive -> SaiyanGold
                                    isCompleted -> SaiyanBlue
                                    else -> Color(0xFF1D2635)
                                }
                            )
                    ) {
                        if (isCompleted && !isActive) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = DeepSpaceBlack,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "$stageId",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) DeepSpaceBlack else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isActive) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = SaiyanGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (index < stages.size - 1) {
                    Divider(
                        color = if (isCompleted) SaiyanBlue else Color(0xFF1D2635),
                        modifier = Modifier
                            .width(10.dp)
                            .height(1.dp)
                    )
                }
            }
        }
    }
}

sealed class NodeCardState {
    object Locked : NodeCardState()
    object Selectable : NodeCardState()
    data class Committed(val glowColor: Color) : NodeCardState()
}

@Composable
fun TagBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
    }
}

@Composable
fun LockedPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090B0F)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderDark),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked Stage",
                tint = TextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "STAGE LOCKED",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}

// ---------------- RETRO RPG ARENA COMPOSABLE ----------------

@Composable
fun RetroRpgArena(
    state: GameState,
    opponents: List<BattleOpponent>,
    onStartBattle: (BattleOpponent) -> Unit,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Subheader Info
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💥", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "THE COMBAT TRAINING FACILITY",
                        style = MaterialTheme.typography.labelLarge,
                        color = FeralCrimson,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Test Kakarot's strength against bosses. Combat power scales with your active timeline stats! Defeating an opponent grants +1 Bonus Stat Token to change story alignment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // 1. OPPONENT SELECTION PANEL (Full Width)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0C0E14))
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "CHOOSE AN OPPONENT",
                style = MaterialTheme.typography.labelLarge,
                color = SaiyanGold,
                fontWeight = FontWeight.Black
            )
            Divider(color = BorderDark)

            opponents.forEach { opponent ->
                val isSelected = state.activeOpponent?.name == opponent.name
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E1714) else DarkSlateCard
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) FeralCrimson else BorderDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartBattle(opponent) }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(opponent.avatarEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                opponent.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) FeralCrimson else TextPrimary
                            )
                            Text(
                                "HP: ${opponent.maxHp} • ATK: ${opponent.attack}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 2. ACTIVE BATTLE SCREEN PANEL (Full Width)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0C0E14))
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "COMBAT ARENA VIEWPORT",
                style = MaterialTheme.typography.labelLarge,
                color = SaiyanGold,
                fontWeight = FontWeight.Black
            )
            Divider(color = BorderDark)

            if (state.activeOpponent == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF06080C), RoundedCornerShape(8.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No active simulation",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextMuted
                        )
                        Text(
                            "Select an opponent above to start fighting!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                val opp = state.activeOpponent

                // Active Battle Screen (Visualizers & Progress indicators)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF06080C), RoundedCornerShape(8.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Health / Ki stats
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔥 KAKAROT", fontWeight = FontWeight.Black, color = SaiyanGold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("HP", style = MaterialTheme.typography.labelSmall, color = HealthGreen, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
                                LinearProgressIndicator(
                                    progress = { state.kakarotHp.toFloat() / state.kakarotMaxHp },
                                    color = HealthGreen,
                                    trackColor = Color(0xFF1D2635),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                            Text(
                                "${state.kakarotHp}/${state.kakarotMaxHp} HP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("KI", style = MaterialTheme.typography.labelSmall, color = PowerKi, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
                                LinearProgressIndicator(
                                    progress = { state.kakarotKi.toFloat() / state.kakarotMaxKi },
                                    color = PowerKi,
                                    trackColor = Color(0xFF1D2635),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                            Text(
                                "${state.kakarotKi}/${state.kakarotMaxKi} KI",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // VS Midpoint Divider
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(FeralCrimson)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("VS", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 11.sp)
                        }

                        // Opponent health status card
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("${opp.avatarEmoji} ${opp.name}", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { opp.hp.toFloat() / opp.maxHp },
                                    color = MaliceRed,
                                    trackColor = Color(0xFF1D2635),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("HP", style = MaterialTheme.typography.labelSmall, color = MaliceRed, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "${opp.hp}/${opp.maxHp} HP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Game Action Deck Buttons
                    if (state.isBattleOver) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (state.battleResult == "VICTORY") SaiyanGold.copy(alpha = 0.15f)
                                    else FeralCrimson.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (state.battleResult == "VICTORY") SaiyanGold else FeralCrimson,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (state.battleResult == "VICTORY") "✨ BATTLE VICTORY! ✨" else "💀 KAKAROT WAS DEFEATED",
                                    fontWeight = FontWeight.Black,
                                    color = if (state.battleResult == "VICTORY") SaiyanGold else FeralCrimson,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    if (state.battleResult == "VICTORY") "Gained +1 Bonus Stat Token. Select another boss or return to narrative map to spend your token!"
                                    else "Try adjusting your story selections to increase Health, Ki, or combat stats and fight again!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Roomy 2x2 Gamepad Layout Grid
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onAction("CHARGE") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141A29)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    border = BorderStroke(1.dp, PowerKi)
                                ) {
                                    Text("⚡ CHARGE", color = PowerKi, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onAction("MELEE") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1212)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    border = BorderStroke(1.dp, MaliceRed)
                                ) {
                                    Text("🩸 CLAW STRIKE", color = MaliceRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onAction("BEAM") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F1A1B)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    border = BorderStroke(1.dp, SaiyanBlue)
                                ) {
                                    Text("☄️ DODON RAY", color = SaiyanBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onAction("ROAR") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1424)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    border = BorderStroke(1.dp, TyrantPurple)
                                ) {
                                    Text("👑 APE ROAR", color = TyrantPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Retro arcade green console feed
                Text(
                    "COMBAT CONSOLE FEED",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFF020305), RoundedCornerShape(6.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.combatLogs.reversed().forEach { log ->
                        Text(
                            text = "> $log",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (log.contains("VICTORY") || log.contains("Gained")) Color(0xFF4AF626) else if (log.contains("DEFEAT")) Color(0xFFFF5F5F) else Color(0xFF39D1B4),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}
