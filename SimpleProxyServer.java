
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleProxyServer {
  public static void main(String[] args) throws IOException {
    try {
      String host = "www.google.com";
      int remoteport = 80;
      int localport = 111;
      // Print a start-up message
      System.out.println("Starting proxy for " + host + ":" + remoteport
          + " on port " + localport);
      // And start running the server
      runServer(host, remoteport, localport); // never returns
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public static void runServer(String host, int remoteport, int localport)
      throws IOException {
    // Create a ServerSocket to listen for connections with
    ServerSocket ss = new ServerSocket(localport);

    final byte[] request = new byte[1024];
    byte[] reply = new byte[4096];

    while (true) {
      Socket client = null, server = null;
      try {
        // Wait for a connection on the local port
        client = ss.accept();

        final InputStream streamFromClient = client.getInputStream();
        final OutputStream streamToClient = client.getOutputStream();

        // Make a connection to the real server.
        // If we cannot connect to the server, send an error to the
        // client, disconnect, and continue waiting for connections.
//        try {
//	  System.out.println("host=" + host + ",port=" + remoteport);
//          server = new Socket(host, remoteport);
//        } catch (IOException e) {
//          PrintWriter out = new PrintWriter(streamToClient);
//          out.print("Proxy server cannot connect to " + host + ":"
//              + remoteport + ":\n" + e + "\n");
//          out.flush();
//          client.close();
//          continue;
//        }



        // Get server streams.
        final InputStream streamFromServer = server.getInputStream();
        final OutputStream streamToServer = server.getOutputStream();

        // a thread to read the client's requests and pass them
        // to the server. A separate thread for asynchronous.
        Thread t = new Thread() {
          public void run() {
            int bytesRead;
            try {
	      java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();

              while ((bytesRead = streamFromClient.read(request)) != -1) {
		bos.write(request, 0, bytesRead);
	      }
	      	byte[] a = bos.toByteArray();

		String rstr = new String(a);

	       Pattern pattern = Pattern.compile("(?<=GET /)([^\\s]*)(?= HTTP)");
	       Matcher matcher = pattern.matcher(rstr);
 if(matcher.find()) {
            System.out.println("Matcher: " + matcher.group(1));
        }

 String target = matcher.group(1);

        try {
	  System.out.println("host=" + host + ",port=" + remoteport);
          server = new Socket(host, remoteport);
        } catch (IOException e) {
          PrintWriter out = new PrintWriter(streamToClient);
          out.print("Proxy server cannot connect to " + host + ":"
              + remoteport + ":\n" + e + "\n");
          out.flush();
          client.close();
          continue;
        }
		System.out.println(rstr);
                streamToServer.write(a, 0, a.length);
                streamToServer.flush();
              
          //    while ((bytesRead = streamFromClient.read(request)) != -1) {
          //      streamToServer.write(request, 0, bytesRead);
          //      streamToServer.flush();
          //    }
            } catch (IOException e) {
            }

            // the client closed the connection to us, so close our
            // connection to the server.
            try {
              streamToServer.close();
            } catch (IOException e) {
            }
          }
        };

        // Start the client-to-server request thread running
        t.start();

        // Read the server's responses
        // and pass them back to the client.
        int bytesRead;
        try {
          while ((bytesRead = streamFromServer.read(reply)) != -1) {
            streamToClient.write(reply, 0, bytesRead);
            streamToClient.flush();
          }
        } catch (IOException e) {
        }

        // The server closed its connection to us, so we close our
        // connection to our client.
        streamToClient.close();
      } catch (IOException e) {
        System.err.println(e);
      } finally {
        try {
          if (server != null)
            server.close();
          if (client != null)
            client.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public static String getHeaderToArray(InputStream inputStream) {

        String headerTempData = "";

        // chain the InputStream to a Reader
        Reader reader = new InputStreamReader(inputStream);
        try {
            int c;
            while ((c = reader.read()) != -1) {
                System.out.print((char) c);
                headerTempData += (char) c;

                if (headerTempData.contains("\r\n\r\n"))
                    break;
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        // headerData = headerTempData;

        return headerTempData;
    }
}
