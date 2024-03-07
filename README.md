## Command usage:
```
/vb stats player [player] <stat> <args>
                           mining_speed_bonus <add/set> <value>
                           mining_speed_bonus_exception <add/set> <material> <value>
                           hardness_translation <material_from> <material_to>
                           empty_hand_tool <material>
                           aquaaffinity <true/false>
                           airaffinity <true/false
```
- mining_speed_bonus: Increases generic mining speed of a player on any block (scalar, 0.1 = +10% speed)
- mining_speed_bonus_exception: Increases mining speed of a player against a specific block (scalar, 0.1 = +10%)
- hardness_translation: Causes the player to mine the *from* block at the same speed as if they were mining the *to* block. For example, using DEEPSLATE and STONE will cause deepslate to be mined at the same speed as stone.
- empty_hand_tool: When the player has an empty hand, they will mine at the same speed as the given tool. For the command you can enter a material such as DIAMOND_PICKAXE, or `hand` to give them your currently held item as empty-hand-tool
- aquaaffinity: Allows the player to mine at regular speeds while swimming
- airaffinity: Allows the player to mine at regular speeds while not on solid ground


```
/vb stats item <stat> <args>
                       mining_speed_bonus <value>
                       mining_power <value>
                       mining_power_exception <material> <value>
                       hardness_translation <material_from> <material_to>
```
- mining_speed_bonus: Sets generic mining speed of this tool on any suitable block (scalar, 0.1 = +10% speed)
- mining_power: Sets the mining power of the tool (rather arbitrary, examples given below)
- mining_power_exception: Sets the mining power of the tool against specific blocks only
- hardness_translation: Causes the tool to mine the *from* block at the same speed as if it was mining the *to* block. For example, using DEEPSLATE and STONE will cause deepslate to be mined at the same speed as stone.



Tool power:<br>

Wooden tools: 2<br>
Stone tools: 4<br>
Iron tools: 6<br>
Diamond tools: 8<br>
Netherite tools: 9<br>
Golden tools: 12<br>

```
/vb give <tool>
```
- Gives you a custom tool special to the plugin. By default, there's only a stick that allows you to control the hardness of specific blocks

```
/vb hardness <block> <value>
```
- Changes the default hardness of the given block to the given value
















