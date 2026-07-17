package com.thecompanyinc.cobblemoninitiative.devtools.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * JSON-lines protocol over a loopback-only socket — the client-side analogue of
 * {@code scripts/mc_rcon.py}'s server channel. One request per line
 * {@code {"id":N,"op":"screen.dump","args":{...}}} → one response per line
 * {@code {"id":N,"ok":true,"data":{...}}}. Synchronous like RCON: each op completes
 * (render-thread round trip included) before its response is written, so the Python
 * side gets free handshaking. Single client at a time; a dropped connection just
 * loops back to accept.
 */
final class DriverServer extends Thread {

  private static final Gson GSON = new Gson();

  private final int port;

  DriverServer(int port) {
    super("ci-test-driver");
    this.port = port;
    setDaemon(true); // never keep the game JVM alive
  }

  @Override
  public void run() {
    try (ServerSocket server = new ServerSocket(port, 1, InetAddress.getLoopbackAddress())) {
      while (true) {
        try (Socket sock = server.accept()) {
          sock.setTcpNoDelay(true);
          serve(sock);
        } catch (IOException e) {
          TestDriverClient.LOGGER.info("[driver] connection dropped: {}", e.getMessage());
        }
      }
    } catch (IOException e) {
      TestDriverClient.LOGGER.error("[driver] cannot bind 127.0.0.1:{}: {}", port, e.getMessage());
    }
  }

  private void serve(Socket sock) throws IOException {
    BufferedReader in =
      new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
    Writer out = new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8);
    TestDriverClient.LOGGER.info("[driver] harness connected");

    String line;
    while ((line = in.readLine()) != null) {
      if (line.isBlank()) continue;
      long id = -1;
      JsonObject response = new JsonObject();
      try {
        JsonObject request = JsonParser.parseString(line).getAsJsonObject();
        id = request.has("id") ? request.get("id").getAsLong() : -1;
        String op = request.get("op").getAsString();
        JsonObject args =
          request.has("args") ? request.getAsJsonObject("args") : new JsonObject();
        JsonObject data = DriverOps.handle(op, args);
        response.addProperty("ok", true);
        response.add("data", data);
      } catch (Exception e) {
        // Unwrap the ExecutionException shell from render-thread futures for readable errors.
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        response.addProperty("ok", false);
        response.addProperty(
          "error", cause.getClass().getSimpleName() + ": " + cause.getMessage());
      }
      response.addProperty("id", id);
      out.write(GSON.toJson(response));
      out.write('\n');
      out.flush();
    }
  }
}
