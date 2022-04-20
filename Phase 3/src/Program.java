import java.util.Properties;
import java.sql.*;
import java.util.Scanner;
 
public class Program {

    public static Connection conn;
    public static Boolean admin = false;
    public static Scanner scanner;

    public static void main(String args[]) throws
            SQLException, ClassNotFoundException {
 
        
 
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres");
        conn = DriverManager.getConnection(url, props);

        System.out.println("Connected to SWL");
        //updateCustomerList();

        String commonList = "\nWelcome to the Costa Train Program.\n"
                        + "Select a number from the options below.\n"
                        + "1) Update customer list\n"
                        + "2) Single route trip\n"
                        + "3) Combination (2) route trip\n"
                        + "4) Make reservation\n"
                        + "5) Get ticket\n"
                        + "6) Find all trains that pass through a specific station \n"
                        + "7) Find the routes that travel more than one rail line \n"
                        + "8) Rank the trains that are scheduled for more than one route.\n"
                        + "9) Find routes that pass through the same stations but don't have the same stops\n"
                        + "10) Find any stations through which all trains pass through\n"
                        + "11) Find all the trains that do not stop at a specific station\n"
                        + "12) Find routes that stop at least at XX% of the Stations they visit\n"
                        + "13) Display the schedule of a route\n"
                        + "14) Find the availability of a route at every stop on a specific day and time\n"
                        + "15) Exit\n\n ";
        String adminList = "Administrator Access Options\n"
                        + "16) Export Database\n"
                        + "17) Delete Database\n"
                        + "18) Update Clock\n"
                        + "19) Query\n\n";
 
        
 
        scanner = new Scanner(System.in);
        //Shows the login screen to validate username and password
        loginScreen();

        while(true){

            
            System.out.println(commonList);
            if ( admin ) { System.out.println(adminList); }

            System.out.print("Input: ");
            String input = "";
            input = scanner.nextLine();

            if ( input.equals("1")) { updateCustomerList(); }
            else if ( input.equals("2") ) { updateCustomerList(); }///Stub
            else if ( input.equals("3") ) { updateCustomerList(); }///Stub
            else if ( input.equals("4") ) { add_reservation(); }
            else if ( input.equals("5") ) { ticket(); }
            else if ( input.equals("6") ) { updateCustomerList(); }///Stub
            else if ( input.equals("7") ) { updateCustomerList(); }///Stub
            else if ( input.equals("8") ) { updateCustomerList(); }///Stub
            else if ( input.equals("9") ) { updateCustomerList(); }///Stub
            else if ( input.equals("10") ) { updateCustomerList(); }///Stub
            else if ( input.equals("11") ) { trains_that_does_not_stop_at_station(); }
            else if ( input.equals("12") ) { pass_through_percent_stations(); }
            else if ( input.equals("13") ) { display_route_schedule(); }
            else if ( input.equals("14") ) { updateCustomerList(); }///Stub
            else if ( input.equals("15") ) { loginScreen();; }
            else if ( admin && input.equals("16") ) { updateCustomerList(); }///Stub
            else if ( admin && input.equals("17") ) { updateCustomerList(); }///Stub
            else if ( admin && input.equals("18") ) { updateCustomerList(); }///Stub
            else if ( admin && input.equals("19") ) { updateCustomerList(); }///Stub
            else
            {
                System.out.println("Invalid Input! Enter a number in the list provided. Try Again. ");
                continue;
            }

            //keyboard.close();
        }

    }

    public static void trains_that_does_not_stop_at_station()  throws SQLException, ClassNotFoundException
    {
        int id = 0;

        System.out.println("View Routes That Does Not Stop At a Station. Input Only Accepts Station ID\n");

        System.out.print("Enter Station ID: ");
        id = Integer.parseInt(scanner.nextLine());

                
        CallableStatement properCase = conn.prepareCall("{ call get_routes_that_does_not_stop_at_station( ?) }");
        //properCase.registerOutParameter(1, Types.INTEGER);
        properCase.setInt(1, id);
        //properCase.execute();
        ResultSet rReturn = properCase.executeQuery();

                    //System.out.println("ID    FIRST NAME    LAST NAME    ADDRESS");

        while ( rReturn.next() )
        {
            int train_id = rReturn.getInt(1);
            String train_name = rReturn.getString(2);
            String train_descrp = rReturn.getString(3);
            System.out.println(train_id + "    " + train_name + "    " + train_descrp);
        }
        properCase.close();                

        return;

    }

