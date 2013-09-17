
package com.azosc.whiteboard.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**********************************************************************************************************************
 * This application runs a Java WebSocket server to listen for and handle calls made by clients for the Azosc
 * Whiteboard.
 *********************************************************************************************************************/
public class WhiteboardServer extends WebSocketServer {

    public static final int PORT = 8887;

    /******************************************************************************************************************
     * Creates a WhiteboardServer to listen on the designated port. Server is not started until WhiteboardServer.start()
     * is called.
     *****************************************************************************************************************/
    public WhiteboardServer( int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    /******************************************************************************************************************
     * TODO Send message to alert clients of the new connection and relevant information. NYN - Not yet needed
     *****************************************************************************************************************/
    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        //this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
        //System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    /******************************************************************************************************************
     * TODO Send message to alert clients of the lost connection and relevant information. NYN - Not yet needed
     *****************************************************************************************************************/
    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        this.sendToAll( conn + " has left the room!" );
        System.out.println( conn + " has left the room!" );
    }

    /******************************************************************************************************************
     * Uh oh. What fancy errors can come about here?
     *****************************************************************************************************************/
    @Override
    public void onError( WebSocket conn, Exception ex ) {
        System.out.println("PS. We have an error... :(");
        ex.printStackTrace();
        if( conn != null ){
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    /******************************************************************************************************************
     * Receives a message from a client, acts upon the message (ex. updates/queries database information), then sends
     * the update to all connected clients (or just the one client if it was just a DB query).
     * 
     * TODO - yea.. still need to figure all that out. So for now, just relay message to all the clients.
     *****************************************************************************************************************/
    @Override
    public void onMessage( WebSocket conn, String message ) {
        this.sendToAll( message );
        System.out.println( conn + ": " + message );
    }

    /******************************************************************************************************************
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *****************************************************************************************************************/
    public void sendToAll( String text ) {
        Collection<WebSocket> con = connections();
        synchronized( con ){
            for( WebSocket c : con ){
                c.send( text );
            }
        }
    }

    /******************************************************************************************************************
     * Creates/starts the server to listen on the designated port. Also, accepts input entered directly into the server
     * console window.
     *****************************************************************************************************************/
    public static void main( String[] args ) throws InterruptedException, IOException {
        WebSocketImpl.DEBUG = true;

        WhiteboardServer server = new WhiteboardServer( PORT );

        // Begin listening for client activity
        server.start();
        System.out.println( "ChatServer started on port: " + server.getPort() );

        // Handles input entered directly into the server's active console
        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while( true ){
            String in = sysin.readLine();

            server.sendToAll( in );             // Send server's input to all clients
            if( in.equals( "exit" ) ){          // Stop server if requested
                server.stop();
                break;
            }else if( in.equals( "restart" ) ){ // Restart server if requested
                server.stop();
                server.start();
            }
        }
    }
}
