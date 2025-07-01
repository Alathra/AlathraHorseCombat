<h1 style="text-align:center;">AlathraHorseCombat (Gradle)</h1>
<p style="text-align:center;">
    <img alt="GitHub License" src="https://img.shields.io/github/license/Alathra/Template-Gradle-Plugin?style=for-the-badge&color=blue&labelColor=141417">
    <img alt="GitHub Downloads (all assets, all releases)" src="https://img.shields.io/github/downloads/Alathra/Template-Gradle-Plugin/total?style=for-the-badge&labelColor=141417">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/Alathra/Template-Gradle-Plugin?include_prereleases&sort=semver&style=for-the-badge&label=LATEST%20VERSION&labelColor=141417">
    <img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/Alathra/Template-Gradle-Plugin/ci.yml?style=for-the-badge&labelColor=141417">
    <img alt="GitHub Issues or Pull Requests" src="https://img.shields.io/github/issues/Alathra/Template-Gradle-Plugin?style=for-the-badge&labelColor=141417">
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/Alathra/Template-Gradle-Plugin?style=for-the-badge&labelColor=141417">
</p>

---

## Description

AlathraHorseCombat introduces enhanced horse combat, by introducing increased damage from specific weapons on horseback via momentum mechanics. Works with Towny. This plugin is heavily inspired by HorsecombatRevamped, with majority of the code only translated from kotlin to java, _[link to original work](https://github.com/SakyQr/HorsecombatRevamped/tree/main/src/main)_.

---

## Usage and Configs

Lances and Pikes (configured under itemIdList), is used as the primary weapons for AlathraHorseCombat. Once the player rides a horse, the momentum bar can be seen above the player's hotbar. Momentum determines what the final damage output will be.

**Combat Configs**
- `mobDamageMultiplier` Damage multipler on mobs, on top of momentum damage multiplier. Default is 1.5
- `knockoffThreshold` Momentum threshold that knocks the targetted player off their horse. Default is 50
- `footDamage` Damage multiplier if attacker is not on a horse and is using a lance/pike. Default is 0.5
- `footDamageMobs` Damage multipler if attacker is not on a horse, is using a lance/pike and has damaged a mob entity. Default is 1.0
- `slownessDuration` Duration of slowness applied on the attacker if not on a horse and is using a lance/pike. Default is 100 seconds
- `slownessLevel` Level of slowness applied on the attacker if not on a horse and is using a lance/pike. Default is 1

**Momentum Configs**
- `movementThreshold` Distance in blocks the horse/player has moved within the configured stallTimeMs, used to check if the horse has not moved within a set amount of time in a set amount of distance. Default is 0.05
- `stallTimeMs` Time in milliseconds the horse has stood still (or moved a little), used in conjunction with movementThreshold, i.e: decrease momentum when horse has not moved 0.05 blocks in 500ms. Default is 0.5s or 500ms
- `maxDecayRate` Maximum momentum decay rate when horse is standing still. Default is 20 per stallTimeMs units, i.e. max is 20 per 500ms
- `turnThreshold` The minimum threshold yaw before the plugin determines the horse has turned "sharply", therefore decrease momentum. Default is 30 degrees
- `turnLoss` Momentum loss per sharp turn. Default is 10
- `straightGain` Momentum gain when horse is moving straight. Default is 1
- `momentum_100` Damage multiplier when momentum is at max. Default is 2.5
- `momentum_75-99` Damage multiplier when momentum is at 75 to 99. Default is 2
- `momentum_50-74` Damage multiplier when momentum is at 50 to 74. Default is 1.5
- `momentum_25-49` Damage multiplier when momentum is at 25 to 49. Default is 1
- `momentum_0-24` Damage multiplier when momentum is at 0 to 24. Default is 0.5

---

## Hooks and Compatibility
**Optional Hooks**
- [Towny](https://github.com/TownyAdvanced/Towny)
  If Towny is enabled, it will check whether or not the attacking player is a member of the town the damaging entity is in, check if the said town is in a war, check if the town has PvP enabled, and check if the town has damage mobs enabled.

- [ItemsAdder](https://itemsadder.devs.beer/), [Nexo](https://docs.nexomc.com/), [Oraxen](https://oraxen.com/)
  The above plugins are used to create and manage custom items. AlathraHorseCombat has a section in the config that can add custom items that can be used in the plugin.

AlathraHorseCombat has no commands.

---

## Permissions

AlathraHorseCombat contains the following permission nodes:
- `horsecombat.admin.townybypass` Grants the user towny bypass for horsecombat, allowing them to damage any player or entity within a towny claim, regardless if the user is a member of the town or not.

---

## Commands

AlathraHorseCombat has no commands.

---

## Configuration

AlathraHorseCombat can be configured by editing values in the config.yml. AlathraHorseCombat does not have a reload command because it automatically checks for updates in the config file. When you make edits to the file the changes will be applied immediately.

---
