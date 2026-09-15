# Private Network Chat

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=openjdk)
![Build](https://img.shields.io/badge/build-Maven-blue?logo=apachemaven)
![License](https://img.shields.io/badge/license-MIT-green)

> A small, privacy-focused desktop chat application for encrypted local or private network conversations.

## About

Private Network Chat is a Java Swing application built for simple, direct communication. It supports multiple connected users, AES-GCM encrypted messages, selectable message colors, a private room model, and optional Tor SOCKS routing.

The project is intentionally lightweight: no database, no external chat service, and no complicated setup. Build it with Maven, start a server, and connect from one or more clients.

## Highlights

| Capability | Details |
| --- | --- |
| Encryption | AES-GCM with PBKDF2 key derivation |
| Networking | Multi-client TCP server and client |
| Interface | Java Swing desktop GUI |
| Rooms | Private room model included |
| Privacy | Optional Tor SOCKS proxy support |
| Compatibility | Java 8 or newer |

## Requirements

- Java 8 or newer
- Maven

## Build

Run these commands from the project folder:

```bash
mvn clean package
```

This creates `target/network-chat-gui-1.0.0.jar`.

## Run the GUI

```bash
java -jar target/network-chat-gui-1.0.0.jar
```

In the GUI:

1. Click `Start Server`.
2. Keep host as `127.0.0.1` and port as `9090`.
3. Click `Connect`.
4. Type a message and click `Send`.

To use multiple computers, run the server on one computer and enter its local IP address in the other clients.

> Build the project before running it. The JAR is created inside `target/`.

## Run the server separately

```bash
java -cp target/classes com.example.networkchat.ChatServer 9090
```

Keep the server terminal open, then start the GUI on each client and click `Connect`.

## Console mode

Useful on a headless machine:

```bash
java -cp target/classes com.example.networkchat.ChatConsoleApp
```

Type `exit` to disconnect.

## Tor

The app supports an existing Tor SOCKS proxy. Start Tor and use its usual local SOCKS address, commonly `127.0.0.1:9050`.

Java example:

```java
ChatClient client = new ChatClient(
	"example.onion", 9001, "your-strong-passphrase", "127.0.0.1", 9050);
client.connect();
```

Use the same strong passphrase for the server and all clients. Tor hides the network path; the app encryption protects the messages.

## Features

- AES-GCM message encryption with PBKDF2 key derivation
- Multiple connected users
- Private room model
- Selectable message colors
- Java SOCKS/Tor support

## Suggested GitHub topics

`java` `java8` `swing` `networking` `chat-application` `encryption` `aes-gcm` `privacy` `tor` `socks-proxy` `maven`

## Project structure

- `ChatApp.java` - Swing GUI
- `ChatServer.java` - multi-client encrypted server
- `ChatClient.java` - encrypted client with SOCKS support
- `PrivacyCrypto.java` - AES-GCM and PBKDF2 helpers
- `ChatConsoleApp.java` - terminal client for headless systems
- `ChatRoom.java` and `ChatUser.java` - room and user models

## Security note

Use a strong shared passphrase and do not use the default passphrase for real deployments. Tor can hide the network path, but it does not replace application-level encryption.

## Tests

```bash
mvn test
```

## Releases and packages

- [Releases](https://github.com/nadabakhay/java-privacy-chat-app-/releases) - download published versions
- [Packages](https://github.com/users/nadabakhay/packages?repo_name=java-privacy-chat-app-) - view published GitHub packages

## License

MIT License. See [LICENSE](LICENSE).
