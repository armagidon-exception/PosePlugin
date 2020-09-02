# PosePlugin

PosePlugin is powerful open-source plugin that can help you change your pose to whatever you want. There are 6 different poses you're definitely gonna like: lying, sitting, swimming, waving, pointing, handshaking.

## PosePluginAPI
```PosePluginAPI``` class has access for following classes:

1. ```ArmorHider``` - hides armor of the player using special NBT tag: ```PosePluginItem:ARMOR```
2. ```PlayerHider``` - hides player from others using packets
3. ```NameTagHider``` - hides player's name tag from himself(uses to hide NPC's name-tag. Also, disables collision with npc)
4. ```TickModuleManager``` - ticking utility
5. ```PersonalHandlerList``` - calls events for the player specifically.
6. ```NMSFactory``` - creates some classes which are using NMS
7. ```P3Map``` - contains all PosePluginPlayer instances



## P3Map
```P3Map``` contains all PosePluginPlayer instances. To get one:
1. Get P3Map instance: ```PosePluginAPI.getAPI().getPlayerMap()```
2. Get PosePluginPlayer ```P3Map#getPosePluginPlayer(String playerName)```
If you need to check if following player has PosePluginPlayer instance, use:
```P3Map#contains(Player player)```

You can also forEach all players:
```P3Map#forEach()```
### PosePluginPlayer
PosePluginPlayer has all data of player's poses.
To get type of his pose use:
```PosePluginPlayer#getPoseType()```

This method returns ```EnumPose``` class which is representing type of player's pose.

To get instance of his pose use

```PosePluginPlayer#getPose()```

This method returns ```IPluginPose``` interface which is representing player's pose, its implementors contains all data and all functionality

#### Pose changing
To change player's pose there're 2 methods:

```java 
PosePluginPlayer#changePose(EnumPose);
```
 - changes pose to provided pose type

``` java
PosePluginPlayer#changePose(EnumPose, boolean);
``` 
 - changes pose to provided pose type with API mode activation

##### API mode
API mode is when poses don't react to main plugin event handlers

## NMSFactory
NMSFactory creates utilities which are using NMS such as:
1. ```ItemUtil``` - uses to add or remove NBTTags
2. ```FakePlayer``` - can create NPC.

## Personal events
Personal event are event which are calling to one player specifically.
To listen those you need to subscribe a player to ```PersonalListener```.
To do this you need to get ```PersonalHandlerList``` instance in ```PosePluginAPI```.
For subscribing player to PersonalListener use:

```java 
PersonalHandlerList#subscribe(PosePluginPlayer, PersonalListener)
```

```PersonalListener``` is an interface of every personal listeners.

To unsubscribe the player from listener use:

```java 
PersonalHandlerList#unsubscribe(PosePluginPlayer, PersonalListener);
```

To tell PersonalHandlerList that this method needs to be listen, mark it with ```@PersonalEventHandler``` annotation

Here's events that can be listened:
1. PlayerMoveEvent
2. EntityDamageEvent
3. PlayerInteractAtEntityEvent
4. PlayerGameModeChangeEvent
5. PlayerItemConsumeEvent
6. PlayerInteractEvent
7. PlayerToggleSneakEvent
8. PlayerDeathEvent
9. BlockBreakEvent
10. PlayerTeleportEvent
11. PlayerArmorChangeEvent

## Events
PosePlugin API has some events:
1. PoseChangeEvent
2. PosePoseChangeEvent
3. StopAnimationEvent
4. HandTypeChangeEvent
5. PlayerArmorChangeEvent(made for spigot support, but also works with paper)

### PoseChangeEvent
This event fires when player is changing his pose. Can be cancelled, pose can be set.
### PostPoseChangeEvent
This event fires when player changed his pose.
### HandTypeChangeEvent
Fires when player changes hand type of experimental pose
### StopAnimationEvent
This event fires when player stops his animation
#### StopCause
STOPPED - Called when animation has been stopped by user

QUIT - Called when animation has been stopped because of the player's quit (you can't cancel it)

##### Custom stop causes
If you need to specify why exactly animation has been stopped, you can use overloaded method
```java
PluginPose.callStopEvent(EnumPose pose, PosePluginPlayer player, StopAnimationEvent.StopCause cause, String custom);
```
Then in StopAnimationEvent handler use:
```java
event.getCustomCause();
```
to get your cause
