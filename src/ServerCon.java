/*
ServerCon Class: Register JDBC driver, Open a connection,
Handels following CORBA apects of application: creates and initializes CORBA ORB,
get reference to root poa & activate the POAManager, create servant instance and register it with the ORB,
get object reference from the servant, get the root naming context (NameService invokes the name service),
bind the Object Reference in Naming,  and waits for invocations from clients.

Student name: Malgorzata Kalarus
Student id:
 */

import java.sql.Connection;
import java.sql.DriverManager;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import ServerApp.*;

public class ServerCon {
    private final static String dbURL = "jdbc:mysql://localhost/tme2";
    private final static String dbUsername = "user";
    private final static String dbPassword = "password";

    public static void main(String[] args) {
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            //Open a connection
            System.out.println("Connecting to database...");
            Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword);

            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            Server server = new Server(conn);
            server.setORB(orb);


            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(server);
            ServerHelp href = ServerHelpHelper.narrow(ref);

            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            String name = "P2P";
            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, href);

            System.out.println("Server is ready and waiting ...");

            // wait for invocations from clients
            orb.run();
        }

        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("HelloServer Exiting ...");


    }
}
