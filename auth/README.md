# Auth module

This module contains the code responsible for the authentication. Auth module also provides a simple [AuthServer] class
that can start the HTTP server. Currently, auth module provides registration of usernames. Any client can register a
username, and it will get a token in return. This token can be used to prove the authentication.

[AuthServer]: src/main/java/com/github/lipinskipawel/AuthServer.java

### Functionality

Auth module exposes an HTTP endpoint at `/register` (see [RegisterHandler] java class) to allow clients register any
username. Clients must provide header named `username` with a value of the proposal username. For any valid http request
with unique username in it the server will respond with http response containing header named `token` with the value of
token associated with the registered username.
In addition to that the module exposes API under the [api] package. This API can be used to query registered clients.
The purpose of this API is to give simple binary layer of integration with the module for others modules.

[RegisterHandler]: src/main/java/com/github/lipinskipawel/register/RegisterHandler.java

[api]: src/main/java/com/github/lipinskipawel/api