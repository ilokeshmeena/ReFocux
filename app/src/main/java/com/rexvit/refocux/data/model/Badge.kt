package com.rexvit.refocux.data.model

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconRes: String,
    val unlockCondition: (UserStats) -> Boolean
)

val allBadges = listOf(
    Badge(
        id = "first_pomodoro",
        name = "First Step",
        description = "Complete your first Pomodoro",
        iconRes = "badge_first",
        unlockCondition = { it.totalPomodoros >= 1 }
    ),
    Badge(
        id = "streak_3",
        name = "3-Day Streak",
        description = "Maintain a 3-day focus streak",
        iconRes = "badge_streak3",
        unlockCondition = { it.currentStreak >= 3 }
    ),
    Badge(
        id = "streak_7",
        name = "7-Day Streak",
        description = "Maintain a 7-day focus streak",
        iconRes = "badge_streak7",
        unlockCondition = { it.currentStreak >= 7 }
    ),
    Badge(
        id = "level_5",
        name = "Level 5 Achiever",
        description = "Reach level 5",
        iconRes = "badge_level5",
        unlockCondition = { it.level >= 5 }
    ),
    Badge(
        id = "marathon",
        name = "Marathon Runner",
        description = "Complete 100 Pomodoros",
        iconRes = "badge_100",
        unlockCondition = { it.totalPomodoros >= 100 }
    )
)