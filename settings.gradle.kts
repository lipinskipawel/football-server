rootProject.name = "football-server"
include("auth")

include(":websocket:api")
project(":websocket:api").projectDir = file("websocket/api")

include(":websocket:app")
project(":websocket:app").projectDir = file("websocket/app")

include(":websocket:domain")
project(":websocket:domain").projectDir = file("websocket/domain")
