# SOFE 4790U Final Project

This is a data transfer and remote control application.

## Running

To run the app, one first needs to install OpenJDK 21.

Each command below should be ran in the source code directory (it should include a `out` folder).

The server should be started before any clients. To start the server, run the following command:
```
$ java -cp ./out/production/UnifiedProject net.uoit.sofe4790.project.server.ServerMain
```

To run a client instance, run the following command, while inserting the server IP in the correct place:
```
$ java -cp ./out/production/UnifiedProject net.uoit.sofe4790.project.client.ClientMain <server IP>
```

The default username and password is `testuser` and `testpassword`. More users can be added by modifying the `users.txt` file and restarting the server.