    public static void pass_through_percent_stations() throws SQLException, ClassNotFoundException
    {
        int percent = 0;

        System.out.println("View a Routes That Pass Through XX% of Stations In Its Path. Input Only Accepts 10 - 90 Integer\n");

        System.out.print("Enter Route ID: ");
        percent = Integer.parseInt(scanner.nextLine());

                
        CallableStatement properCase = conn.prepareCall("{ call get_routes_that_stop_xx_stations( ?) }");
        //properCase.registerOutParameter(1, Types.INTEGER);
        properCase.setInt(1, percent);
        //properCase.execute();
        ResultSet rReturn = properCase.executeQuery();

                    //System.out.println("ID    FIRST NAME    LAST NAME    ADDRESS");

        while ( rReturn.next() )
        {
            int route_id = rReturn.getInt(1);
            int percentages = rReturn.getInt(2);
            System.out.println(route_id + "    " + percentages + "%");
        }
        properCase.close();                

        return;

    }

    //Employyee and admistrators can make no mistakes when inputtng data. 
    public static void display_route_schedule() throws SQLException, ClassNotFoundException
    {
        int id = 0;

        System.out.println("View a Route Schedule. Input Only Accepts Route ID\n");

        System.out.print("Enter Route ID: ");
        id = Integer.parseInt(scanner.nextLine());

                
        CallableStatement properCase = conn.prepareCall("{ call display_sch_of_routes( ?) }");
        //properCase.registerOutParameter(1, Types.INTEGER);
        properCase.setInt(1, id);
        //properCase.execute();
        ResultSet rReturn = properCase.executeQuery();

                    //System.out.println("ID    FIRST NAME    LAST NAME    ADDRESS");

        while ( rReturn.next() )
        {
            int sch_id = rReturn.getInt(1);
            String day = rReturn.getString(2);
            String hour = rReturn.getString(3);
            int train_id = rReturn.getInt(4);
            String train_name = rReturn.getString(5);
            String dscrp = rReturn.getString(6); 
            System.out.println(sch_id + "    " + day + "    " + hour + "    " + train_id + "   " + train_name + "   " + dscrp);
        }
        properCase.close();                

        return;
    }

    public static void ticket() throws SQLException, ClassNotFoundException
    {
        while ( true )
        {
            System.out.println("Reservation Ticket Menu\n You will need Reservation ID to ticket your reservation. \n");


            System.out.print("Enter 1 to go back to menu, Any other number to continue: ");
            String input = scanner.nextLine();
            if ( input.equals("1") ) { return; }

            int rsv_id = 0;

            System.out.println("Ticket Reservation\n");
            System.out.print("Enter Reservation ID: ");
            rsv_id = Integer.parseInt(scanner.nextLine());

            CallableStatement properCase = conn.prepareCall("{ call get_ticket( ? ) }");
            //properCase.registerOutParameter(1, Types.INTEGER);
            properCase.setInt(1, rsv_id);
            properCase.execute();
            properCase.close();

            System.out.println("Reservation Ticketed! \n");

            break;

        }

    }



    public static void add_reservation() throws SQLException, ClassNotFoundException
    {
        while ( true )
        {
            System.out.println("Reservation Menu\n You will need customer ID and Train Schedule ID to book a reservation. \n");

            

            System.out.print("Enter 1 to go back to menu, Any other number to continue: ");
            String input = scanner.nextLine();
            if ( input.equals("1") ) { return; }

            int cust_id = 0;
            int sch_id = 0;

            System.out.println("Add Reservation\n");
            System.out.print("Enter Customer ID: ");
            cust_id = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Train Schedule ID: ");
            sch_id = Integer.parseInt(scanner.nextLine());

            CallableStatement properCase = conn.prepareCall("{ ? = call book_reservation( ?, ? ) }");
            properCase.registerOutParameter(1, Types.INTEGER);
            properCase.setInt(2, cust_id);
            properCase.setInt(3, sch_id);
            properCase.execute();
            int rReturn = properCase.getInt(1);
            properCase.close();

            if ( rReturn == -1 ) { System.out.println("There aren't enough seats for that Train Schedule. Please choose another one. \n"); }
            else { System.out.println("Train Reservation Successfully Made. The Reservation ID is " + rReturn); }

            break;

        }

    }

