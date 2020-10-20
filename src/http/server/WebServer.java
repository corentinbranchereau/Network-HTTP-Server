///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebServer {

  enum HeaderType {
    ERROR,
    GET,
    HEAD,
    POST,
    PUT,
    DELETE
  }

  /**
   * WebServer constructor.
   */
  protected void start() {
    ServerSocket s;

    System.out.println("Webserver starting up on port 3000");
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(3000);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    System.out.println("Waiting for connection");
    for (;;) {
      try {
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        BufferedReader in = new BufferedReader(new InputStreamReader(
                remote.getInputStream()));

        // read the data sent.
        // stop reading once a blank line is hit. This
        // blank line signals the end of the client HTTP
        // headers.
        String str = ".";
        StringBuilder stringBuilder = new StringBuilder();
        while (!(str=in.readLine()).isBlank()){
            stringBuilder.append(str).append("\n");
        }

        String request = stringBuilder.toString();
        System.out.println("Request :\n"+request);

        handleClientRequest(remote, request);

        // close the connection
        remote.close();
      } catch (Exception e) {
        System.out.println("Error line "+e.getStackTrace()[0].getLineNumber()+" : " + e);
      }
    }
  }

  private void handleClientRequest(Socket client, String request) throws IOException {
    //Parse the request
    String[] linesFromRequest = request.split("\n");
    String[] singleLineRequest = linesFromRequest[0].split(" ");
    String method = singleLineRequest[0];
    String path = singleLineRequest[1];
    String version = singleLineRequest[2];
    String host = linesFromRequest[1].split(" ")[1];

    List<String> headers = new ArrayList<>();
    for (int h = 2; h < linesFromRequest.length; h++) {
      String header = linesFromRequest[h];
      headers.add(header);
    }

    switch(method) {
      case "GET":
        handleGET(client, path);
        break;
      case "HEAD":
        handleHEAD(client, path);
        break;
      case "DELETE":
        handleDELETE(client, path);
        break;
      case "POST" :
        //handlePOST(client, path);
      default:
        sendResponse(client, StatusCode.CODE_503, null, null, HeaderType.ERROR);
    }
  }

  private void handleGET(Socket client, String path) throws IOException {
      Path filePath = getFilePath(path);
      System.out.println("FilePath after guess : "+filePath);
      if (Files.exists(filePath)) { // if the file exist
        String contentType = Files.probeContentType(filePath);
        sendResponse(client, StatusCode.CODE_200, contentType, Files.readAllBytes(filePath),HeaderType.GET);
      } else { // Error 404 not found
        byte[] contentNotFound = "<h1>404 Not found :(</h1>".getBytes(StandardCharsets.UTF_8);
        sendResponse(client, StatusCode.CODE_404, "text/html", contentNotFound,HeaderType.GET);
      }
  }

  private void handleHEAD(Socket client,String path) throws IOException {
    Path filePath = getFilePath(path);
    // Vérification de l'existence de la ressource demandée
    if(Files.exists(filePath)) {
      String contentType = Files.probeContentType(filePath);
      sendResponse(client, StatusCode.CODE_200, contentType,Files.readAllBytes(filePath),HeaderType.HEAD);
    } else {
      sendResponse(client, StatusCode.CODE_404, null, null, HeaderType.ERROR);
    }
  }

  private void handleDELETE(Socket client, String path) throws IOException {
    Path filePath = getFilePath(path);
    if(Files.exists(filePath)){
        System.out.println("DELETED file : "+filePath);
        Files.delete(filePath);
        sendResponse(client, StatusCode.CODE_200, null, null, HeaderType.HEAD);
    } else {
      System.out.println("File not found : "+filePath);
      sendResponse(client, StatusCode.CODE_404, null, null, HeaderType.ERROR);
    }
  }

  private void sendResponse(Socket client, StatusCode status, String contentType, byte[] content, HeaderType type) throws IOException {
    PrintWriter out = new PrintWriter(client.getOutputStream());
    BufferedOutputStream binaryDataOutput = new BufferedOutputStream(client.getOutputStream());
    Date date = new Date();

    System.out.println("Response with status code : "+status+" for header type : "+type);

    out.println("HTTP/1.1 "+status);
    out.println("Date: "+date);

    if(type.equals(HeaderType.GET) || type.equals(HeaderType.HEAD)) {
      out.println("Content-Type: " + contentType);
      out.println("Content-Encoding: UTF-8");
      out.println("Content-Length: " + content.length);
    }

    out.println("Server: Java Web Server (Unix) (H4411 B01)");
    out.println("Connection: close");
    out.println("");
    out.flush();

    if(type.equals(HeaderType.GET)){
      binaryDataOutput.write(content,0,content.length);
      binaryDataOutput.flush();
    }
  }

  private Path getFilePath(String path) {
    if ("/".equals(path)) {
      path = "/index.html";
    }
    return Paths.get("./res", path);
  }

  /**
   * Start the application.
   *
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
