rootProject.name = "football-server"

include(":auth:app")
project(":auth:app").projectDir = file("auth/app")

include(":auth:client")
project(":auth:client").projectDir = file("auth/client")


include(":websocket:api")
project(":websocket:api").projectDir = file("websocket/api")

include(":websocket:app")
project(":websocket:app").projectDir = file("websocket/app")

include(":websocket:domain")
project(":websocket:domain").projectDir = file("websocket/domain")
