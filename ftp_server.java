import java.io.*; 
import java.net.*;
import java.util.*;


public class ftp_server {
    private static ServerSocket welcomeSocket;
    private static final int PORT = 1234;

    public static void main(String[] args) throws IOException
    {
        try
        {
            /* This is the main socket that listens for incoming connections.
             * Once a "control connection" is established between a client,
             * a thread is spun off to handle all further communication with
             * that client. This socket only handles the first incoming 
             * connection request. */
            welcomeSocket = new ServerSocket(PORT);
        }
        catch (IOException ioEx)
        {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

        do
        {
            Socket client = welcomeSocket.accept();

            System.out.println("\nNew client accepted.\n");

            /* spin off new thread to handle all future communication with client */
            ClientHandler handler = new ClientHandler(client);

            handler.start();
        }while (true);  //continue listening; program must be ended by a Ctrl-C
    }
}

class ClientHandler extends Thread
{
    private Socket client;
    private Scanner input;
    private PrintWriter output;
    private int dataConnPort;
    private String command;
    private String received;
    private int dataInputPort = 1236;

    public ClientHandler(Socket socket)
    {
        client = socket;

        try
        {
            /* get input/output streams from the client socket 
             * These represent the i/o for the persistent command connection */
            input = new Scanner(client.getInputStream());
            output = new PrintWriter(client.getOutputStream(),true);
        }
        catch(IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    public void run()
    {

        /* loop until client sends QUIT command */
        /* all commands must be in the form COMMAND PARAMETERS */
        do
        {
            /* Get the next command */
            received = input.nextLine();  //this line blocks until message is received
            StringTokenizer tokens = new StringTokenizer(received);
            command = tokens.nextToken();
            
            if(command.equals("LIST"))
            {
                /* command should be in the form LIST dataConnPort */
                /* return a list of the files in the current directory in which this process is executing */
                
                try {
                    dataConnPort = Integer.parseInt(tokens.nextToken());
                } catch (NumberFormatException e1) {
                    /* user typed a bad port number. send error message, wait for next command, parse it, repeat while loop */
                    output.println("Invalid port number. Command must be in the form LIST (int)portNumber");
                    continue;
                }
                
                /* Create new socket to data port specified by user for data transfer 
                 * Get output stream to write data to */
                try {
                    Socket dataSocket = new Socket(client.getInetAddress(), dataConnPort);
                    PrintWriter dataOutput = new PrintWriter(dataSocket.getOutputStream(), true);
                                    
                    String fileList = "";
                    File folder = new File(".");  //the folder for this process
                    File[] listOfFiles = folder.listFiles();  //this object contains all files AND folders in the current directory

                    /* Iterate through and add the name to the list only if the file object is indeed a file (not a directory) */
                    for (File file : listOfFiles) {
                      if (file.isFile()) {
                          fileList += file.getName() + ", ";
                      }
                    }
                        
                    /* If the list of files isn't empty, then trim the last ", " off the list */
                    if (fileList.length() != 0){
                        fileList = fileList.substring(0,  fileList.length() - 2);
                    }
                    
                    /* Send list to client */
                    dataOutput.write(fileList);
                    
                    /* Close data socket connection */
                    dataOutput.close();
                    dataSocket.close();
                    
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            
            if(command.equals("RETR"))
            {
                /* will be of the form RETR <filename> <port> */
                /* send <filename> to client address at <port> */
                
                String fileName = tokens.nextToken();
                
                try {
                    dataConnPort = Integer.parseInt(tokens.nextToken());
                } catch (NumberFormatException e1) {
                    /* user typed a bad port number. send error message, wait for next command, parse it, repeat while loop */
                    output.println("Invalid port number. Command must be in the form RETR (string)filename (int)portNumber");
                    continue;
                }
                
                /* If the file lives where this class lives, the directory will be on the classpath */
                URL path = ClassLoader.getSystemResource("myFile.txt");
                if(path == null) {
                    /* File was not found. Send error message, repeat while loop */
                    output.println(fileName + " cound not be found. Please specify a different file.");
                    continue;
                }
   
                byte[] bytes = new byte[16 * 1024];
                InputStream fileIn = null;
                
                try {
                    /* get the file and get an input stream from it */
                    fileIn = new FileInputStream(fileName);
                    
                    /* get an output stream using the client's provided data port number */
                    Socket dataSocket = null;
                    OutputStream dataOutput = null;
                    dataSocket = new Socket(client.getInetAddress(), dataConnPort);
                    dataOutput = dataSocket.getOutputStream();
                    
                    /* there may be a much more efficient way to do this?... */
                    /* read bytes from file and write them to output stream */
                    int count;
                    while ((count = fileIn.read(bytes)) > 0) {
                        dataOutput.write(bytes, 0, count);
                    }
                    
                    /* close streams and socket */
                    dataOutput.close();
                    fileIn.close();
                    dataSocket.close();
                    
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }    
            }
    
            if(command.equals("STOR"))
            {
                /* allow a client to store a file
                 * command will be of the form STOR <filename> */
                
                try {
                    /* Capture name of file to be stored */
                    String fileName = tokens.nextToken();
                    
                    /* open up a new data port */
                    ServerSocket inputSocket = new ServerSocket(dataInputPort);

                    /* send client a message on which port to use */
                    output.println("STOR " + dataInputPort);

                    /* listen for client connection to come in */
                    Socket dataInputSocket = inputSocket.accept();  //this blocks until client connects
                    
                    /* write incoming file to disk */
                    /* better way to do this? */
                    InputStream fileIn = dataInputSocket.getInputStream();
                    OutputStream fileOut = new FileOutputStream(fileName);
                    byte[] bytes = new byte[16*1024];

                    int count;
                    while ((count = fileIn.read(bytes)) > 0) {
                        fileOut.write(bytes, 0, count);
                    }
                    
                    /* close all resources */
                    fileOut.close();
                    fileIn.close();
                    dataInputSocket.close();
                    inputSocket.close();
                    
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
            } while (!command.equals("QUIT"));
        
        /* client has sent command QUIT */
        try
        {
            if (client!=null)
            {
                System.out.println("Closing down connection...");
                client.close();
            }
        }
        catch(IOException ioEx)
        {
            System.out.println("Unable to disconnect!");
        }
    }
    
    
}
