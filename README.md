# Network Http Server
Building HTTP servers with Java Synchronous network communication systems at INSA Lyon (4th) 

javac -d bin/ src/http/client/WebPing.java src/http/server/WebServer.java

java -cp bin/ http.server.WebServer

java -cp bin/ http.client.WebPing 127.0.0.1 3000
