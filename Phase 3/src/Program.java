import java.util.Properties;
import java.sql.*;
import java.util.Scanner;
 
public class Program {

    public static Connection conn;

    public static void main(String args[]) throws
            SQLException, ClassNotFoundException {
 
        
 
 
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "password");
        conn = DriverManager.getConnection(url, props);
 
        Scanner keyboard = new Scanner(System.in);
 
        String username = "admin";
        String password = "password";
 
        if(keyboard.nextLine() != username || keyboard.nextLine() != password){
            System.out.println("Invalid Username/Password!");
            return;
 
        }

        while(true){
            String str = """
                        Welcome to the Costa Train Program.\n
                        Select a number from the options below.\n
                        1) Update customer list\n
                        2) Single route trip\n
                        3) Combination (2) route trip\n
                        4) Make reservation\n
                        5) Get ticket\n
                        6) Find all trains that pass through a specific station \n
                        7) Find the routes that travel more than one rail line \n
                        8) Rank the trains that are scheduled for more than one route.\n 
                        9) Find routes that pass through the same stations but don't have the same stops\n
                        10) Find any stations through which all trains pass through\n
                        11) Find all the trains that do not stop at a specific station\n
                        12) Find routes that stop at least at XX% of the Stations they visit\n
                        13) Display the schedule of a route\n
                        14) Find the availability of a route at every stop on a specific day and time\n
                        15) Exit\n\n """;
            System.out.println(str);
            String input = keyboard.nextLine();
            switch(input){
                case "1":  updateCustomerList(); //This function not defined yet
                        break;
                default: System.out.println("Invalid input recieved.");
            }
        }
    }

    public static void updateCustomerList() throws
            SQLException, ClassNotFoundException{
       
       /* Statement st = conn.createStatement();
        String query1 =
                "SELECT SID, Name, Major FROM STUDENT WHERE Major='CS'";
        ResultSet res1 = st.executeQuery(query1);
        String rid;
        String rname, rmajor;
        while (res1.next()) {
            rid = res1.getString("SID");
            rname = res1.getString("Name");
            rmajor = res1.getString(3);
            System.out.println(rid + " " + rname + " " + rmajor);
        }
        */
    }
}
