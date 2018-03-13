# RecordingServer
## Description
This is a small REST server written in kotlin to save, and serve recorded paths from a robot. This is intended to be run on the driver station laptop, and the robot will request a path for autonomous. This then sends the JSON encoded path data over HTTP, and the robot executes that.

## Usage
To use this server, open this in your editor of choice (I use intellij), and type 
```gradlew run```
That runs the server on the default port, which is 5802.

## Paths
```
GET /data:
    Params: {"name": Name of the path to run}
    Returns: JSON object representing a path
GET /data/all
    Returns: Names of all saved paths
POST /data
    Params: A path object to be saved
```

## Path Object schema
```json
{
  "name": "pathSchema",
  "reversed": false,
  "elements": [
    {
      "timestamp": 0.0,
      "leftRPM": 0.0,
      "rightRPM": 0.0
    }
  ]
}
```