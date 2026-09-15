# Private Network Chat

Simple Java 8 chat with AES-GCM encryption, multiple users, colored messages, and optional Tor SOCKS routing.

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

## Tests

```bash
mvn test
```

## License

MIT License. See [LICENSE](LICENSE).
