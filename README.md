  <h1>PosePlugin [Minecraft 1.15.2-1.16.3]</h1>
  <h3>Choose your favorite pose</h3>

[![Discord](https://img.shields.io/discord/720917120862519347?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discord.io/stumpstudio)
![img](http://badge.henrya.org/spigot/downloads?id=76990&color=green)
![img2](https://img.shields.io/spiget/version/76990?color=blue&label=current%20version)

<p align="left">PosePlugin - is powerful open-source plugin that can help you change your pose to whatever you want. <br> There are 6 different poses you're definitely gonna like: lying, sitting, swimming, waving, pointing, handshaking.</p>

## Video review about the plugin ##
In Netherlands/Dutch languages

[![Netherlands review](https://img.youtube.com/vi/VLg3LIp6QIY/0.jpg)](https://www.youtube.com/watch?v=VLg3LIp6QIY)

## Give it a try ##
Available at the following options:

* **[SpigotMC](https://www.spigotmc.org/resources/poseplugin-choose-your-favorite-pose-1-15-2-1-16-3.76990/)** - Download plugin on SpigotMC
* **[GitHub Releases](https://github.com/armagidon-exception/PosePlugin/releases)** - Download source code PosePlugin

## Changing pose

Main feature that PosePlugin provides is the pose changing

Firstly, you have to get a PosePluginPlayer instance, class which represents a player who can change his pose.
Use following code to get it:
```java
PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(Player);
```

Where Player is a player whose instance of PosePluginPlayer you want to get.

**Note**: PosePluginPlayer is **NOT** a Player, so you **CANNOT** cast it to Player class.

When we got our PosePluginPlayer instance we need to change his pose.

Use following code to do this:
```java
posePluginPlayer.changePose(IPluginPose pose);
```

Where IPluginPose is an object of the pose we want to set to player.

### IPluginPose

IPluginPose is class which represents the pose we want to set to player.

You could actually create an instance of pose and pass it to the method, but you can't customize it.
To create a customizable pose object, you have to use PoseBuilder.

To create one use following code:
```java
PoseBuilder.builder(EnumPose posetype)
```

EnumPose is an interface which represents type of pose. There's 6 standard types of pose:
SITTING, LYING, SWIMMING, WAVING, POINTING, HANDSHAKING.

To customize your pose you have to use the "option" method.
```java
PoseBuilder.builder(EnumPose).option(EnumPoseOption option, T value);
```
EnumPoseOption is an interface which represents option of the pose.

"value" is what value you want to set for this option

Here's options for different poses:
- Lay pose:
    - HEAD_ROTATION - enables head rotation of lying player
    - SWING_ANIMATION - enables swinging-hand animation of lying player
    - SYNC_EQUIPMENT - enables equipment synchronization with an original player of lying player
    - SYNC_OVERLAYS - enables overlays synchronization with an original player of lying player
    - INVISIBLE - toggles invisibility of lying player
    - VIEW_DISTANCE - changes distance between lying player and other player to see the lying player
- Wave pose:
    - HANDTYPE - changes hand player will be waving with
- Point pose:
    - HANDTYPE - changes hand player will be pointing with
- Handshake pose:
    - HANDTYPE - changes hand player will be handshaking with
Now after you customized your pose, you have to build it using "build" method:
```java
PoseBuilder.builder(EnumPose poseType).option(EnumPoseOption option, T value).build(Player player);
```

Player is a player you want to set pose of.

Here's example of how to change your player's pose.

```java
    public void someMeth(Player player){
        PosePluginPlayer posePluginPlayer = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player);
        IPluginPose pose = PoseBuilder.builder(EnumPose.WAVING).option(EnumPoseOption.HANDTYPE, HandType.LEFT).build(player);
        posePluginPlayer.changePose(pose);
    }
```

**NOTE**: If you try to change player's pose that was already set to player, changePose will throw IllegalArgumentException

## Events
PosePlugin API has some events to use.

### StopPosingEvent
*fired when player stops his pose*

- getPose() - gets pose player has stopped posing with
- getPlayer() - gets player who stopped posing
- isCancellable() is this event cancellable
- setCancelled(boolean cancelled) sets whether this event cancelled.

#### Cancellable StopPosingEvent

StopPosingEvent is cancellable, but not every stop posing can be canceled such as death, quit, toggling fly, getting in a boat, etc.
To check if this event cancellable you should use a isCancellable();

#### Stop posing
To stop player posing, you have to use "resetCurrentPose" method.
```java
posePluginPlayer.resetCurrentPose(boolean cancellable);
```

resetCurrentPose fires StopPosingEvent, cancellable is passing to StopPosingEvent, and can be got using isCancellable()

### PoseChangeEvent
*fired when player changes his pose*

- getPlayer() - gets player who changed his pose.
- getNewPose() - gets new pose
- setNewPose(IPluginPose newPose) - changes what pose should be set to the given player.
- getBefore() - gets pose type of pose that was before changing
- setCancelled(boolean cancelled) sets whether this event cancelled.

### PostPoseChangeEvent
*fired when player changed his pose*

- getPlayer() - gets player who changed his pose.
- getNewPose() - gets new pose 
