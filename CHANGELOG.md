# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

- Allow to play a game only after pairing through /lobby endpoint
- Server will treat header cookie value as username
- Add GameMove API which is used to make a move
- Add PlayPairing API which will be sent by the server to connected clients always when they will play with each other
- Add RequestToPlay API in order to select the client to play with
- Stream all connected client's information under the /lobby endpoint
- Does not open a WebSocket connection for endpoints others that /lobby, /game/{id}
- Allow only two clients to connect to the same game endpoint under /game/{id}
