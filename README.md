# PosePlugin
PosePluginPlayer is class to interact with player's poses.
If you want to change pose you should use:
```
PosePluginPlayer#changePose(EnumPose);
```
EnumPose is enum which contains all available poses.
## Events
When you use changePose method, PoseChangeEvent fires.
Using it you can set which pose will be sat, cancel event, enable/disable logging.

When animation stops, StopAnimationEvent fires.
Using it you can cancel event, or enable/disable logging.

## Config
You can also use plugin's config using:
```
ConfigurationManager.addSetting(String path, Object value);
```
And get values from it using:
```
Object get(String path);

String getString(String path);

boolean getBoolean(String path);
```
