
<div style="align: center;">
<h1>AlathraHorseCombat (Gradle)</h1>
    <img alt="GitHub License" src="https://img.shields.io/github/license/Alathra/Template-Gradle-Plugin?style=for-the-badge&color=blue&labelColor=141417">
    <img alt="GitHub Downloads (all assets, all releases)" src="https://img.shields.io/github/downloads/Alathra/Template-Gradle-Plugin/total?style=for-the-badge&labelColor=141417">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/Alathra/Template-Gradle-Plugin?include_prereleases&sort=semver&style=for-the-badge&label=LATEST%20VERSION&labelColor=141417">
    <img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/Alathra/Template-Gradle-Plugin/ci.yml?style=for-the-badge&labelColor=141417">
    <img alt="GitHub Issues or Pull Requests" src="https://img.shields.io/github/issues/Alathra/Template-Gradle-Plugin?style=for-the-badge&labelColor=141417">
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/Alathra/Template-Gradle-Plugin?style=for-the-badge&labelColor=141417">
</div>

---

## Description

AlathraHorseCombat is a modernized, highly configurable and Java-implemented fork of [HorseCombatRevamped](https://github.com/SakyQr/HorsecombatRevamped?tab=AGPL-3.0-1-ov-file) by [SakyQ](https://github.com/SakyQr). AlathraHorseCombat is ShermansWorld's take on horse combat mechanics that are designed for the [Alathra Minecraft Server](https://alathra.com/), now overhauled and generalized for public use. It adds configurable items referred to as "lances" that facilitate the horse combat system. When riding a horse and holding a lance item in their hand, a player is able to build up momentum and deliver a powerful strike to mobs and other players. This plugin is designed simmulate medieval-style cavalry charges and jousting as a standalone software. Capability is enhanced by adding support for widely used item framework plugins including [ItemsAdder](https://itemsadder.devs.beer/), [MMOItems](https://gitlab.com/phoenix-dvpmt/mmoitems/-/wikis/home) [Nexo](https://docs.nexomc.com/), and [Oraxen](https://oraxen.com/) for the registration of lance items.

---

## Usage and Configs

Lances and Pikes (configured under itemIdList), is used as the primary weapons for AlathraHorseCombat. Once the player rides a horse, the momentum bar can be seen above the player's hotbar. Momentum determines what the final damage output will be.

# Combat Configs
- `playerDamageMultiplier` Damage multipler on players, on top of momentum damage multiplier.
    Default is default is set to 1.0
- `mobDamageMultiplier` Damage multipler on mobs, on top of momentum damage multiplier.
    Default is set to 1.0
- `knockoffThreshold` Momentum threshold that knocks the targetted player off their horse. 
    Default is set to 50
- `knockoffChance` The chance that a lance hit will knock another player off their horse if the knockoffThreshold is exceeded.
    Default is set to 0.2
- `knockbackPlayers` The chance that the spear can deliver a knockback effect on players, proportional to momentum.
    The default is set to True.
-  `knockbackMobs` The chance that the spear can deliver a knockback effect on mobs, proportional to momentum.
    The default is set to True
-   `knockbackThreshold` The minimum momentum needed to deliver a knockback effect with a lance strike (hit).
    The default for this is set to 25.
-   `knockbackMultiplier` A multiplier that impacts the velocity of the knockback effect.
    The default for this is set to 1.0.

    ![LanceStrike](https://github.com/Alathra/AlathraHorseCombat/blob/main/docs/assets/LanceStrike.gif "Lance Strike")

# Momentum Configs
- `baseGain` How much momentum is gained when the horse is moving in a straight line, per player move event fired. 
    The default for this is set to 2.

# Stall Mechanics
    **Once a horse is considered stalling it will experience momentum decay.**
![MomentumMechanic](https://github.com/Alathra/AlathraHorseCombat/blob/main/docs/assets/MomentumMechanic.gif "Momentum Mechanic")
- `stallTimeSeconds` The time in seconds used in conjunction with stallCancelDistance to determine if a horse is stalling. 
    The default for this is set to .5.
- `stallCancelDistance` The distance (blocks traveled) that the horse must move within stallTimeSeconds, before it stalls.
    The default is set to 1.25
- `maxDecayRate` The maximum rate of decay of momentum as a result of turning or stalling
 The default for this is set to 20.
- `turnMinDegrees` The minimum threshold yaw before the plugin determines the horse has turned, therefore decrease momentum.
    Default is set to 50 degrees.
- `turnLoss` Momentum loss per sharp turn.
    Default is 15
![TurnLoss](https://github.com/Alathra/AlathraHorseCombat/blob/main/docs/assets/TurnMomentumMechanic.gif "Turn Mechanic")
# Damage Multipliers
- `momentum_100` Damage multiplier when momentum is at max. Default is 2.5
- `momentum_75-99` Damage multiplier when momentum is at 75 to 99. Default is 2
- `momentum_50-74` Damage multiplier when momentum is at 50 to 74. Default is 1.5
- `momentum_25-49` Damage multiplier when momentum is at 25 to 49. Default is 1
- `momentum_0-24` Damage multiplier when momentum is at 0 to 24. Default is 0.5

# Sound Settings
## *Hit*
    You can changed the sound effects here 
#### [Sounds Effects](https://www.digminecraft.com/lists/sound_list_pc.php)
- `enabled` The option to turn the particles off and on.
    The default for this is set to true
- `effect` "minecraft:entity.zombie.attack_iron_door"
- `volume` Default is set for .5
- `pitch` How far the sound can be heard. If this is not set it will sound across the world.
    Default is set to 32.
- `range` Damage multiplier when momentum is at 0 to 24. 
    Default is 0.5
- `minMomentum` Minimum momentum required for the  hit sound to be produced
    This is default set to 25.

# Particle Settings

## *Hit*
You can changed the particle effects here 
#### - [Particle Effects]( https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html)
- `enabled` The option to turn the particles off and on.
    The default for this is set to true
- `type` Crit
- `amount` The amount of particles that will spawn on hit. 
    The default is set to 20.
- `spread` Damage multiplier when momentum is at 0 to 24. 
    Default is 1.
- `minMomentum` Minimum momentum required for the  hit sound to be produced
    This is default set to 25.
---

## Hooks and Compatibility
**Optional Hooks**
- [Towny](https://github.com/TownyAdvanced/Towny)
  If Towny is enabled, it will check whether or not the attacking player is a member of the town the damaging entity is in, check if the said town is in a war, check if the town has PvP enabled, and check if the town has damage mobs enabled.

- [ItemsAdder](https://itemsadder.devs.beer/), [Nexo](https://docs.nexomc.com/), [Oraxen](https://oraxen.com/), [MmoItems](https://www.spigotmc.org/wiki/mmoitems-wiki/)
  The above plugins are used to create and manage custom items. AlathraHorseCombat has a section in the config that can add custom lance items that can be used in the plugin.


---

## Permissions

AlathraHorseCombat contains the following permission nodes:
- `alathrahorsecombat.admin` Grants the user towny bypass for horsecombat, allowing them to damage any player or entity within a towny claim, regardless if the user is a member of the town or not.

---

## Commands

AlathraHorseCombat has two current commands
- `reload` This command reloads the Horse combat plugin
- `getlances` This command will give you all of the lances from the AlathraHorseCombat plugin
![GetLanceCommand](https://github.com/Alathra/AlathraHorseCombat/blob/main/docs/assets/GetLancesCommand.gif "Get Lances Command")
---
