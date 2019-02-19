package gg.rsmod.plugins.content.combat

import gg.rsmod.game.model.Tile
import gg.rsmod.game.model.attr.COMBAT_TARGET_FOCUS_ATTR
import gg.rsmod.game.model.combat.CombatClass
import gg.rsmod.game.model.combat.PawnHit
import gg.rsmod.game.model.entity.Pawn
import gg.rsmod.game.model.entity.Projectile
import gg.rsmod.game.model.timer.ACTIVE_COMBAT_TIMER
import gg.rsmod.game.plugin.Plugin
import gg.rsmod.plugins.api.ProjectileType
import gg.rsmod.plugins.api.ext.hit
import gg.rsmod.plugins.content.combat.formula.CombatFormula
import gg.rsmod.util.AabbUtil

/**
 * @author Tom <rspsmods@gmail.com>
 */

fun Pawn.isAttacking(): Boolean = attr[COMBAT_TARGET_FOCUS_ATTR]?.get() != null

fun Pawn.isBeingAttacked(): Boolean = timers.has(ACTIVE_COMBAT_TIMER)

fun Pawn.getCombatTarget(): Pawn? = attr[COMBAT_TARGET_FOCUS_ATTR]?.get()

fun Pawn.removeCombatTarget() = attr.remove(COMBAT_TARGET_FOCUS_ATTR)

fun Pawn.canEngageCombat(target: Pawn): Boolean = Combat.canEngage(this, target)

fun Pawn.canAttack(target: Pawn, combatClass: CombatClass): Boolean = Combat.canAttack(this, target, combatClass)

fun Pawn.isAttackDelayReady(): Boolean = Combat.isAttackDelayReady(this)

fun Pawn.combatRaycast(target: Pawn, distance: Int, projectile: Boolean): Boolean = Combat.raycast(this, target, distance, projectile)

suspend fun Pawn.canAttackMelee(it: Plugin, target: Pawn, moveIfNeeded: Boolean): Boolean =
        AabbUtil.areBordering(tile.x, tile.z, getSize(), getSize(), target.tile.x, target.tile.z, target.getSize(), target.getSize())
                || moveIfNeeded && moveToAttackRange(it, target, distance = 0, projectile = false)

fun Pawn.dealHit(target: Pawn, formula: CombatFormula, delay: Int, onHit: (PawnHit) -> Unit = {}): PawnHit {
    val accuracy = formula.getAccuracy(this, target)
    val maxHit = formula.getMaxHit(this, target)
    val landHit = accuracy >= world.randomDouble()

    val hit = if (landHit) {
        target.hit(damage = world.random(maxHit), delay = delay)
    } else {
        target.hit(damage = 0, delay = delay)
    }

    val pawnHit = PawnHit(hit, landHit)
    hit.addAction { onHit(pawnHit) }.setCancelIf { this.isDead() }
    return pawnHit
}

suspend fun Pawn.moveToAttackRange(it: Plugin, target: Pawn, distance: Int, projectile: Boolean): Boolean = Combat.moveToAttackRange(it, this, target, distance, projectile)

fun Pawn.postAttackLogic(target: Pawn) = Combat.postAttack(this, target)

fun Pawn.createProjectile(target: Tile, gfx: Int, type: ProjectileType, endHeight: Int = -1): Projectile {
    val builder = Projectile.Builder()
            .setTiles(start = tile, target = target)
            .setGfx(gfx = gfx)
            .setHeights(startHeight = type.startHeight, endHeight = if (endHeight != -1) endHeight else type.endHeight)
            .setSlope(angle = type.angle, steepness = type.steepness)
            .setTimes(delay = type.delay, lifespan = type.delay + Combat.getProjectileLifespan(this, target, type))

    return builder.build()
}

fun Pawn.createProjectile(target: Pawn, gfx: Int, type: ProjectileType, endHeight: Int = -1): Projectile {
    val builder = Projectile.Builder()
            .setTiles(start = tile, target = target)
            .setGfx(gfx = gfx)
            .setHeights(startHeight = type.startHeight, endHeight = if (endHeight != -1) endHeight else type.endHeight)
            .setSlope(angle = type.angle, steepness = type.steepness)
            .setTimes(delay = type.delay, lifespan = type.delay + Combat.getProjectileLifespan(this, target.tile, type))

    return builder.build()
}