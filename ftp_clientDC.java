import java.awt.Checkbox;
import java.io.*; 
import java.net.*;
import java.util.*;

public class ftp_clientDC {
    private static InetAddress host;
    private static final int PORT = 1236;

    public static void main(String[] args)
    {
        Socket server = null;
        String message, command, response, serverName = "";
        int serverPort;
        
        Scanner serverInput = null;
        PrintWriter serverOutput = null;

        //Set up stream for keyboard entry...
        Scanner userEntry = new Scanner(System.in);

        /* currently, I'm enclosing everything in a giant try block
         * This should be divided into smaller blocks for better error handling */
        try {
            do
            {
                /* display normal prompt preceeding every user entry */
                System.out.print("Enter command ('QUIT' to exit): ");
                
                /* get user input and tokenize to get command */
                message =  userEntry.nextLine();
                StringTokenizer tokens = new StringTokenizer(message);
                command = tokens.nextToken();
                
                if(command.equals("CONNECT"))
                {
                    /* check that no connection already exists with a server */
                    if (server != null)
                    {
                        System.out.println("A connection with server " + serverName + " has already been established.");
                        continue;
                    }
                    
                    /*check for correct parameters
                     * must be in the form CONNECT servername/IP server port */
                    serverName = tokens.nextToken();
                    serverPort = Integer.parseInt(tokens.nextToken());
                    server = new Socket(serverName, serverPort);

                    /* get input stream from server to receive response */
                    /* get output stream from server to send request */
                    serverInput = new Scanner(server.getInputStream());
                    serverOutput = new PrintWriter(server.getOutputStream(),true);
                    
                    System.out.println("Connection with " + serverName + " has been established.");
                    continue;  //repeat while loop
                }
                
                if(command.equals("LIST"))
                {
                    /* check that a connection with a server exists */
                    if (server == null)
                    {
                        System.out.println("A connection with a server has not been established.");
                        continue;
                    }

                    /* send the data port along with the command */
                    serverOutput.println("LIST " + PORT);

                    /* establish a welcome socket to receive the new connection on the new port */
                    ServerSocket welcomeSocket = new ServerSocket(PORT);

                    /* receive connection */
                    Socket dataSocket = welcomeSocket.accept();
                    
                    /* get input stream to read response to the data socket */
                    Scanner dataInput = new Scanner(dataSocket.getInputStream());
                    
                    /* get and print response */
                    response = dataInput.nextLine();
                    System.out.println(response);
                    
                    /* close connection and resources */
                    dataInput.close();
                    dataSocket.close();
                    welcomeSocket.close();
                }
                
                if(command.equals("RETR"))
                {
                }
                
                if(command.equals("STOR"))
                {
                }
                
            }while (!command.equals("QUIT"));
            
            /* tell the server that you are quitting */
            serverOutput.println("QUIT");
            
            /* close connection and resources */
            server.close();
            userEntry.close();
            serverOutput.close();
            serverInput.close();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}
