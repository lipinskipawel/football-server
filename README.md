# Football Server

This repository contains the server code for the football game. Server accepts WebSocket connections as defined in the
[RFC-6455].

[RFC-6455]: http://tools.ietf.org/html/rfc6455

### Features

Currently, Football Server offers:

- Smooth and fast playing capabilities
- Lobby where clients can connect to and choose the opponent to play

### Project modules

Football Server repository is made of two modules:

- [api]
- [app]

The `api` module defines every object that the server understands. Those objects are the API and must be used after
successful connection to the server. The `app` module contains the server code and the main class to start the server.
Please refer to the documentation of each module for mode information.

[api]: ./api

[app]: ./app

### Football game

![Football game picture](./football.png)

Football game is a great game without any age limit. It's fun and simple to play. There are existing clients that are
able to communicate with the server such as:

- [football-web]
- [football-desktop]

[football-web]: https://github.com/lipinskipawel/football-web

[football-desktop]: https://github.com/lipinskipawel/football-desktop