    //
    public static void updateCustomerList() throws SQLException, ClassNotFoundException
    {
        String input = "";

        while ( true)
        {
            System.out.println("Customer Menu\n Here a new customer can be added [1], customers' information can be updated [2],\n a customers' entire data can be viewed [3], Back[4] \n");

            //Scanner scanner = new Scanner(System.in);

            System.out.print("Input: ");
            input = scanner.nextLine();

            if ( input.equals("1") )
            {
                String fname = "";
                String lname = "";
                String addr = "";
                String city = "";
                String zip = "";

                System.out.println("Add A New Customer\n");

                System.out.print("Enter First Name: ");
                fname = scanner.nextLine();
                System.out.print("Enter Last Name: ");
                lname = scanner.nextLine();
                System.out.print("Enter Street Address: ");
                addr = scanner.nextLine();
                System.out.print("Enter City: ");
                city = scanner.nextLine();
                System.out.print("Enter State and Zip: ");
                zip = scanner.nextLine();

                CallableStatement properCase = conn.prepareCall("{ ? = call add_passenger( ?, ?, ?, ?, ? ) }");
                properCase.registerOutParameter(1, Types.INTEGER);
                properCase.setString(2, fname);
                properCase.setString(3, lname);
                properCase.setString(4, addr);
                properCase.setString(5, city);
                properCase.setString(6, zip);
                properCase.execute();
                int rReturn = properCase.getInt(1);
                properCase.close();

                if ( rReturn == 0){ System.out.println("Unable to make a new customer profile! Try Again. "); }
                else { System.out.println("Successfully made a customer profile. The customer id is " + rReturn); }
                
                return;
            }
            else if ( input.equals("2") )
            {
                String fname = "";
                String lname = "";
                String addr = "";
                String city = "";
                String zip = "";
                int id = 0;

                System.out.println("Edit A Customer. If the field doesn't need to be changed, just press Enter key.\n");

                System.out.print("Enter Cutomer ID or 0 if not known: ");
                id = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter New First Name or Enter if same: ");
                fname = scanner.nextLine();
                System.out.print("Enter New Last Name or Enter if same: ");
                lname = scanner.nextLine();
                System.out.print("Enter New Street Address or Enter if same: ");
                addr = scanner.nextLine();
                System.out.print("Enter New City  or Enter if same: ");
                city = scanner.nextLine();
                System.out.print("Enter New State and Zip  or Enter if same: ");
                zip = scanner.nextLine();

                CallableStatement properCase = conn.prepareCall("{ ? = call edit_passenger( ?, ?, ?, ?, ? ) }");
                properCase.registerOutParameter(1, Types.INTEGER);
                properCase.setInt(1,id);
                properCase.setString(2, fname);
                properCase.setString(3, lname);
                properCase.setString(4, addr);
                properCase.setString(5, city);
                properCase.setString(6, zip);
                properCase.execute();
                int rReturn = properCase.getInt(1);
                properCase.close();

                if ( rReturn == 0 ){ System.out.println("Unable to update customer profile! Try Again. "); }
                else { System.out.println("Successfully updated customer profile. The customer id is " + rReturn); }

                
                return;

            }
            else if ( input.equals("3") )
            {
                int id = 0;
                String fname = "";
                String lname = "";
                String addr = "";
                String city = "";
                String zip = "";

                System.out.println("View a Customer Info. If the field isn't known, just press Enter key \n");

                System.out.print("Enter Cutomer ID or 0 if not known: ");
                id = Integer.parseInt(scanner.nextLine());

                if ( id == 0 )
                {
                    System.out.print("Enter First Name: ");
                    fname = scanner.nextLine();
                    System.out.print("Enter Last Name: ");
                    lname = scanner.nextLine();
                    System.out.print("Enter Street Address: ");
                    addr = scanner.nextLine();
                    System.out.print("Enter City: ");
                    city = scanner.nextLine();
                    System.out.print("Enter State and Zip: ");
                    zip = scanner.nextLine();
                }

                
                CallableStatement properCase = conn.prepareCall("{ ? = call view_passenger( ?, ?, ?, ?, ? ) }");
                //properCase.registerOutParameter(1, Types.);
                properCase.setInt(1, id);
                properCase.setString(2, fname);
                properCase.setString(3, lname);
                properCase.setString(4, addr);
                properCase.setString(5, city);
                properCase.setString(6, zip);
                //properCase.execute();
                ResultSet rReturn = properCase.executeQuery();

                    //System.out.println("ID    FIRST NAME    LAST NAME    ADDRESS");

                while ( rReturn.next() )
                {
                    int cust_id = rReturn.getInt(1);
                    String f_name = rReturn.getString(2);
                    String l_name = rReturn.getString(3);
                    String s_addr = rReturn.getString(4);
                    String cty = rReturn.getString(5);
                    String post = rReturn.getString(6);
                    System.out.println(cust_id + "    " + f_name + "    " + l_name + "    " + s_addr + ", " + cty + ", " + post);
                }
                properCase.close();                

                return;

            }
            else if ( input.equals("4") )
            {
                return;
            }
            else
            {
                System.out.println("Invalid Input! Input only 1, 2, or 3 appropriately. \n");
                continue;
            }
        }

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

        //Scanner scanner = new Scanner(System.in);

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
                System.out.println("Successfull Login.\n You are logged in as an Administrator. \n");
                break; 
            }
            else if ( inputUsername.equals(employeeUsername) && inputPassword.equals(employeePassword) )
            {
                System.out.println("Successfull Login.\n You are logged in as an Employee. \n");
                break;
            }
            else { System.out.println("Invalid Credentials! Try Again. "); }
            
        }

        //scanner.close();
        return;

    }
}
