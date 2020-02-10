/*
Client Class
Student name: Malgorzata Kalarus
Student id:
 */
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ServerApp.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;


public class Client {

    static ServerHelp server;
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get the root naming context
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            String name = "P2P";
            server = ServerHelpHelper.narrow(ncRef.resolve_str(name));


        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }

        //ask user for login
        // create scanner object for user input
        Scanner sc = new Scanner(System.in);
        String username = "";
        String password = "";
        String filename = "";
        String userIn = "";
        String loginResult = "";

        // check to see if user exists in database by taking user input and calling loginTest() to see if a match is found
        // while incorrect login is made continue to ask user for credentials
        while (!loginResult.equals("User Login Successful")) {
            System.out.println("* Database User Login *");
            System.out.print("Enter Username: ");
            username = sc.next();
            System.out.print("Enter Password: ");
            password = sc.next();

            System.out.println("Now logging you in...");


            //use Servers login function to connect to database and try to login user using provided credentials
            loginResult = server.login(username, password);

            //if login unsuccessful, ask user to login again
            if(!loginResult.equals("User Login Successful")){
                System.out.println("Incorrect Login Attempt. Please Try Again");
            }
        }//Jump out of while loop when loginResult.equals("User Login Successful")

        System.out.println("Successful Login\n");
        // Display to user that they are now logged in
        System.out.println("User " + username + " is logged on ");

        // Call updateDatabase() to get user's Address and Port numbers and updates database after user logs in.
        // this allows for another user to open a Peer to Peer connection with user's port number and download file
        // that has database's shared file's name.
        // set the result of function call to be the port
        String stringPort = server.updateDatabase(username);
        int port = Integer.parseInt(stringPort);


        //ask user to select from the main menu options to continue. will exit loop when user selects Option 4 which will end the program
        while (true) {
            String option = "";
            // call serverSocket function to begin a Server socket connection
            serverSocketFunction(port);

            System.out.println("--------------- ");
            System.out.println("*  Main Menu  * ");
            System.out.println("--------------- ");
            System.out.println("to continue, select from the following options. ");
            //The user can choose which files s/he wants to share with other users. The client program then registers the files on the server via CORBA.
            //The user can remove the files that s/he no longer wants to share with others. Client program then will update the sharing status on the server via CORBA.
            System.out.println("1 -> Share a file");
            System.out.println("2 -> Stop sharing a file");
            System.out.println("3 -> Search for a file, and download if available");
            System.out.println("4 -> Logout user");
            System.out.print("Enter Option: ");
            // scan users selection and use to to take appropriate action
            option = sc.next();

            //--------Option 1 (Share a file) ---------//
            if (option.equals("1")) {
                // scan users input
                sc = new Scanner(System.in);
                System.out.println("Which file would you like to share? Ex: Sample.txt");
                System.out.println("Enter file name: ");
                filename = sc.next();
                // send request to Server which communicates with the database
                String fileUploadResult = server.upload(username, filename, option);
                //print out the result from database
                System.out.println(fileUploadResult);

            //--------Option 2 (Stop Sharing a file) ---------//
            } else if (option.equals("2")) {
                // scan users input
                System.out.println("Which file would you like to stop sharing? Ex: Sample.txt");
                System.out.println("Enter file name: ");
                sc = new Scanner(System.in);
                filename = sc.next();
                // send request to Server which communicates with database to remove the filename from database

                String fileUploadResult = server.upload(username, filename, option);
                // print the result from the database
                System.out.println(fileUploadResult);

            //--------Option 3 (Search for file and then download if available) ---------//
            } else if (option.equals("3")) {
                // have string variable hold the result from the fileUpload()
                String fileUploadResult = "";

                System.out.println("Search for a shared file.  Exs: 'Sample.txt','java.jpg', 'HelloWorld.txt'");
                System.out.println("Enter file name: ");
                // scan input from user
                sc = new Scanner(System.in);
                filename = sc.next();
                // send request to Server which communicates with database
                fileUploadResult = server.upload(username, filename, option);

                // if the db finds a match and server returns back result that starts with success! parse the result
                // to extract the filename and file owner's address and port numbers
                if (fileUploadResult.startsWith("Success!")) {
                    // result from  function is 'Success! fileName + ":" + userAddress +":"+ userPort'

                    // split the result into 'Success!' and the rest of the result 'fileName + ":" + userAddress +":"+ userPort'
                    String[] comp = fileUploadResult.split(" ");
                    String client2FullAddress = comp[1];

                    // For testing purposes prints out returned string
                    //System.out.println(client2FullAddress);

                    // split the rest of the result 'fileName + ":" + userAddress +":"+ userPort'
                    String[] resultSplit = client2FullAddress.split(":");
                    // fileName
                    String client2Filename = resultSplit[0];
                    // userAddress
                    String client2Address = resultSplit[1];
                    // userPort
                    String client2Port = resultSplit[2];
                    // for testing purposes print the user's Port number to show that the client requesting file
                    // makes P2P socket connection with file owner's port
                    // System.out.println("File owner's Address is: " + client2Address);
                    // System.out.println("File owner's port number is: "+ client2Port);

                    // ask user if they want to download file.
                    // the client program shows the retrieved file name to the user without revealing who owns the file.
                    System.out.println("Result found! Did you want to download the file '" + client2Filename + "'? ");
                    String inputResult = "";
                    //do while loop: continue to ask user for input if it is invalid (ie not Y,y,N, or n)
                    do {
                        System.out.println("(Y/N): ");
                        sc = new Scanner(System.in);
                        userIn = sc.next();
                        // if input result is Yes, execute method to begin Socket connection to owener of file.
                        if (userIn.equals("Y") || userIn.equals("y")) {
                            inputResult = "Yes";

                            // Call method to open P2P Socket connection
                            // sends filename over socket and Receives the bytesream of the file
                            // and saves it to a new filename that starts with "downloaded_" to indicate that
                            // it is different from the original
                           // System.out.println(client2Address + client2Port);
                            downloadFile(client2Filename, client2Address, client2Port);

                            // else if input result is No, go back to the main menu
                        } else if (userIn.equals("N") || userIn.equals("n")) {
                            inputResult = "No";
                            // else it is an invalid input and inform user
                        } else {
                            inputResult = "invalid";
                            System.out.println("Invalid input. Please try again");

                        }
                        //do while loop (while invalid input)
                    } while (inputResult.equals("invalid"));
                    // else the result  of fileUpload() is not start with 'Success!"
                } else {
                    // if result does not start with 'success!' return back to main menu.
                    System.out.println(fileUploadResult);
                }
            }
            //--------Option 4 ---------//
             else if (option.equals("4")) {
                // if user enters 4 close program.
                System.out.println("Goodbye");
                System.exit(0);
                // End program
            }
             else {
                System.out.println("Invalid option. Please try again.");
            }
        }

    }


    //***************Client Class methods*****************//

     /*
      method to open P2P Socket connection.
      sends filename over socket and Receives the byte stream of the file
      and saves it to a new filename that starts with "downloaded_" to indicate that
      it doesnt overwrite the original file.
      Method Returns nothing since it just saves file to a folder called 'files' in Folder called TME2.
      */
    private static void downloadFile(String filename, String ownerAddress, String client2Port){
        int ownerPort =Integer.parseInt(client2Port);
        try {
            //make a new socket connection to the file's owner
            Socket sock = new Socket(ownerAddress, ownerPort);
//for testing
//            System.out.println("File Owner's Port number ["+ownerPort+"]");

            // send the filename over the socket
            OutputStream outputStream = sock.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(filename);

            //Receive file from client who owns the desired file
            int byteTotal;
            int offset = 0;
            int arrSize = 4096;
            // create an array to hold the incoming bytes over the socket
            byte[] byteArr = new byte[arrSize];

            // set up inputStream with the socket
            InputStream iStream = sock.getInputStream();

            // create a new file with prefix 'downloaded_' that will save to the file 'files'
            File downFile = new File("files/downloaded_" + filename);

            // open a file output stream to be able to write to the file
            FileOutputStream fileOStream = new FileOutputStream(downFile);

            // open buffer output stream to send file output stream through
            BufferedOutputStream buffOStream = new BufferedOutputStream(fileOStream);

            // set byteTotal to the size of the file and read bytes into byteArr
            byteTotal = iStream.read(byteArr,offset, byteArr.length);

            // write to the new file the bytes from the byteArr
            buffOStream.write(byteArr, offset, byteTotal);


            //flush the buffer output stream
            buffOStream.flush();

            //inform user that the file has been downloaded and that they can view it in the folder 'files'
            System.out.println("File '" + filename + "' downloaded as 'downloaded_"+ filename+"' to the folder 'files'");
            System.out.println("Downloaded file's size is [" + byteTotal + "] bytes");
            //close buffer output stream and file output stream
            buffOStream.close();
            fileOStream.close();
            objectOutputStream.close();
            iStream.close();

            serverSocketFunction(ownerPort);
        // in order to use program correctly, Client class needs to run as ClientA and ClientB at the same time
        }catch(Exception e){
            System.out.println("The user that owns file is not online. Please have both ClientA and ClientB run ats the same time.");
        }

    }
     /*
     Method to start a server Socket on the client side to listen for incoming connections from another client
     Since this is a P2P application, Clients act as both clients and servers in order to
     open socket connections with each other and transfer files to one another.
     Method takes file owner's port as input
      */
    public static void serverSocketFunction (final int port) {

        final ExecutorService pool = Executors.newFixedThreadPool(5);
        Runnable serverRun = new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSock = new ServerSocket(port);
                    Socket sock = serverSock.accept();
                    pool.submit(new ClientBackground(sock));
                    serverSock.close();

                }
                catch (Exception e) {

                }
            }
        };
        // create a new thread that takes serverRun as input
        Thread serverThread = new Thread(serverRun);
        // start the thread
        serverThread.start();
    }
}
