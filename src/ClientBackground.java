/*
Client Background Class helps Client class make P2P Socket connections
Student name: Malgorzata Kalarus
Student id:
 */
import java.io.*;
import java.net.Socket;

public class ClientBackground implements Runnable  {
    private final Socket clientSock;

    public ClientBackground(Socket clientSock) {
        this.clientSock = clientSock;
    }

    public void run() {
//used for testing
//        System.out.println("\n- - - - - - - - - - - - - - - - - - - ");
//used for testing
//        System.out.println("You currently have a download request");


        // Send the file back to who requested it

        //unpack file request from client and save it in String input
        String input = "";
        try {
            InputStream inputStream;
            inputStream = clientSock.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            input = objectInputStream.readObject().toString();

        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
//used for testing
//       System.out.println("A user wants to download '" + input+"'");

        try {
            int offset = 0;
            // set ownersFile to the input from P2P Socket
            // Find the file in user's folder 'files'
            File ownersFile = new File("files/"+input);
            //Set Array to the size of the owner's file
            int fileSize = (int) ownersFile.length();
            byte[] byteArr = new byte[fileSize];
            //open file and read from it
            FileInputStream fileIStream = new FileInputStream(ownersFile);
            BufferedInputStream buffIStream = new BufferedInputStream(fileIStream);
            buffIStream.read(byteArr, offset, byteArr.length);


            // open and outputStream to write to client socket
            OutputStream oStream = clientSock.getOutputStream();
//used for testing
//            System.out.println("Sending '" + input + "'...\nFile size is [" + byteArr.length + "] bytes");
            // write the byteArr to over the outputStream oStream
            oStream.write(byteArr, offset, byteArr.length);


            oStream.flush();
            //close streams
            oStream.close();
            buffIStream.close();
            //oStream.close();
            fileIStream.close();

//used for testing
//            System.out.println("File sent");
//            System.out.println("- - - - - - - - - - - - - - - - - - -  ");

        }catch(Exception e){
            e.getMessage();
        }

    }

}
