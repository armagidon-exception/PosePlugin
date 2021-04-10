  <h1>PosePlugin [Minecraft 1.15.2-1.16.5]</h1>
  <h3>Choose your favorite pose</h3>

[![Discord](https://img.shields.io/discord/720917120862519347?color=%237289DA&label=Discord&logo=discord&logoColor=white)](https://discord.io/stumpstudio)
![img](http://badge.henrya.org/spigot/downloads?id=76990&color=green)
![img2](https://img.shields.io/spiget/version/76990?color=blue&label=current%20version)

<p align="left">PosePlugin - is powerful open-source plugin that can help you change your pose to whatever you want. <br> There are 7 different poses you're definitely gonna like: lying, sitting, swimming, waving, pointing, handshaking and praying.</p>

## Video review about the plugin ##
In English

[![English review](https://img.youtube.com/vi/8ls495n5uY8/0.jpg)](https://www.youtube.com/watch?v=8ls495n5uY8)

In Netherlands/Dutch languages

[![Netherlands review](https://img.youtube.com/vi/VLg3LIp6QIY/0.jpg)](https://www.youtube.com/watch?v=VLg3LIp6QIY)

## Give it a try ##
Available at the following options:

* **[SpigotMC](https://www.spigotmc.org/resources/poseplugin-choose-your-favorite-pose-1-15-2-1-16-3.76990/)** - Download plugin on SpigotMC
* **[GitHub Releases](https://github.com/armagidon-exception/PosePlugin/releases)** - Download source code PosePlugin

## Changing pose

Main feature that PosePlugin provides is the pose changing

Firstly, you have to get a PosePluginPlayer instance.

### PosePluginPlayer
PosePluginPlayer is the class which represents a player who can change his pose.
Use following code to get it:
```java
PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(Player player);
```

Where "player" is a player, whose instance of PosePluginPlayer you want to get.

Methods of PosePluginPlayer you need:

- void changePose(IPluginPose pose) - changes pose of player
- void resetCurrentPose() - resets the current pose of player and fires StopPosingEvent
- Player getHandle() - gets Player instance
- IPluginPose getPose() - gets current pose object
- EnumPose - gets current pose type

**Note**: PosePluginPlayer is **NOT** a Player, so you **CANNOT** cast it to Player class.
If you want to get Player instance use:
```java
Player player = posePluginPlayer.getHandle();
```

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
    - VIEW_DISTANCE - distance between lying player and other player that is needed to display lying player
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
- setCancelled(boolean cancelled) sets whether this event cancelled.

#### Stop posing
To stop player posing, you have to use "resetCurrentPose" method.
```java
posePluginPlayer.resetCurrentPose();
```
This method throws StopPosingEvent and can be canceled.

```java
@EventHandler
public void onStop(StopPosingEvent event){
    if (event.getPose().getType().equals(EnumPose.LYING)){
        event.setCancelled(true);
    }
}
```

### PoseChangeEvent
*fired when player changes his pose*

- getPlayer() - gets player who changed his pose.
- getNewPose() - gets new pose
- setNewPose(IPluginPose newPose) - changes what pose should be set to the given player.
- getBefore() - gets pose type of pose that was before changing
- setCancelled(boolean cancelled) sets whether this event cancelled.

```java
@EventHandler
public void onPoseChange(PoseChangeEvent event){
    if (event.getNewPose().getType().equals(EnumPose.SWIMMING)){
        event.setCancelled(true);
    }
}
```

### PostPoseChangeEvent
*fired when player changed his pose*

- getPlayer() - gets player who changed his pose.
- getNewPose() - gets new pose

```java
@EventHandler
public void onPostPoseChange(PostPoseChangeEvent event){
     if (event.getNewPose().getType().equals(EnumPose.WAVING)){
        event.getPlayer().getHandle().sendMessage("Wave!");
     }
}
```
