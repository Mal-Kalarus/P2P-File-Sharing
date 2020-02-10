/*
Server Class : implementation of the server.idl file.
Student name: Malgorzata Kalarus
Student id:
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;
import org.omg.CORBA.*;
import ServerApp.*;

public class Server extends ServerHelpPOA{
    private ORB orb;
    private static Connection db;

    public Server(Connection dbCon) {
        super();
        db = dbCon;
    }
    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    // implement shutdown() method
    public void shutdown() {
        orb.shutdown(false);
    }

    //implement P2P application related methods //
    /*
    Method login() takes username and password as input.
    Generates connection to mySQL database using a jdbc Driver
     */
    public String login(String username, String password) {
        try {
            //Open a connection
            System.out.println("Connecting to database...");
            Statement stmt = db.createStatement();
            String sql = "SELECT * FROM users WHERE username='" + username + "' && password='" + password + "'";
            //
            // sql = "SELECT username FROM users WHERE username = '" + username + "'";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                return "User Login Successful";
            }
            return "User Login Failed";
        } catch (Exception e) {
            //Handle errors for Class.forName
            return "Exception " + e.getMessage();
        }
    }
    /*
    Method upload() takes username, filename and clientOpt as input. clientOpt determines which queries
    to perform on the database. username and filename input needed to tell database which user record it is updating.
    Method returns a string to tell the client the interpreted result of the database query

    Option 1: corresponds to sharing a file. it inserts a new record into the files_shared table column 'username' = input username
              and column 'file_name' = input filename. Perform select query to check that result was inserted correctly.
              informs user that query was successful.
    Option 2: Corresponds to stop sharing of file. it deletes the record under column 'file_name' that matches input filename and
             associated username. if there is a match inform user that file
    Option 3: Corresponds to Searching for a filename and returning Address and Port numbers to make P2P socket connection
              to download file.
              First, Query 'files_shared' table to get username who owns file.
              Second, Query 'users' table using username result from first query to find user's address and port numbers.
    Option 4: corresponds to closing Program and is handled on client side
    else: user input invalid option and is informed.
     */
    public String upload(String username, String filename, String clientOpt ) {

        try {
            //-------- processing Option 1 (Share a file) ---------//
            if (clientOpt.equals("1")) {
                try {
                    Statement stmt = db.createStatement();
                    String sql1 = "INSERT INTO files_shared (username, file_name) VALUES( '" + username + "', '" + filename + "')";
                    stmt.executeUpdate(sql1);

                    //checking to see if it was written to db. by retrieving record just inserted.
                    stmt = db.createStatement();
                    sql1= "SELECT file_name FROM files_shared WHERE (username = '"+ username +"')";
                    ResultSet rs = stmt.executeQuery(sql1);
                    String fileName = null;
                    //Extract data from result set
                    while (rs.next()) {
                        //Retrieve by column name
                        fileName = rs.getString("file_name");

                    }
                    // inform user that filename has been uploaded successfully.
                    return ("Your file: " + fileName + " was successfully shared.");

                } catch (Exception e) {
                    return "Upload Failed";
                }
            }
            //-------- processing Option 2(Stop sharing a file) ---------//
            else if(clientOpt.equals("2")) {
                try {
                    int resultStatus = 0;
                    Statement stmt = db.createStatement();
                    String sql2 = "DELETE FROM files_shared WHERE (username = '" + username + "' AND   file_name = '" + filename + "')";
                    resultStatus = stmt.executeUpdate(sql2);
                    // if there is a match and has been deleted successfully inform user else inform user that user is not sharing
                    // a file with that filename. return back to main menu.
                    if (resultStatus > 0) {
                        return "Stop Sharing file '"+ filename +"' Successful";
                    }
                    else {
                        return "Delete filename Failed - No match found";
                    }
                }catch (Exception e) {
                    return e.getMessage();
                }
            }
            //-------- processing Option 3(Search for file) ---------//
            else if(clientOpt.equals("3")) {

                //checking to see if it was writen to db.
                Statement stmt = db.createStatement();
                //Search for shared filenme in 'files_shared' table and get the username it belongs to. (use in next query)
                String sql1= "SELECT username, file_name FROM files_shared WHERE (file_name = '" + filename + "')";
                ResultSet rs = stmt.executeQuery(sql1);
                String userName = null;
                String fileName = null;

                // extract data from result set
                while (rs.next()) {
                    userName = rs.getString("username");
                    fileName = rs.getString("file_name");
                }

                //query the users table based on the userName obtained from the query above to get user's address and port numbers
                stmt = db.createStatement();
                String sql2= "SELECT user_address, user_port FROM users WHERE (username = '" + userName + "' )";
                ResultSet rs2 = stmt.executeQuery(sql2);
                String userAddress = null;
                String userPort = null;

                while (rs2.next()) {
                    userAddress = rs2.getString("user_address");
                    userPort = rs2.getString("user_port");
                }
                // if there is a match, return filename, userAddress and userPort
                // (address and port used to make P2P socket connection with this user)
                if(userAddress != null ) {
                    return ("Success! " + fileName +":" + userAddress + ":" + userPort);
                }
                else{
                    return ("No results matching '"+ filename +"'");
                }

            }
            //----user inputs invalid option----//
            else{
                return "invalid option";
            }

        } catch (Exception e) {
            return e.getMessage();
        }

    }
    /*
    Method updateProperties() takes username, user's address and port as input to update user_address and user_port columns
    in the database.
     */
    //public String updateProperties(String username, String address, int port) {

    //}
    /*
    Method to get users Address and Port numbers and updates database after user logs in.
    This will later allow for another user to open a Peer to Peer connection with that port number and download file
    that has its filename in the database.
    Method returns user's port number to be used as the server socket's port number.
   */
    public String updateDatabase(String username){
        //get the local hosts's address and port numbers
        //update the database these numbers
        String userAddress = "";
        try {
            userAddress = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception e) {
        }
        int port = 0;
        boolean portAvailable = false;
        while (!portAvailable) {
            try {
                int portNum = ThreadLocalRandom.current().nextInt(4000,  4500);
                //System.out.println("-->Testing port [" + portNum +"]");

                Socket s = null;
                try {
                    s = new Socket(userAddress, portNum);
                    //System.out.println("-->Port [" + portNum + "] is not available");
                } catch (IOException e) {
                    //System.out.println("-->Port [" + portNum + "] is available");

                    Statement stmt = db.createStatement();
                    String checkIfTaken = "SELECT * FROM users WHERE user_address = '" + userAddress + "' AND user_port = '" + portNum + "'";
                    ResultSet usersWithPort = stmt.executeQuery(checkIfTaken);
                    // If there is a result in the result set - the username and password must be correct!
                    if (!usersWithPort.next()) {
                        port = portNum;
                        //set portAvailable to true
                        portAvailable = true;

                        // update the signed in user's Address and Port number
                        //takes username, user's address and port as input to update user_address and user_port columns in the database.

                        try {
                            // perform SQL query to update the table users by setting the user_address and user_port columns
                            // to the user's address and port after signing in where column username is equal to the username from the input
                            stmt = db.createStatement();
                            String sql1= "UPDATE users SET user_address = '" + userAddress + "' , user_port = '" + port + "' WHERE username = '" + username + "'";
                            //execute SQL statement
                            stmt.executeUpdate(sql1);

                        } catch (Exception f) {
                            //Handle errors for Class.forName
                            f.printStackTrace();
                            return "Failed!";
                        }
                        String stringPort = String.valueOf(port);
                        return stringPort;

                    }
                    //db.close();
                } finally {
                    if (s != null) {
                        try {
                            //close socket
                            s.close();
                        } catch (IOException e) {
                            System.out.println("An error has been thrown: " + e.getMessage());
                        }
                    }
                }

                //Upload the address and port to the database here
            } catch (Exception e){}
        }

        return null;
    }

}

