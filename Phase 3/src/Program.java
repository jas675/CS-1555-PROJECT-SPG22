import java.util.Properties;
import java.sql.*;
import java.util.Scanner;

import java.text.ParseException;

import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Program {

    public static Connection conn;
    public static Boolean admin = false;
    public static Scanner scanner;

    public static void main(String args[]) throws
            SQLException, ClassNotFoundException, ParseException {
 
        
 
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
            else if ( input.equals("2") ) { single_route_trip(); }
            else if ( input.equals("3") ) { multi_route_trip(); }
            else if ( input.equals("4") ) { add_reservation(); }
            else if ( input.equals("5") ) { ticket(); }
            else if ( input.equals("6") ) { at_station(); }
            else if ( input.equals("7") ) { multi_line_routes(); } 
            else if ( input.equals("8") ) { ranked_trains(); }
            else if ( input.equals("9") ) { same_station_diff_stops(); } //In Progress (A)??
            else if ( input.equals("10") ) { updateCustomerList(); }///Stub -- J
            else if ( input.equals("11") ) { trains_that_does_not_stop_at_station(); }
            else if ( input.equals("12") ) { pass_through_percent_stations(); }
            else if ( input.equals("13") ) { display_route_schedule(); }
            else if ( input.equals("14") ) { availability(); } //In Progress (A)??
            else if ( input.equals("15") ) { loginScreen(); } //DONE
            else if ( admin && input.equals("16") ) { updateCustomerList(); }///Stub -- J
            else if ( admin && input.equals("17") ) { updateCustomerList(); }///Stub -- J
            else if ( admin && input.equals("18") ) { updateCustomerList(); }///Stub -- J
            else if ( admin && input.equals("19") ) { updateCustomerList(); }///Stub
            else
            {
                System.out.println("Invalid Input! Enter a number in the list provided. Try Again. ");
                continue;
            }

            System.out.println("\n (done; press enter to continue)");
            scanner.nextLine();
            System.out.println("\n\n\n\n");
             
        }

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
        String password = "root";

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
