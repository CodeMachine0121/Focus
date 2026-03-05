package org.coding.afternoon.focus

/**
 * Hardcoded curated library of break activities.
 * Contains 24 activities across all five [BreakCategory] values.
 */
object BreakActivityLibrary {

    val allActivities: List<BreakActivity> = listOf(

        // ── Movement ─────────────────────────────────────────────────────────
        BreakActivity(
            id = "neck_roll",
            title = "Neck Roll",
            description = "Slowly roll your head in a full circle, 3 reps clockwise then 3 counter-clockwise. Keep shoulders relaxed.",
            category = BreakCategory.Movement,
            durationSeconds = 60,
            emoji = "🔄",
        ),
        BreakActivity(
            id = "shoulder_shrug",
            title = "Shoulder Shrug",
            description = "Raise both shoulders up toward your ears, hold for 3 seconds, then release fully. Repeat 5 times.",
            category = BreakCategory.Movement,
            durationSeconds = 45,
            emoji = "🤷",
        ),
        BreakActivity(
            id = "desk_pushup",
            title = "Desk Push-Up",
            description = "Place hands on desk edge, shoulder-width apart. Perform 10 incline push-ups to activate chest and arms.",
            category = BreakCategory.Movement,
            durationSeconds = 60,
            emoji = "💪",
        ),
        BreakActivity(
            id = "wrist_stretch",
            title = "Wrist Stretch",
            description = "Extend one arm palm-up and gently pull the fingers back with the other hand. Hold 15 seconds, then switch hands.",
            category = BreakCategory.Movement,
            durationSeconds = 45,
            emoji = "🤲",
        ),
        BreakActivity(
            id = "standing_calf_raise",
            title = "Calf Raise",
            description = "Stand up and rise slowly onto your tiptoes, then lower. Do 15 reps to boost circulation in your legs.",
            category = BreakCategory.Movement,
            durationSeconds = 60,
            emoji = "🦵",
        ),
        BreakActivity(
            id = "spine_twist",
            title = "Seated Spine Twist",
            description = "Sit tall and gently twist your torso to the right, holding the chair back for 15 seconds. Repeat on the left.",
            category = BreakCategory.Movement,
            durationSeconds = 45,
            emoji = "🌀",
        ),

        // ── Eye Rest ──────────────────────────────────────────────────────────
        BreakActivity(
            id = "twenty_twenty",
            title = "20-20-20 Rule",
            description = "Look at something at least 20 feet away for 20 seconds. This relaxes the ciliary muscles that focus your lens.",
            category = BreakCategory.EyeRest,
            durationSeconds = 30,
            emoji = "👁️",
        ),
        BreakActivity(
            id = "eye_palming",
            title = "Eye Palming",
            description = "Rub your palms together until warm, then cup them gently over your closed eyes. Enjoy 30 seconds of complete darkness.",
            category = BreakCategory.EyeRest,
            durationSeconds = 45,
            emoji = "🖐️",
        ),
        BreakActivity(
            id = "blink_reset",
            title = "Blink Reset",
            description = "Blink rapidly 20 times to re-lubricate your eyes, then close them gently and rest for 10 seconds.",
            category = BreakCategory.EyeRest,
            durationSeconds = 40,
            emoji = "👀",
        ),
        BreakActivity(
            id = "figure_eight_eyes",
            title = "Figure-Eight Eyes",
            description = "Trace an imaginary figure-8 pattern on the wall with your eyes. Go slowly — 3 rounds in each direction.",
            category = BreakCategory.EyeRest,
            durationSeconds = 60,
            emoji = "8️⃣",
        ),
        BreakActivity(
            id = "near_far_focus",
            title = "Near-Far Focus Shift",
            description = "Hold a finger 6 inches from your nose. Alternate focus between your finger and a distant object, 10 times.",
            category = BreakCategory.EyeRest,
            durationSeconds = 45,
            emoji = "🔭",
        ),

        // ── Mindfulness ───────────────────────────────────────────────────────
        BreakActivity(
            id = "box_breathing",
            title = "Box Breathing",
            description = "Inhale for 4 counts, hold for 4 counts, exhale for 4 counts, hold for 4 counts. Repeat 4 full cycles.",
            category = BreakCategory.Mindfulness,
            durationSeconds = 90,
            emoji = "🌬️",
        ),
        BreakActivity(
            id = "body_scan",
            title = "1-Minute Body Scan",
            description = "Close your eyes and mentally scan from scalp to toes, noticing — without judging — any tension you find.",
            category = BreakCategory.Mindfulness,
            durationSeconds = 60,
            emoji = "🧘",
        ),
        BreakActivity(
            id = "gratitude_pause",
            title = "Gratitude Pause",
            description = "Think of three specific things you are genuinely grateful for right now. Let each one land before moving to the next.",
            category = BreakCategory.Mindfulness,
            durationSeconds = 60,
            emoji = "🙏",
        ),
        BreakActivity(
            id = "mindful_breath",
            title = "Mindful Breath Count",
            description = "Focus solely on your breath. Count each exhale — 1, 2, 3… up to 10 — then start again. No multitasking.",
            category = BreakCategory.Mindfulness,
            durationSeconds = 90,
            emoji = "🌊",
        ),
        BreakActivity(
            id = "five_senses",
            title = "5-4-3-2-1 Grounding",
            description = "Name 5 things you see, 4 you hear, 3 you can touch, 2 you smell, and 1 you taste. Pulls you into the present moment.",
            category = BreakCategory.Mindfulness,
            durationSeconds = 120,
            emoji = "✨",
        ),

        // ── Hydration ─────────────────────────────────────────────────────────
        BreakActivity(
            id = "drink_water",
            title = "Drink Water",
            description = "Stand up, walk to the kitchen, and drink a full glass of water. Staying upright for 60 seconds also helps circulation.",
            category = BreakCategory.Hydration,
            durationSeconds = 60,
            emoji = "💧",
        ),
        BreakActivity(
            id = "healthy_snack",
            title = "Healthy Snack",
            description = "Grab a piece of fruit or a small handful of nuts. Eat away from your screen and chew slowly.",
            category = BreakCategory.Hydration,
            durationSeconds = 120,
            emoji = "🍎",
        ),
        BreakActivity(
            id = "tea_break",
            title = "Tea Break",
            description = "Brew a cup of herbal tea. Use the 3-minute steeping time to stay away from all screens.",
            category = BreakCategory.Hydration,
            durationSeconds = 180,
            emoji = "🍵",
        ),
        BreakActivity(
            id = "refill_reminder",
            title = "Refill Your Bottle",
            description = "Take your water bottle to the sink and refill it. Setting a full bottle on your desk is a visible hydration cue.",
            category = BreakCategory.Hydration,
            durationSeconds = 45,
            emoji = "🫙",
        ),

        // ── Creative ──────────────────────────────────────────────────────────
        BreakActivity(
            id = "journal_prompt",
            title = "Journal Prompt",
            description = "Write 3 sentences in a notebook: what you are working on, what the next concrete step is, and how you feel about it.",
            category = BreakCategory.Creative,
            durationSeconds = 120,
            emoji = "📓",
        ),
        BreakActivity(
            id = "quick_sketch",
            title = "Quick Sketch",
            description = "Grab a pen and sketch the first object you see. No artistic skill needed — just observe shape and shadow for 2 minutes.",
            category = BreakCategory.Creative,
            durationSeconds = 120,
            emoji = "✏️",
        ),
        BreakActivity(
            id = "window_gaze",
            title = "Window Gaze",
            description = "Look out a window and invent a brief backstory for the first person or object you see. Let your imagination wander.",
            category = BreakCategory.Creative,
            durationSeconds = 60,
            emoji = "🪟",
        ),
        BreakActivity(
            id = "desk_tidy",
            title = "Desk Tidy",
            description = "Spend 90 seconds organizing one small section of your physical desk. A clearer space often clears the mind too.",
            category = BreakCategory.Creative,
            durationSeconds = 90,
            emoji = "🗂️",
        ),
    )
}
