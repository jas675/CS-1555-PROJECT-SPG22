import java.util.Properties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;

import java.text.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Program {

    public static Connection conn;
    public static Boolean admin = false;
    public static Scanner scanner;

    public static void main(String args[]) throws
            SQLException, ClassNotFoundException, ParseException, IOException {
        
        

        //JBDC basic procedures
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres"); 
        conn = DriverManager.getConnection(url, props);

        //Initlizing the main options list varibales
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
                        + "19) Trigger 2 Test\n"
                        + "20) Import Data\n\n";
 
        
        //Initializes scanner to be used by whole program
        scanner = new Scanner(System.in);

        //Shows the login screen to validate username and password
        loginScreen();

        while(true){

            try
            {

                //Prints out the options list
                System.out.println(commonList);
                if ( admin ) { System.out.println(adminList); }

                //Asks for which option to execute
                System.out.print("Input: ");
                String input = "";
                input = scanner.nextLine();

                if ( input.equals("1")) { updateCustomerList(); }
                else if ( input.equals("2") ) { single_route_trip(); }
                else if ( input.equals("3") ) { multi_route_trip(); }
                else if ( input.equals("4") ) { add_reservation(); }
                else if ( input.equals("5") ) { ticket(); }
                else if ( input.equals("6") ) { at_station(); }
                else if ( input.equals("7") ) { multi_line_routes(); } 
                else if ( input.equals("8") ) { ranked_trains(); }
                else if ( input.equals("9") ) { same_station_diff_stops(); } //In Progress (A)??
                else if ( input.equals("10") ) { station_all_trains_pass_through(); }
                else if ( input.equals("11") ) { trains_that_does_not_stop_at_station(); }
                else if ( input.equals("12") ) { pass_through_percent_stations(); }
                else if ( input.equals("13") ) { display_route_schedule(); }
                else if ( input.equals("14") ) { availability(); }
                else if ( input.equals("15") ) { loginScreen();; }
                else if ( admin && input.equals("16") ) { export(); }
                else if ( admin && input.equals("17") ) { dropAll(); }
                else if ( admin && input.equals("18") ) { update_clock(); }
                else if ( admin && input.equals("19") ) { triggerTest(); }
                else if ( admin && input.equals("20") ) { importData(); }
                else
                {
                    System.out.println("Invalid Input! Enter a number in the list provided. Try Again. ");
                    continue;
                }

                System.out.println("\n (Done; Press Enter key to Continue)");
                scanner.nextLine();
                System.out.println("\n\n\n\n");
            }
            catch ( Exception e)
            {

                System.out.println("Error in Database. " + e);
                System.out.println("Continuing....... ");
            }
        }

    }

    public static void importData() throws SQLException, ClassNotFoundException, IOException
    {

        System.out.println("Able to import data from appropriate files in repository");

        String create_table = new String(Files.readAllBytes( Paths.get("../Phase 3/new_schema-v2.sql")));
        String run_functions = new String(Files.readAllBytes( Paths.get("../Phase 3/revised_dml.sql")));
        String inserts = new String(Files.readAllBytes( Paths.get("../Phase 3/data2.sql")));

        Statement stmt = conn.createStatement();	

        
        stmt.executeUpdate(create_table);
        System.out.println("Creating Tables....DONE");

        stmt.executeUpdate(run_functions);
        System.out.println("Defining Functions.....DONE");

        stmt.executeUpdate(inserts);
        System.out.println("Adding Data.....DONE");
    }

    public static void triggerTest() throws SQLException, ClassNotFoundException
    {
        System.out.println("Functional Testing of Line Disruptions Schedule");

        System.out.print("Enter Reservation ID: ");
        String rsv_id = scanner.nextLine();

        System.out.print("Enter Train Schedule ID: ");
        String tran_sch_id = scanner.nextLine();

        Statement stmt = conn.createStatement();	

        String sql = "UPDATE RESERVATION SET no_adjustments = FALSE WHERE reservation_id = "  + rsv_id;
        stmt.executeUpdate(sql);

        sql = "UPDATE TRAINSCHEDULE SET disruption = TRUE WHERE schedule_id = "  + tran_sch_id;
        stmt.executeUpdate(sql);

        System.out.println("\nPrereqs For Trigger 2 Set. ");
    }
    
    public static void export() throws SQLException, ClassNotFoundException
    {
        //Initial message
        System.out.println("Export or View Table Data\n");

        System.out.println("1. Clock\n2. Passenger\n3. Rail Line\n4. Reservation\n5. Route\n6. Station\n7. Station Line\n8. Station Route\n9. Train\n10. Train Schdule");

        System.out.println("Pick Any Numbers Above to View Table Data");

        while (true)
        {
            System.out.print("Input: ");
            String input = scanner.nextLine();

            if ( input.equals("1") )
            {
                CallableStatement properCase = conn.prepareCall("{ ? = call get_clock_timestamp() }");
                properCase.registerOutParameter(1, Types.TIMESTAMP);
                properCase.execute();
                Timestamp rReturn = properCase.getTimestamp(1);
                properCase.close();

                System.out.println( "Current Pseudo Date and Time is : " + rReturn);
                return;
            }
            else if ( input.equals("2") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Passenger";
                ResultSet res1 = st.executeQuery(query1);
                int id;
                String fname, lname, s_addr, city, post;
                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-10s %-15s %-15s %s %s %s %n", "ID", "FIRST NAME", "LAST NAME", "ADDRESS", "", "");

                    }
                    id = res1.getInt(1);
                    fname = res1.getString(2);
                    lname = res1.getString(3);
                    s_addr = res1.getString(4);
                    city =  res1.getString(5);
                    post = res1.getString(6);
                    System.out.printf("%-10s %-15s %-15s %s, %s, %s %n", "" + id, fname, lname, s_addr, city, post);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-10s %-15s %-15s %s %s %s %n", "ID", "FIRST NAME", "LAST NAME", "ADDRESS", "", "");

                    }
                }

                if ( count == -1){ System.out.println("NO PASSENGER DATA FOUND!"); }

                return;
            }
            else if ( input.equals("3") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM RailLine";
                ResultSet res1 = st.executeQuery(query1);
                int id;
                int speed_limit;
                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-6s %s %n", "ID", "SPEED LIMIT");
                    }
                    id = res1.getInt(1);
                    speed_limit = res1.getInt(2);

                    System.out.println(id + " " + speed_limit);
                    System.out.printf("%-6s %s %n", "" + id, "" + speed_limit);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-6s %s %n", "ID", "SPEED LIMIT");

                    }
                }

                if ( count == -1){ System.out.println("NO RAIL LINE DATA FOUND!"); }

                return;
            }
            else if ( input.equals("4") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Reservation";
                ResultSet res1 = st.executeQuery(query1);
                int id, cust_id, sch_id;
                Timestamp start, end;
                float price;
                boolean ticket, adj;

                while (res1.next()) {

                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-6s %-10s %-12s %-23s %-23s %-10s %-9s %s %n", "ID", "CUST ID", "TRAIN SCH ID", "DEPARTURE", "EXPIRY", "PRICE", "TICKETED", "NO ADJUSTMENT");
                    }
                    
                    id = res1.getInt(1);
                    cust_id = res1.getInt(2);
                    sch_id = res1.getInt(3);
                    start = res1.getTimestamp(4);
                    end =  res1.getTimestamp(5);
                    price = res1.getFloat(6);
                    ticket = res1.getBoolean(7);
                    adj = res1.getBoolean(7);
                    System.out.printf("%-6s %-10s %-12s %-23s %-23s %-10s %-9s %s %n", "" + id, "" + cust_id, "" + sch_id, start, end, price, ticket, adj);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-6s %-10s %-12s %-23s %-23s %-10s %-9s %s %n", "ID", "CUST ID", "TRAIN SCH ID", "DEPARTURE", "EXPIRY", "PRICE", "TICKETED", "NO ADJUSTMENT");

                    }
                }

                if ( count == -1){ System.out.println("NO RESERVATION DATA FOUND!"); }

                return;
            }
            else if ( input.equals("5") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Route";
                ResultSet res1 = st.executeQuery(query1);
                int id;
                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%s", "ROUTE ID");
                    }

                    id = res1.getInt(1);

                    System.out.printf("%s", "" + id);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%s", "ROUTE ID");
                    }
                }

                if ( count == -1){ System.out.println("NO ROUTE DATA FOUND!"); }

                return;
            }
            else if ( input.equals("6") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Station";
                ResultSet res1 = st.executeQuery(query1);
                int id, delay;
                String name, street, town, post;
                Time start, end;

                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-6s %-7s %-12s %-12s %-6s %s %s %s %n", "ID", "STATION", "OPEN TIME", "END TIME", "DELAY", "ADDRESS", "", "");
                    }

                    id = res1.getInt(1);
                    name = res1.getString(2);
                    start = res1.getTime(4);
                    end =  res1.getTime(5);
                    delay = res1.getInt(6);
                    street = res1.getString(6);
                    town = res1.getString(7);
                    post = res1.getString(7);

                    System.out.printf("%-6s %-7s %-12s %-12s %-6s %s, %s, %s %n", "" + id, name, "" + start, "" + end, "" + delay, street, town, post);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-6s %-7s %-12s %-12s %-6s %s %s %s %n", "ID", "STATION", "OPEN TIME", "END TIME", "DELAY", "ADDRESS", "", "");

                    }
                }

                if ( count == -1){ System.out.println("NO STATION DATA FOUND!"); }

                return;
            }
            else if ( input.equals("7") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Station_Line";
                ResultSet res1 = st.executeQuery(query1);
                int id, sta_id, dist, order;

                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-8s %-8s %-10s %-8s %n", "RAIL ID", "STAT ID", "PREV DIST", "ORDER");
                    }

                    id = res1.getInt(1);
                    sta_id = res1.getInt(2);
                    dist = res1.getInt(3);
                    order = res1.getInt(4);
                    System.out.printf("%-8s %-8s %-10s %-8s %n", "" + id, "" + sta_id, "" + dist, "" + order);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-8s %-8s %-10s %-8s %n", "RAIL ID", "STAT ID", "PREV DIST", "ORDER");

                    }
                }

                if ( count == -1){ System.out.println("NO STATION LINE DATA FOUND!"); }

                return;
            }
            else if ( input.equals("8") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Station_Route";
                ResultSet res1 = st.executeQuery(query1);
                int rt_id, st_id, order;
                boolean stop;

                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-8s %-8s %-10s %-8s %n", "RT ID", "STAT ID", "STOP", "ORDER");
                    }

                    rt_id = res1.getInt(1);
                    st_id = res1.getInt(2);
                    stop = res1.getBoolean(3);
                    order = res1.getInt(4);
                    System.out.printf("%-8s %-8s %-10s %-8s %n", "" + rt_id, "" + st_id, "" + stop, "" + order);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-8s %-8s %-10s %-8s %n", "RT ID", "STAT ID", "STOP", "ORDER");

                    }
                }

                if ( count == -1){ System.out.println("NO STATION ROUTE DATA FOUND!"); }

                return;
            }
            else if ( input.equals("9") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Train";
                ResultSet res1 = st.executeQuery(query1);
                int id, seats, speed, ppk;
                String name, dscrp;


                while (res1.next()) {

                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-6s %-10s %-6s %-6s %-6s %s %n", "ID", "NAME", "SEATS", "SPEED", "$/KM", "DESCRIPTION");
                    }

                    id = res1.getInt(1);
                    name = res1.getString(2);
                    dscrp = res1.getString(3);
                    seats = res1.getInt(4);
                    speed =  res1.getInt(5);
                    ppk = res1.getInt(6);
                    
                    System.out.printf("%-6s %-10s %-6s %-6s %-6s %s %n", "" + id, name, "" + seats, "" + speed, "" + ppk, dscrp);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-6s %-10s %-6s %-6s %-6s %s %n", "ID", "NAME", "SEATS", "SPEED", "$/KM", "DESCRIPTION");

                    }
                }

                if ( count == -1){ System.out.println("NO TRAIN DATA FOUND!"); }

                return;
            }
            else if ( input.equals("10") )
            {
                int count = -1;
                Statement st = conn.createStatement();
                String query1 = "SELECT * FROM Trainschedule";
                ResultSet res1 = st.executeQuery(query1);
                int id, rt_id, tr_id;
                Time time;
                boolean disruption;
                String day;

                while (res1.next()) {
                    if ( count == -1)
                    {
                        count = 0;
                        System.out.printf("%-6s %-6s %-10s %-12s %-6s %s %n", "ID", "RTE ID", "DAY", "TIME", "TR_ID", "DISRUPTION");
                    }

                    id = res1.getInt(1);
                    rt_id = res1.getInt(2);
                    day = res1.getString(3);
                    time = res1.getTime(4);
                    tr_id =  res1.getInt(5);
                    disruption = res1.getBoolean(7);
                    System.out.printf("%-6s %-6s %-10s %-12s %-6s %s %n", "" + id , "" + rt_id, day, "" + time, "" + tr_id, "" + disruption);
                    count++;

                    if ( count == 10 )
                    {
                        count = 0;
                        System.out.print("Stop [0]: ");
                        input = scanner.nextLine();

                        System.out.println();

                        if ( input.equals("0") ) { return; }
                        System.out.printf("%-6s %-6s %-10s %-12s %-6s %s %n", "ID", "RTE ID", "DAY", "TIME", "TR_ID", "DISRUPTION");

                    }
                }

                if ( count == -1){ System.out.println("NO TRAIN SCHEDULE DATA FOUND!"); }

                return;
            }
            else 
            {
                System.out.println("Invalid input! Enter Numbers from 1 - 10 only. Try Again. ");
            }
        }
        
    }
    
    public static void station_all_trains_pass_through() throws SQLException, ClassNotFoundException
    {
        System.out.println("Printing the stations that all trains pass through\n");

        CallableStatement properCase = conn.prepareCall("{ call get_routes_that_does_not_stop_at_station() }");

        ResultSet rReturn = properCase.executeQuery();

        int count = -1;

        while ( rReturn.next() )
        {
            count++;
            int station_id = rReturn.getInt(1);
            System.out.println(station_id);
        }
        properCase.close();
        
        if ( count == -1) { System.out.println("NO APPROPRIATE STATIONS FOUND!\n"); }

        return;
    }
    
    public static void update_clock() throws SQLException, ClassNotFoundException
    {
        System.out.println("Update Clock Here:\n");

        System.out.print("Enter new year YYYY: ");
        String year = scanner.nextLine();
        System.out.print("Enter new month MM: ");
        String month = scanner.nextLine();
        System.out.print("Enter new month DD: ");
        String day = scanner.nextLine();
        System.out.print("Enter new time HH: ");
        String hour = scanner.nextLine();
        System.out.print("Enter new time MM: ");
        String minute = scanner.nextLine();
        System.out.print("Enter new time SS: ");
        String second = scanner.nextLine();


        CallableStatement properCase = conn.prepareCall("{ ? = call get_routes_that_does_not_stop_at_station(?, ?, ?, ?, ?, ?) }");

        properCase.registerOutParameter(1, Types.TIMESTAMP);
        properCase.setString(2, year);
        properCase.setString(3, month);
        properCase.setString(4, day);
        properCase.setString(5, hour);
        properCase.setString(6, minute);
        properCase.setString(7, second);

        System.out.println("Date and Time Updated Successfully. The new Date and Time is " + properCase.getTimestamp(1));


    }
    
    public static void dropAll() throws SQLException, ClassNotFoundException
    {
        Statement stmt = conn.createStatement();	

        String sql = "DROP TABLE IF EXISTS CLOCK CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS PASSENGER CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS RAILLINE CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS RESERVATION CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS ROUTE CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS STATION CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS STATION_LINE CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS STATION_ROUTE CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS TRAIN CASCADE";
        stmt.executeUpdate(sql);

        sql = "DROP TABLE IF EXISTS TRAINSCHEDULE CASCADE";
        stmt.executeUpdate(sql);

        System.out.println("Successfully Dropped All The Table In The Schema\n");
    }

    public static void single_route_trip()throws SQLException, ClassNotFoundException
    {
        System.out.print("What day of the week?: ");
        String day = scanner.nextLine();
        day = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();

        System.out.print("Enter departure stop #: ");
        int id1 = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter destination stop #: ");
        int id2 = Integer.parseInt(scanner.nextLine());
        
        System.out.println("How would you like the data ordered?");
        System.out.println(" 1) By # stations");
        System.out.println(" 2) By # stops");
        System.out.println(" 3) By time on route");
        System.out.println(" 4) By cost");

        int selection = Integer.parseInt(scanner.nextLine());

        CallableStatement call;

        if(selection == 1){

            //Set up call
            call = conn.prepareCall("{ call single_route_num_stations( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int train_id = result.getInt(2);
                    int num_stations = result.getInt(3);
                    System.out.println("Route #"+route_id + " on train #" + train_id + "... num stations = "+num_stations);
                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }
        else if(selection == 2){

            //Set up call
            call = conn.prepareCall("{ call single_route_num_stops( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int train_id = result.getInt(2);
                    int num_stops = result.getInt(3);
                    System.out.println("Route #"+route_id + " on train #" + train_id + "... num stops = "+num_stops);
                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }else if(selection == 3){
            //Set up call
            call = conn.prepareCall("{ call single_route_time( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int train_id = result.getInt(2);
                    float time = result.getFloat(3);
                    System.out.println("Route #"+route_id + " on train #" + train_id + "... time = "+time+" hours");
                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }else if(selection == 4){

            //Set up call
            call = conn.prepareCall("{ call single_route_cost( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int train_id = result.getInt(2);
                    float cost = result.getFloat(3);
                    System.out.println("Route #"+route_id + " on train #" + train_id + "... cost = $"+cost);
                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }else{
            System.out.println("Input invalid, exiting function...");
        }
    }

    public static void multi_route_trip()throws SQLException, ClassNotFoundException
    {
        System.out.print("What day of the week?: ");
        String day = scanner.nextLine();
        day = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();

        System.out.print("Enter departure stop #: ");
        int id1 = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter destination stop #: ");
        int id2 = Integer.parseInt(scanner.nextLine());
        
        System.out.println("How would you like the data ordered?");
        System.out.println(" 1) By # stations");
        System.out.println(" 2) By # stops");
        System.out.println(" 3) By time on route");
        System.out.println(" 4) By cost");

        int selection = Integer.parseInt(scanner.nextLine());

        CallableStatement call;

        if(selection == 1){

            //Set up call
            call = conn.prepareCall("{ call multi_route_num_stations( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int route_id2 = result.getInt(2);
                    int switch_station = result.getInt(3);
                    int train_id = result.getInt(4);
                    int train_id2 = result.getInt(5);
                    int num_stations = result.getInt(6);
                    System.out.println("START: Route #"+route_id + " on train #" + train_id);
                    System.out.println(" THEN: Switch via station #"+switch_station);
                    System.out.println(" THEN: Route #"+route_id2 + " on train #" + train_id2); 
                    System.out.println(" (This route passes through "+num_stations + " stations.)\n");

                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }
        else if(selection == 2){

            //Set up call
            call = conn.prepareCall("{ call multi_route_num_stops( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int route_id2 = result.getInt(2);
                    int switch_station = result.getInt(3);
                    int train_id = result.getInt(4);
                    int train_id2 = result.getInt(5);
                    int num_stops= result.getInt(6);
                    System.out.println("START: Route #"+route_id + " on train #" + train_id);
                    System.out.println(" THEN: Switch via station #"+switch_station);
                    System.out.println(" THEN: Route #"+route_id2 + " on train #" + train_id2); 
                    System.out.println(" (This route passes through "+num_stops + " stops.)\n");

                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }else if(selection == 3){
            
            //Set up call
            call = conn.prepareCall("{ call multi_route_time( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int route_id2 = result.getInt(2);
                    int switch_station = result.getInt(3);
                    int train_id = result.getInt(4);
                    int train_id2 = result.getInt(5);
                    float time = result.getFloat(6);
                    System.out.println("START: Route #"+route_id + " on train #" + train_id);
                    System.out.println(" THEN: Switch via station #"+switch_station);
                    System.out.println(" THEN: Route #"+route_id2 + " on train #" + train_id2); 
                    System.out.println(" (You will spend "+time + " hours travelling on these routes.)\n");

                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }else if(selection == 4){

            //Set up call
            call = conn.prepareCall("{ call multi_route_cost( ?, ?, ? ) }");
            call.setString(1, day);
            call.setInt(2,id1);
            call.setInt(3,id2);

            //Execute
            ResultSet result = call.executeQuery();

            //Ensure result set not empty
            if (!result.next()) { 
                System.out.println("No routes found!"); 
            } else {
                int count = 0;
                String ans = "";
                System.out.println("---------------------------------------------------");

                //For each record
                do{
                    int route_id = result.getInt(1);
                    int route_id2 = result.getInt(2);
                    int switch_station = result.getInt(3);
                    int train_id = result.getInt(4);
                    int train_id2 = result.getInt(5);
                    float cost = result.getFloat(6);
                    System.out.println("START: Route #"+route_id + " on train #" + train_id);
                    System.out.println(" THEN: Switch via station #"+switch_station);
                    System.out.println(" THEN: Route #"+route_id2 + " on train #" + train_id2); 
                    System.out.println(" (Route cost = $"+cost + "\n");

                    
                    count++;

                    //Paginate
                    if(count >= 10){
                        System.out.println("---------------------------------------------------");
                        System.out.print("(press enter to display next page, or q then enter to stop)");
                        ans = scanner.nextLine();
                        count = 0;
                        if(ans.equals("q")){
                            break;
                        }
                        System.out.println("---------------------------------------------------");
                        
                    }
                }  while(result.next());
            }

        }else{
            System.out.println("Input invalid, exiting function...");
        }
    }

    public static void at_station() throws SQLException, ClassNotFoundException, ParseException
    {
        
        System.out.print("Enter station #: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("On what day of the week?: ");
        String day = scanner.nextLine();
        day = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();

        
        DateFormat format = new SimpleDateFormat("HH:mm");

        java.sql.Time time1;
        java.sql.Time time2;

        System.out.print("Enter start time in 24-hour format: <HH:mm> : ");
        try{
            time1 = new java.sql.Time(format.parse(scanner.nextLine()).getTime());
        }catch(Exception e){
            System.out.println("Invalid Input..!  Returning to menu...");
            return;
        }

        System.out.print("Enter end time in 24-hour format: <HH:mm> : ");
        try{
            time2 = new java.sql.Time(format.parse(scanner.nextLine()).getTime());
        }catch(Exception e){
            System.out.println("Invalid Input..!  Returning to menu...");
            return;
        }

        CallableStatement call;

        //Set up call
        call = conn.prepareCall("{ call trains_at_station( ?, ?, ?, ? ) }");
        call.setInt(1, id);
        call.setString(2,day);
        call.setTime(3,time1);
        call.setTime(4,time2);

        //System.out.println(call);

        //Execute
        ResultSet result = call.executeQuery();

         if (!result.next()) { 
                System.out.println("No trains found!"); 
            } else {

                //For each record
                do{
                    int train_id = result.getInt(1);
                    String time = result.getString(4);
                    System.out.println("Train #"+train_id + " will be here ~"+time);
                    
                }  while(result.next());
            }
    }


    public static void same_station_diff_stops() throws SQLException, ClassNotFoundException{
        System.out.print("Enter route #: ");
        int id = Integer.parseInt(scanner.nextLine());

        CallableStatement call;

        call = conn.prepareCall("{ call pass_same_stop_not_same( ? ) }");
        call.setInt(1, id);

        ResultSet result = call.executeQuery();

         if (!result.next()) { 
                System.out.println("No routes with identical stations / different stops found for this route!"); 
        } else {

            //For each record
            do{
                int route_id = result.getInt(1);
                System.out.println("Route #"+route_id + " shares its stations (but not stops) with "+id);
                
            }  while(result.next());
        }
    }

    public static void availability() throws SQLException, ClassNotFoundException, ParseException{
        System.out.print("What day of the week?: ");
        String day = scanner.nextLine();
        day = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();

        
        DateFormat format = new SimpleDateFormat("HH:mm");

        java.sql.Time time1;
        java.sql.Time time2;

        System.out.print("Enter start time in 24-hour format: <HH:mm> : ");
        try{
            time1 = new java.sql.Time(format.parse(scanner.nextLine()).getTime());
        }catch(Exception e){
            System.out.println("Invalid Input..!  Returning to menu...");
            return;
        }

        System.out.print("Enter end time in 24-hour format: <HH:mm> : ");
        try{
            time2 = new java.sql.Time(format.parse(scanner.nextLine()).getTime());
        }catch(Exception e){
            System.out.println("Invalid Input..!  Returning to menu...");
            return;
        }

        CallableStatement call;

        //Set up call
        call = conn.prepareCall("{ call find_availability (?, ?, ?) }");
        call.setString(1,day);
        call.setTime(2,time1);
        call.setTime(3,time2);

        //System.out.println(call);

        //Execute
        ResultSet result = call.executeQuery();

         if (!result.next()) { 
                System.out.println("Input data invalid!  No data found."); 
            } else {
                System.out.println("The following stations will have a train available within the given time range: ");
                //For each record
                do{
                    int station_id = result.getInt(1);
                    boolean available = result.getBoolean(2);
                    if(available){
                        System.out.println("Station #"+station_id);
                    }
                    
                    
                }  while(result.next());
            }
    }

    public static void multi_line_routes() throws SQLException, ClassNotFoundException{
        String sql = "SELECT * FROM multi_line_routes";
        Statement st = conn.createStatement();
        ResultSet result = st.executeQuery(sql);

        System.out.println("The following routes traverse multiple lines:");
         while(result.next()){
            int route_id = result.getInt(1);
            System.out.println(" -- Route #"+route_id);
        }
    }

    public static void ranked_trains() throws SQLException, ClassNotFoundException{
        String sql = "SELECT * FROM ranked_trains";
        Statement st = conn.createStatement();
        ResultSet result = st.executeQuery(sql);

        System.out.println("Train ranking (by # of routes)");
         while(result.next()){
            int train_id = result.getInt(1);
            int route = result.getInt(2);
            int rank = result.getInt(3);
            System.out.println(" #"+rank+") Train #"+train_id + " with "+route+" routes.");
        }
    }


    public static void trains_that_does_not_stop_at_station()  throws SQLException, ClassNotFoundException
    {
        int id = 0;
        int count = -1;

        System.out.println("View Trains That Does Not Stop At a Station. Input Only Accepts Station ID\n");

        System.out.print("Enter Station ID: ");
        id = Integer.parseInt(scanner.nextLine());

                
        CallableStatement properCase = conn.prepareCall("{ call get_routes_that_does_not_stop_at_station( ?) }");
        //properCase.registerOutParameter(1, Types.INTEGER);
        properCase.setInt(1, id);
        //properCase.execute();
        ResultSet rReturn = properCase.executeQuery();

        System.out.printf("%-9s %-9s %s %n", "TRAIN ID", "NAME", "DESCRIPTION");

        while ( rReturn.next() )
        {
            int train_id = rReturn.getInt(1);
            String train_name = rReturn.getString(2);
            String train_descrp = rReturn.getString(3);
            System.out.printf("%-6s %-7s %s %n", "" + train_id, train_name, train_descrp);

            count = 1;
        }
        properCase.close(); 
        
        if ( count == -1 ){ System.out.println("NO APPROPRIATE STATIONS FOUND!"); }

        return;

    }

    public static void pass_through_percent_stations() throws SQLException, ClassNotFoundException
    {
        int percent = 0;
        int count = 0;

        System.out.println("View Routes That Pass Through XX% of Stations In Its Path. Input Only Accepts 10 - 90 Integer\n");

        System.out.print("Enter Route ID: ");
        percent = Integer.parseInt(scanner.nextLine());

                
        CallableStatement properCase = conn.prepareCall("{ call get_routes_that_stop_xx_stations( ?) }");
        //properCase.registerOutParameter(1, Types.INTEGER);
        properCase.setInt(1, percent);
        //properCase.execute();
        ResultSet rReturn = properCase.executeQuery();

        System.out.printf("%-9s %s %n", "ROUTE", "PERCENT");

        while ( rReturn.next() )
        {
            int route_id = rReturn.getInt(1);
            int percentages = rReturn.getInt(2);
            System.out.printf("%-9s %s %n", "" + route_id, percentages + "%");
        }
        properCase.close();
        
        if ( count == -1){ System.out.println("NO APPROPRIATE DATA FOUND. "); }

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

        System.out.printf("%-9s %-10s %-10s %-6s %-7s %s %n", "SCH ID", "DATE", "TIME", "TRAIN ID", "NAME", "DESCRIPTION");

        while ( rReturn.next() )
        {
            int sch_id = rReturn.getInt(1);
            String day = rReturn.getString(2);
            String hour = rReturn.getString(3);
            int train_id = rReturn.getInt(4);
            String train_name = rReturn.getString(5);
            String dscrp = rReturn.getString(6); 
            System.out.printf("%-9s %-10s %-10s %-6s %-7s %s %n", "" + sch_id, "" + day, "" + hour, "" + train_id, train_name, dscrp);
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
        String password = "root";

        //Define employee username and password
        String employeeUsername = "employee";
        String employeePassword = "employee";

        String inputUsername = "";
        String inputPassword = "";

        System.out.println("\n");

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

        return;

    }
}
