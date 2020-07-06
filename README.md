# PosePlugin
##PosePluginAPI
It's a main class of API. Through 
```PosePluginAPI.getAPI()```
You can get all API utils.

###PosePluginPlayerMap
aka *P3Map* used to get *PosePluginPlayer*
use ```PosePluginAPI.getAPI().getPlayerMap()``` to get PlayerMap
###PosePluginPlayer
PosePluginPlayer is class to interact with player's poses.
If you want to change pose you should use:
```
PosePluginPlayer#changePose(EnumPose);
```
To get it you need to use:
```P3Map#getPosePluginPlayer(String)```
Put name of the player as argument
###EnumPose
EnumPose is enum which contains all available poses.
### Events
When you use changePose method, PoseChangeEvent fires.
Using it you can set which pose will be sat, cancel event
```
@EventHandler
public void onEvent(PoseChangeEvent event){
	//TODO pose change handling
}
```
When animation stops, StopAnimationEvent fires.
Using it you can cancel event
```
@EventHandler
public void onEvent(StopAnimationEvent event){
	//TODO animation stop handling
}
```
##API mode
If you don't need a plugin function but just API, you can just disable it
```HandlerList.unregisterAll(PosePlugin.getInstance().getListener())```