import java.util.Properties;
import java.sql.*;
import java.util.Scanner;
 
public class Program {

    public static Connection conn;
    public static Boolean admin = false;

    public static void main(String args[]) throws
            SQLException, ClassNotFoundException {
 
        
 
 
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "password");
        conn = DriverManager.getConnection(url, props);

        String commonList = "
                        \nWelcome to the Costa Train Program.\n
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
                        15) Exit\n\n ";
        String adminList = "
                        Administrator Access Options\n
                        16) Export Database\n
                        17) Delete Database\n\n";
 
        Scanner keyboard = new Scanner(System.in);
 
        //Shows the login screen to validate username and password
        loginScreen();

        while(true){
            
            System.out.println(commonList);
            if ( admin ) { System.out.println(adminList); }

            System.out.print("Input: ");
            String input = keyboard.nextLine();

            if ( input.equals("1")) { updateCustomerList(); } ///Stub
            else if ( input.equals("2") ) { updateCustomerList(); }///Stub
            else if ( input.equals("3") ) { updateCustomerList(); }///Stub
            else if ( input.equals("4") ) { updateCustomerList(); }///Stub
            else if ( input.equals("5") ) { updateCustomerList(); }///Stub
            else if ( input.equals("6") ) { updateCustomerList(); }///Stub
            else if ( input.equals("7") ) { updateCustomerList(); }///Stub
            else if ( input.equals("8") ) { updateCustomerList(); }///Stub
            else if ( input.equals("9") ) { updateCustomerList(); }///Stub
            else if ( input.equals("10") ) { updateCustomerList(); }///Stub
            else if ( input.equals("11") ) { updateCustomerList(); }///Stub
            else if ( input.equals("12") ) { updateCustomerList(); }///Stub
            else if ( input.equals("13") ) { updateCustomerList(); }///Stub
            else if ( input.equals("14") ) { updateCustomerList(); }///Stub
            else if ( input.equals("15") ) { updateCustomerList(); }///Stub
            else if ( admin && input.equals("16") ) { updateCustomerList(); }///Stub
            else if ( admin && input.equals("17") ) { updateCustomerList(); }///Stub
            else
            {
                System.out.println("Invalid Input! Enter a number in the list provided. Try Again. ");
                continue;
            }
        }
    }

    //Still in process
    public static void updateCustomerList() throws SQLException, ClassNotFoundException
    {

        System.out.println("Customer Menu\n Here a new customer can be added [1], customers' information can be updated [2],\n and a customers' entire data can be viewed [3]. \n")

        Scanner scanner = new Scanner(System.in);

        while (true)
        {

        }



       
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

    public static void loginScreen() throws SQLException, ClassNotFoundException
    {
        //Preset administrator access to false
        admin = false; 

        //Define administrator username and password
        String username = "admin";
        String password = "password";

        //Define employee username and password
        String employeeUsername = "employee";
        String employeePassword = "employee";

        String inputUsername = "";
        String inputPassword = "";

        System.out.println("\n");

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            //Prompts for the username
            System.out.print("\nUsername: ");
            inputUsername = scanner.nextLine();

            //prompts for the password
            System.out.print("Password: ");
            inputPassword = scanner.nextLine();

            //Checks the username and password. Error message otherwise. 
            if ( inputUsername.equals(username) && inputPassword.equals(password) )
            {
                admin = true;
                System.out.println("Successfull Login.\n You are logged in as an Administrator. \n")
                break; 
            }
            else if ( inputUsername.equals(employeeUsername) && inputPassword.equals(employeePassword) )
            {
                System.out.println("Successfull Login.\n You are logged in as an Employee. \n")
                break;
            }
            else { System.out.println("Invalid Credentials! Try Again. ") }
            
        }

        scanner.close();
        return;

    }
}
