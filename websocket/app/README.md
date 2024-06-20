# app module of Football Server

This module contains the code of Football Server. Here is the code responsible for accepting WebSocket connections. Each
client that attempts to connect to Football Server must follow defied [policy].

[policy]: README.md#Policy

### Development

Contributions are welcome.

#### Running server locally

Run local server by executing `gradle app:run`.

#### Running tests

Unit tests can be run by `gradle test`. Integration tests can be run by `gradle iT`.

### Policy

In order to establish connection to the server the client must use WebSocket technology. Furthermore, it must open the
connection at `/ws/lobby` endpoint. It will be possible now for a client to select an opponent to play with. After
choosing the opponent server will send a message with the redirect endpoint that will be available for client to connect
to the game. This redirect endpoint will look like `/ws/game/{id}`.