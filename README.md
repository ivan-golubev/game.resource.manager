Game Resource Manager
-----------

Service used to manage the game resources:

1. create/ read/ update/ delete resources for a specified game
2. basic authentication for a predefined set of users
3. basic access control: owner can update/ delete, others can read
4. real-time updates in browser

REST API
--------

POST /login - does Basic authentication, Authorization header should be set, returns x-auth-token header to use

POST /logout - invalidates the session

POST /resources?game=<game_name> - creates a new resource for the specified game,
 Accept and Content-Type headers should be set to application/json, x-auth-token should be set. Returns a resource id.
 
GET /resources/<id>?game=<game_name> - returns a resource with the specified id and game if exists.

GET /resources?game=<game_name> - returns all resource for the specified game or empty list if game does not exist.

PUT /resources/<id>?game=<game_name> - update a resource with the specified id and game if it exists and user owns this resource.

DELETE /resources/<id>?game=<game_name> - deletes a resource with the specified id and game if it exists and user owns this resource.


Tech stack
----------
spring boot + spring web - server, REST API

spring websockets - supporting websockets

spring session - session management

restassured - testing the REST api

Installation
-------------------------
1. Either download a zip or perform a git checkout from [github][1].
2. Install [JDK 8][2].
3. Install [Gradle][3].
4. [Set up][4] the environment variable: GRADLE\_HOME.

Usage
-----

Execute this in console (cd to the project root directory first) or in you favourite IDE:

`> gradle clean test`

This will execute various test scenarios.

To launch the server execute:

`> gradle clean build && java -jar build/libs/resmanager-1.0.jar`

Then you may issue requests from curl/browser/postman/etc..
Example requests are available as a Postman collection in file

`resource-manager.postman_collection.json`
 
To view the resource updates in real-time go to: http://localhost:8080 and press Connect.
This page is available without authentication.
Use up-to-date Firefox/ Chrome / Opera / Safari supporting the websocket protocol.

[1]: https://github.com/ivan-golubev/game.resource.manager
[2]: http://www.oracle.com/technetwork/java/javase/downloads
[3]: https://gradle.org/gradle-download/
[4]: https://docs.gradle.org/current/userguide/installation.html