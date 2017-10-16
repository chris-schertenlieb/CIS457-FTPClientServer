
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
                	// first check that the server is not null, code copied from Dave
                    if (server == null)
                    {
                        System.out.println("A connection with a server has not been established.");
                        continue;
                    }
                    
                    StringTokenizer retrToken = new StringTokenizer(message);
                    
                    // get the token for the name of the file we're looking for
                    String retrCommand = retrToken.nextToken();
                    String targetFile = retrToken.nextToken();
                    
                    // send our command to the server, with the file we want and our welcomeSocket port number
                    serverOutput.println("RETR " + targetFile + " " + PORT);

                    // establish our sockets
                    ServerSocket welcomeSocket = new ServerSocket(PORT);
                    Socket dataSocket = welcomeSocket.accept();
                    
                    byte [] fileContents = new byte[10000];
                    
                    File file = new File("./Test.txt");
                    
                    FileOutputStream fileStream = new FileOutputStream(file);
                    BufferedOutputStream buffStream = new BufferedOutputStream(fileStream);
                    
                    InputStream fileIn = dataSocket.getInputStream();
                    
                    int bytesRead = 0;
                    
                    bytesRead = fileIn.read(fileContents);
                    
                    System.out.println(Arrays.toString(fileContents));
                    
                    buffStream.write(fileContents, 0, bytesRead);
                    
                    //while((bytesRead = fileIn.read(fileContents)) != -1) {
                    //	buffStream.write(fileContents, 0, bytesRead);
                    //}
//
//                    // byte array buffer thingy for our file
//                    byte[] fileContents = new byte[10000];
//
//                    // set up a fileOutputStream and set the path to our current directory
//                    // plus the file we want
//                    // also we nest it inside a buffer for efficiency and all that jazz
//                    FileOutputStream fileStream = new FileOutputStream("./" + targetFile);
//                    BufferedOutputStream buffStream = new BufferedOutputStream(fileStream);
//
//                    // our inputStream for getting data from the server
//                    InputStream fileIn = dataSocket.getInputStream();
//
//                    // this will help us keep track of completion
//                    int bytesRead = 0;
//
//                    // fileIn.read(fileContents) is saying: read bytes from the
//                    // fileInputStream and store them in the byte array fileContents.
//                    // https://www.tutorialspoint.com/java/io/inputstream_read_byte.htm
//                    // fileIn.read(fileContents) returns the number of bytes it read (see doc)
//                    // and we store that number in bytesRead and check that it is not -1, as
//                    // the read method will return -1 if the end of the stream is reached (see doc)
//                    while((bytesRead=fileIn.read(fileContents)) != -1)
//                    {
//                      // we write all these bytes to our OutputStream whose destination is our current directory
//                      buffStream.write(fileContents, 0, bytesRead);
//                    }
//                    
//                    // we flush our output stream to free up our memory
//                    buffStream.flush();
//                    fileStream.close();
//                    buffStream.close();
//                    dataInput.close();
                    buffStream.close();
                    dataSocket.close();
                    welcomeSocket.close();
                    
                    System.out.println("File Saved!");
                }
                
                if(command.equals("STOR")){

                	//first check server is not null
                	if (server == null){
                        	System.out.println("A connection with a server has not been established.");
                        	continue;
                        }
                	
                	//get file name from user input
                	StringTokenizer retrToken = new StringTokenizer(message);
                	String retrCommand = retrToken.nextToken();
                	String fileName = retrToken.nextToken();
                	
                	//tell the server what command will be executed as well as the file name being transfered.
                	server.Output.println("STOR " + fileName);

                	//set up data socket with server
                	ServerSocket welcomeSocket = new ServerSocket(PORT);
                	Socket dataSocket = welcomeSocket.accept();

                	//PrintWriter dataOutput = new PrintWriter(dataSocket.getOutputStream(), true);


                	URL path = ClassLoader.getSystemResource("myFile.txt");
                               if(path == null) {
                                   /* File was not found. Send error message, repeat while loop */
                                   System.out.println(fileName + " cound not be found. Please specify a different file.");
                                   continue;
                               }	
                		
                 		byte[] bytes = new byte[16 * 1024];
                                InputStream fileIn = null;
                                
                                try {
                                    /* get the file and get an input stream from it */
                                    fileIn = new FileInputStream(fileName);
                                    
                                    /* get an output stream using the client's provided data port number */
                                    //Socket dataSocket = null;
                                    OutputStream dataOutput = null;
                                    dataSocket = new Socket(server.getInetAddress(), PORT);
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