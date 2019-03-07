package gg.rsmod.plugins.content.areas.edgeville.chat

on_npc_option(npc = Npcs.ABBOT_LANGLEY, option = "talk-to") {
    player.queue { dialog(this) }
}

suspend fun dialog(it: Plugin) {
    it.chatNpc("Greetings traveller.", animation = 588)
    when (it.options("Can you heal me? I'm injured.", "Isn't this place built a bit out of the way?")) {
        1 -> {
            it.chatPlayer("Can you heal me? I'm injured.", animation = 554)
            it.chatNpc("Ok.", animation = 588)
            heal(it.player)
            it.messageBox("Abbot Langley places his hands on your head. You feel a little better.")
        }
        2 -> {
            it.chatPlayer("Isn't this place built a bit out of the way?", animation = 554)
            it.chatNpc("We like it that way actually! We get disturbed less. We<br>still get rather a large amount of travellers looking for<br>sanctuary and healing here as it is!", animation = 590)
        }
    }
}

fun heal(p: Player) {
    if (p.getSkills().getCurrentLevel(Skills.HITPOINTS) < p.getSkills().getMaxLevel(Skills.HITPOINTS)) {
        p.getSkills().setCurrentLevel(Skills.HITPOINTS, p.getSkills().getMaxLevel(Skills.HITPOINTS))
    }
}