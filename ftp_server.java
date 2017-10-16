import java.io.*;
import java.net.*;
import java.util.*;


public class FTPServer {
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
            output = new PrintWriter(client.getOutputStream(), true);
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
                    output.write("Invalid port number. Command must be in the form LIST (int)portNumber");
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
                /* will be of the form RETR <filename>  */
                /* send <filename> to client address at <port> */


                // get the file name passed by the client
                String fileName = tokens.nextToken();

                // Check if the file lives with us, if it does we get a file object, if not, we print a message and continue

                System.out.println("Checking for File...");
                File file = new File(fileName);
                if(!file.exists()) {
                    // File was not found. Send error message, repeat while loop */

                    System.out.println(fileName + " could not be found. Please specify a different file.");
                    continue;
                }
                System.out.println("File found!");



                // get the data port for the client
                System.out.println("Finding data port...");
                try {
                    dataConnPort = Integer.parseInt(tokens.nextToken());
                } catch (NumberFormatException e1) {
                    /* user typed a bad port number. send error message, wait for next command, parse it, repeat while loop */
                    System.out.println("Invalid port number. Command must be in the form RETR (string)filename (int)portNumber");
                    continue;
                }

                System.out.println("Data Port Found!");



                // give this pupper a go
                System.out.println("Preparing for file transfer...");
                try {
                	// get our data connection going
                	Socket dataSocket = new Socket(client.getInetAddress(), dataConnPort);


                    // initialize our byte array at the size of our file
                    byte [] byteArray = new byte [(int)file.length()];

                    // establish our filestreams tht point to the file we're sending
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    // read from the filestream into our byte array
                    bis.read(byteArray, 0, byteArray.length);

                    // initialize our outputstream on the data Socket
                    OutputStream os = dataSocket.getOutputStream();
                    System.out.println("Sending " + file + " (" + byteArray.length + " bytes)");

                    // write all the stuff out to the client
                    os.write(byteArray, 0, byteArray.length);

                    os.flush();
                    bis.close();
                    dataSocket.close();


                    System.out.println("Done!");
                } catch (FileNotFoundException e) {
                    System.out.println("File could not be found!");
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }

            }

            if(command.equals("STOR"))
            {

            	// get the file name passed by the client
            	String fileName = tokens.nextToken();

              // get the data port we are working at
            	try {
            		dataConnPort = Integer.parseInt(tokens.nextToken());
            	} catch (NumberFormatException e1) {
            		/* user typed a bad port number. send error message, wait for next command, parse it, repeat while loop */
                    System.out.println("Invalid port number. Command must be in the form RETR (string)filename (int)portNumber");
                    continue;
            	}

            	try {
                // establish our data connection
            		Socket dataSocket = new Socket(client.getInetAddress(), dataConnPort);

                // initialize our byte array buffer
            		byte [] fileContents = new byte[10000];

                // create our file object that we will dump the data into
            		File file = new File(fileName);

                // our file streams that attach to the file
            		FileOutputStream fileStream = new FileOutputStream(file);
            		BufferedOutputStream buffStream = new BufferedOutputStream(fileStream);

                // get our stream from the client
            		InputStream fileIn = dataSocket.getInputStream();

                // a counter variable
            		int bytesRead = 0;

                // read contents from fileIn into fileContents, and returns into bytesRead the number of bytes that were read
            		bytesRead = fileIn.read(fileContents);

                // write the data from our byte array into our fileStreams
            		buffStream.write(fileContents, 0, bytesRead);

            		buffStream.close();
            		dataSocket.close();

            		System.out.println("File Saved!");

            	} catch (FileNotFoundException e) {
                    System.out.println("File could not be found!");
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
