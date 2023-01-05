/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
	    System.out.println("3. Bypass");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
		case 3: authorisedUser = "admin"; break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
		 String query = String.format("SELECT type FROM Users WHERE login = '%s'", authorisedUser);
                 List<List<String>> result = esql.executeQueryAndReturnResult(query);
                 String userType = result.get(0).get(0);

                 boolean keepon1 = true;
                 while(keepon1 == true){
                        if(userType.equals("Customer")){
				System.out.println("MAIN MENU");
		                System.out.println("---------");
                                System.out.println("1. View menu");
                                System.out.println("2. Item search");
                                System.out.println("3. Search for item category");
                                System.out.println("4. Update Information");
                                System.out.println("5. Modify order");
                                System.out.println("6. Add order");
                                System.out.println("7. Browse Order History");
				System.out.println(".........................");
                                System.out.println("9. Log Out");
                                switch (readChoice()){
                                        case 1: Print_Menu(esql);
                                        break;
                                        case 2: searchItemName(esql);
                                        break;
                                        case 3: searchItemCategory(esql);
                                        break;
                                        case 4: UpdateProfile(esql, authorisedUser, userType);
                                        break;
                                        case 5: UpdateOrder(esql, authorisedUser, userType);
                                        break;
                                        case 6: PlaceOrder(esql, authorisedUser);
                                        break;
                                        case 7: BrowseOrderHistory(esql, authorisedUser, userType);
                                        break;
                                        case 9: keepon1 = false;
                                        break;
                                        default : System.out.println("Error: invalid choice!");
                                        break;
                                }
                        }
			 else if(userType.equals("Employee")){
				System.out.println("MAIN MENU");
                                System.out.println("---------");
                                System.out.println("1. View Menu");
                                System.out.println("2. Item search");
                                System.out.println("3. Search for item category");
                                System.out.println("4. Update Information");
				System.out.println("5. Add Order");
                                System.out.println("6. Browse unpaid orders");
                                System.out.println("7. Update order status");
                                System.out.println(".........................");
                                System.out.println("9. Log Out");
                                switch(readChoice()){
                                        case 1: Print_Menu(esql);
                                        break;
                                        case 2: searchItemName(esql);
                                        break;
                                        case 3: searchItemCategory(esql);
                                        break;
                                        case 4: UpdateProfile(esql, authorisedUser, userType);
                                        break;
					case 5: PlaceOrder(esql, authorisedUser);
                                        break;
                                        case 6: BrowseOrderHistory(esql, authorisedUser, userType);
                                        break;
                                        case 7: UpdateOrder(esql, authorisedUser, userType);
                                        break;
                                        case 9: keepon1 = false;
                                        break;
                                        default : System.out.println("Error: invalid choice!");
                                        break;
                                }
                        }
			else if(userType.equals("Manager ")){
				System.out.println("MAIN MENU");
                                System.out.println("---------");
                                System.out.println("1. View Menu");
                                System.out.println("2. Item Search");
                                System.out.println("3. Search for item category");
                                System.out.println("4. Update User's Information");
				System.out.println("5. Add Order");
                                System.out.println("6. Modify menu");
                                System.out.println("7. Browse unpaid orders");
                                System.out.println("8. Update order status");
                                 System.out.println(".........................");
                                System.out.println("9. Log Out");
                                switch(readChoice()){
                                        case 1: Print_Menu(esql);
                                        break;
                                        case 2: searchItemName(esql);
                                        break;
                                        case 3: searchItemCategory(esql);
                                        break;
                                        case 4: UpdateProfile(esql, authorisedUser, userType);
                                        break;
					case 5: PlaceOrder(esql, authorisedUser);
                                        break;
                                        case 6: ModifyMenu(esql);
                                        break;
                                        case 7: BrowseOrderHistory(esql, authorisedUser, userType);
                                        break;
                                        case 8: UpdateOrder(esql, authorisedUser, userType);
                                        break;
                                        case 9: keepon1 = false;
                                        break;
                                        default : System.out.println("Error: invalid choice!");
                                        break;
                                }
                        }
		}
	}
		
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
	return;
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here


//for all
public static void Print_Menu(Cafe esql){
	try {
		System.out.println("Menu:"	);
		String query = "SELECT * FROM MENU";
		int complete = esql.executeQueryAndPrintResult(query);
	}catch(Exception e){
		System.err.println(e.getMessage());
		return;
	}
}

//add delete modify menu
//only for manager
public static void ModifyMenu(Cafe esql) {
	try{
		boolean modify = true;
            while (modify){
                System.out.println("1. Add Item");
                System.out.println("2. Delete Item");
                System.out.println("3. Modify Item");
                System.out.println("9. Exit Modify Menu");
                switch (readChoice()){
                    case 1: //add item
			System.out.println("New Item: ");
                        String newItem = in.readLine();
                        System.out.println("Type: ");
                        String itemType = in.readLine();
                        System.out.println("Price: ");
                        double itemPrice = Double.parseDouble(in.readLine());
                        System.out.println("Description: ");
                        String itemDesc = in.readLine();
                        System.out.println("imageURL: ");
                        String itemImageURL = in.readLine();

                        String addQuery = String.format("INSERT INTO MENU (itemName, type, price, description, imageURL) VALUES ('%s','%s','%f','%s','%s')", newItem, itemType, itemPrice, itemDesc, itemImageURL);
                        esql.executeUpdate(addQuery);
                        System.out.println("Item successfully added!");
                        break;
                    case 2: //delete item
			System.out.println("What item do you want to delete?");
                        String deleteItem = in.readLine().toLowerCase();
			String deleteMenuQuery = String.format("SELECT itemName FROM MENU WHERE LOWER(itemName) = '%s'", deleteItem);
                        if (esql.executeQueryAndPrintResult(deleteMenuQuery) != 0){
                            String deleteMenuQuery1 = String.format("DELETE FROM MENU WHERE LOWER(itemName) = '%s'", deleteItem);
                            esql.executeUpdate(deleteMenuQuery1);
                            System.out.println("Item successfully deleted");
                        }else{
                            System.out.println("Item is not in the menu.");
                        }
                        break;
                    case 3: //modify item
			System.out.println("Which item do you want to modify?");
                        String modifyItem = in.readLine().toLowerCase();
			String modifyQuery = String.format("SELECT itemName FROM MENU WHERE LOWER(itemName) = '%s'", modifyItem);
                        if (esql.executeQueryAndPrintResult(modifyQuery) != 0){
                            System.out.println("What would you like to modify?");
                            System.out.println("1. Item Name");
                            System.out.println("2. Item Type");
                            System.out.println("3. Item Price");
                            System.out.println("4. Item Description");
                            System.out.println("5. Item Image URL");
                            System.out.println("9. Exit Modify Menu");
                            int number = readChoice();
                            System.out.println("What would you like to modify it to?");
                            String modification = in.readLine();
				boolean execute = true;
				String modQuery = "";
                            if (number == 1){
                                modQuery = String.format("UPDATE MENU SET itemName = '%s' WHERE LOWER(itemName) = '%s'", modification, modifyItem);   
			    }
                            else if (number == 2){
                                modQuery = String.format("UPDATE MENU SET type = '%s' WHERE LOWER(itemName) = '%s'", modification, modifyItem);
                            }
                            else if (number == 3){
                                modQuery = String.format("UPDATE MENU SET price = '%s' WHERE LOWER(itemName) = '%s'", modification, modifyItem);
                            }
                            else if (number == 4){
                                modQuery = String.format("UPDATE MENU SET description = '%s' WHERE LOWER(itemName) = '%s'", modification, modifyItem);
                            }
                            else if (number == 5){
                                modQuery = String.format("UPDATE MENU SET imageURL = '%s' WHERE LOWER(itemName) = '%s'", modification, modifyItem);
                            }
                            else if (number == 9){
                                 break;
                            }
                            else {
                                System.out.println("Error! Not an option to modify");
				execute = false;
                            }
			    if(execute){ 
                            	esql.executeUpdate(modQuery);
				System.out.println("Item successfully updated");
			    }
                        }else{
                            System.out.println("Item is not in the menu.");
                        }
                        break;
                    case 9:
                        modify = false;
                        break;
                    default:
                        System.out.println("Unrecognized choice!");
                        break;
                }
            }
	}catch(Exception e){
		System.err.println(e.getMessage());
		return;
	}
}

//manager and employee are the same: view unpaid orders in last 24 hours
//customer by itself view 5 most recent order history
public static void BrowseOrderHistory(Cafe esql, String authUser, String userType) {
        try{
        	if(userType.equals("Manager ") || userType.equals("Employee")){
               // String timeQuery = String.format("SELECT CURRENT_TIMESTAMP");
                 //               List<Lists<String>>  currTime = esql.executeQueryAndReturnResult(timeQuery);
                   //             String currTime2 = currTime.get(0).get(0);
			String HQuery = String.format("SELECT * FROM ORDERS WHERE timeStampRecieved > (NOW()- INTERVAL '24 HOUR') AND paid = false GROUP BY orderid");
                	int history = esql.executeQueryAndPrintResult(HQuery);
                }else if(userType.equals("Customer")){
                    String query = String.format("SELECT * FROM ORDERS WHERE login = '%s' ORDER BY orderid DESC LIMIT 5", authUser);
                    int history = esql.executeQueryAndPrintResult(query);
                }else{ 
			System.out.println("Error: invalid choice! Browse History");
		} 
           
	}catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
}

//for all
 public static void searchItemName(Cafe esql) {
	try{
         	System.out.println("What item do you want to search for?");
            String searchItem = in.readLine().toLowerCase();
            //check to see if item exists
            String query = String.format("SELECT * FROM MENU WHERE LOWER(itemName) = '%s'", searchItem);
            if (esql.executeQueryAndPrintResult(query) != 0){
            	System.out.println("Item found!");
            }else{
       		     System.out.println("Item Not Found");
            }       
        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }

}

//for all
 public static void searchItemCategory(Cafe esql) {
	try{
		System.out.println("What item Category do you want to search for?");
            	String searchItemCat = in.readLine().toLowerCase();
		String query = String.format("SELECT * FROM MENU WHERE LOWER(type) = '%s'", searchItemCat);
		if ( esql.executeQueryAndPrintResult(query) != 0){
                	System.out.println("Item found!");
            	}else{
                	System.out.println("Item Not Found");
            	}
        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }

}

//customer and employee can only update their user information
//manager can update any users information
public static void UpdateProfile(Cafe esql, String authUser, String userType){
	try{
		
		if(userType.equals("Manager ")){
                    boolean update = true;
                    while(update){
                        System.out.println("What would you like to modify?");
                        System.out.println("1. My profile");
                        System.out.println("2. Users Profile");
                        System.out.println("9. Exit Modify Menu");
                        switch(readChoice()){
                        case 1:
                               	boolean myUpdate = true;
                               	while(myUpdate){
                              	System.out.println("What would you like to modify?");
                               	System.out.println("1. Password");
                               	System.out.println("2. Favorite Items");
                               	System.out.println("3. Phone Number");
                               	System.out.println("9. Exit Modify Menu");
                               	switch(readChoice()){
                                case 1:
                                       	System.out.println("New Password: ");
                                      	String newPassword = in.readLine();
                                       	String query = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s'", newPassword, authUser);
                                       	esql.executeUpdate(query);
                                       	break;
                                case 2:
                                       	System.out.println("New Favorite Items: ");
                                       	String newFavItems = in.readLine();
                                       	String query1 = String.format("UPDATE USERS SET favItems = '%s' WHERE login = '%s'", newFavItems, authUser);
                                       	esql.executeUpdate(query1);
                                       	break;
                                case 3:
                                       	System.out.println("New Phone Number: ");
                                       	String newPhone = in.readLine();
                                       	String query2 = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s'", newPhone, authUser);
                                       	esql.executeUpdate(query2);
                                       	break;
                                case 9:
                                       	myUpdate = false;
                                       	break;
                                default:
                                       	System.out.println("Unrecognized choice!");
                                       	break;
                                    }
                                }
                                break;
                	case 2:
                                System.out.println("User login: ");
                                String profile = in.readLine().toLowerCase();
                                String userQuery = String.format("SELECT login FROM USERS WHERE LOWER(login) = '%s'", profile);
				if (esql.executeQueryAndPrintResult(userQuery) != 0){
                                    boolean userUpdate = true;
                                    while(userUpdate){
                                        System.out.println("What would you like to modify?");
                                        System.out.println("1. Password");
                                        System.out.println("2. Favorite Items");
                                        System.out.println("3. Phone Number");
                                        System.out.println("4. User type");
                                        System.out.println("9. Exit Modify Menu");
                                        switch(readChoice()){
						case 1:
							System.out.println("New Password: ");
                                                	String newPassword = in.readLine();
                                                	String query3 = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s'", newPassword, profile);
                                                	esql.executeUpdate(query3);
                                                	break;
                                            	case 2:
                                                	System.out.println("New Favorite Items: ");
                                                	String newFavItems = in.readLine();
                                                	String query4 = String.format("UPDATE USERS SET favItems = '%s' WHERE login = '%s'", newFavItems, profile);
                                                	esql.executeUpdate(query4);
                                                	break;
                                            	case 3:
                                                	System.out.println("New Phone Number: ");
                                                	int newPhone = Integer.parseInt(in.readLine());
                                                	String query5 = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s'", newPhone, profile);
                                                	esql.executeUpdate(query5);
                                                	break;
                                            	case 4:
                                                	System.out.println("New type: ");
                                                	String EMC = in.readLine().toLowerCase();
                                                	boolean invalid = true;
                                                	while(invalid){
                                                    		if (EMC == "customer" || EMC == "manager" || EMC == "employee"){
                                                        		String query = String.format("UPDATE USERS SET type = '%s' WHERE login = '%s'", EMC, profile);
                                                        		esql.executeUpdate(query);
                                                        		invalid=false;
                                                    		}else{
                                                        		System.out.println("Invalid type");
                                                    		}
                                                	}
                                                	break;
                                            	case 9:
                                                	userUpdate = false;
                                                break;
                                            	default:
                                                	System.out.println("Unrecognized choice!");
                                                break;
                                        }
                                    }
                                }else{
                                    System.out.println("Invalid Profile");
                                }
                                break;
                        case 9:
                                update = false;
                                break;
                        default:
                                System.out.println("Unrecognized choice!");
                                break;
                        }
                    }
             }
	     else if(userType.equals("Customer") || userType.equals("Employee")){
                     boolean update = true;
                     while(update){
                        System.out.println("What would you like to modify?");
                        System.out.println("1. Password");
                        System.out.println("2. Favorite Items");
                        System.out.println("3. Phone Number");
                        System.out.println("9. Exit Modify Menu");
                        switch(readChoice()){
                            case 1:
                                System.out.println("New Password: ");
                                String newPassword = in.readLine();
                                String query = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s'", newPassword, authUser);
                                esql.executeUpdate(query);
                                break;
                            case 2:
                                System.out.println("New Favorite Items: ");
                                String newFavItems = in.readLine();
                                String query1 = String.format("UPDATE USERS SET favItems = '%s' WHERE login = '%s'", newFavItems, authUser);
                                esql.executeUpdate(query1);
                                break;
                            case 3:
                                System.out.println("New Phone Number: ");
                                String newPhone = in.readLine();
                                String query2 = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s'", newPhone, authUser);
                                esql.executeUpdate(query2);
                                break;
                            case 9:
                                update = false;
                                break;
                            default:
                                System.out.println("Unrecognized choice!");
                                break;
                        }
                    }
             }


        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
}

//only for customer
public static void PlaceOrder(Cafe esql, String authUser){
	try{
		double Ptotal = 0;
		boolean done = false;
		String currTime2 = "";

		String intQuery = String.format("SELECT MAX(orderid) as maxid FROM ORDERS");
		List<List<String>>  maxInt = esql.executeQueryAndReturnResult(intQuery);

		int maxInt2 = Integer.parseInt(maxInt.get(0).get(0)) +1;

		 String timeQuery = String.format("SELECT CURRENT_TIMESTAMP");
                                List<List<String>>  currTime = esql.executeQueryAndReturnResult(timeQuery);
                                currTime2 = currTime.get(0).get(0);

		String NewOrder = String.format("INSERT INTO ORDERS (orderid, login,  paid, timeStampRecieved, total) VALUES ('%d', '%s', false, '%s', 0)", maxInt2, authUser, currTime2);
                esql.executeUpdate(NewOrder);


		while(!done){
    			System.out.println("What would you like to order:");
    			String itemname = in.readLine().toLowerCase();

    			String priceQuery = String.format("SELECT itemName, price from MENU WHERE LOWER(itemName) = '%s' ", itemname);
			List<List<String>> inList = esql.executeQueryAndReturnResult(priceQuery);

			System.out.println(inList);
		//	String menuItem = inList.get(0).get(0);

   	 		if (inList.isEmpty()){
        			System.out.println("no such item on the menu");
	    		}else{
      			//	String timeQuery = String.format("SELECT CURRENT_TIMESTAMP");
        		//	List<List<String>>  currTime = esql.executeQueryAndReturnResult(timeQuery);
	        	//	currTime2 = currTime.get(0).get(0);

          			//String ItemAdd = String.format("INSERT INTO ITEMSTATUS (orderid, itemName, lastUpdated, status, comments) VALUES ('%d', '%s', '%s', false, '')", maxInt2, itemname, currTime2);
				//esql.executeUpdate(ItemAdd);
				String menuItem = inList.get(0).get(0);


        			List<List<String>>  priceint = esql.executeQueryAndReturnResult(priceQuery);
	        		double singlePrice = Double.parseDouble(priceint.get(0).get(1));
      				Ptotal = Ptotal + singlePrice ;
				System.out.println("Total:" + Ptotal);
	        		boolean invalid = true;
      				while(invalid){
            				System.out.println("Would you like to add to  your order?");
            				String finished = in.readLine();
           				 if (finished.toLowerCase().equals("no")){
                				done = true;
                				invalid = false;
            				}else if(finished.toLowerCase().equals("yes")){
                				invalid = false;
            				}else{
                				System.out.println("Invalid input");
            				}
        			}
        			String updateOrder1 = String.format("UPDATE ORDERS SET timeStampRecieved = '%s', total = '%f' WHERE orderid = '%d' ",currTime2, Ptotal, maxInt2);
				esql.executeUpdate(updateOrder1);
				System.out.println(itemname);
				String ItemAdd = String.format("INSERT INTO ITEMSTATUS (orderid, itemName, lastUpdated, status, comments) VALUES ('%d', '%s', '%s', false, '')", maxInt2, menuItem, currTime2);
        	     		esql.executeUpdate(ItemAdd);
				System.out.println("Order Added");
    			}
		}
		//String NewOrder = String.format("INSERT INTO ORDERS (orderid, login,  paid, timeStampRecieved, total) VALUES ('%d', '%s', false, '%s', %f)", maxInt2, authUser, currTime2, Ptotal);
                //esql.executeUpdate(NewOrder);

        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
}

//customer update order of nonpaid order by orderIDID
//manager and employee update order staus by orderID
public static void UpdateOrder(Cafe esql, String authUser, String userType){
	try{
		if(userType.equals("Customer")){
           		System.out.println("Which order would you like to update?");
            		int orderNum = Integer.parseInt(in.readLine());
			String query = String.format("SELECT orderid FROM ORDERS WHERE orderid = '%d' AND login = '%s' AND paid = false" , orderNum, authUser);
            		if (esql.executeQueryAndPrintResult(query) !=0 ){
				boolean updateOrder = true;
                		while (updateOrder){
                    			System.out.println("What would you like to modify?");
                    			System.out.println("1. Add to order");
                    			System.out.println("2. Delete an item in the order");
                    			System.out.println("9. Exit Update Order");
                    			switch(readChoice()){
                        			case 1:
                            				PlaceOrder(esql, authUser);
                            			break;
                        			case 2:
                            				System.out.println("Which item would you like to delete?");
 		 	                        	String itemDelete = in.readLine().toLowerCase();
							String orderQuery = String.format("SELECT itemName FROM ITEMSTATUS WHERE orderid = '%d' AND LOWER(itemName) = '%s'" , orderNum, itemDelete);
                            				if (esql.executeQueryAndPrintResult(orderQuery) !=0 ){
								String deleteQuery = String.format("DELETE FROM ITEMSTATUS WHERE LOWER(itemName) = '%s' AND orderid = '%d'", itemDelete, orderNum );
                                				esql.executeUpdate(deleteQuery);
								//gets the price of item we are deleting
								String subtractPrice = String.format("Select price FROM MENU WHERE LOWER(itemName) = '%s'", itemDelete);
                                				List<List<String>> priceTable = esql.executeQueryAndReturnResult(subtractPrice);
                                				double itemPrice = Double.parseDouble(priceTable.get(0).get(0));
								
								//get the price of original total
								String originalTotal = String.format("Select total FROM ORDERS WHERE orderid = '%d'", orderNum);
                                                                List<List<String>> priceTable1 = esql.executeQueryAndReturnResult(originalTotal);
                                                                double ogTotal = Double.parseDouble(priceTable1.get(0).get(0));
                                                                                                                                                                                                
								double newPrice = ogTotal - itemPrice;								

								String updateOrderQuery = String.format("UPDATE ORDERS SET total = '%f' WHERE orderid = '%d'", newPrice, orderNum);
                                				esql.executeUpdate(updateOrderQuery);
								//String updateOrderQuery1 = String.format("UPDATE ORDERS SET total =0 WHERE orderid = '%d' AND total <0" , orderNum);
                                                                //esql.executeUpdate(updateOrderQuery1);	

								String timeQuery = String.format("SELECT CURRENT_TIMESTAMP");
                                				List<List<String>>  currTime = esql.executeQueryAndReturnResult(timeQuery);
                                				String currTime2 = currTime.get(0).get(0);
								String updateStatusQuery = String.format("UPDATE ITEMSTATUS SET lastUpdated = '%s'  WHERE orderid = '%d'", currTime2, orderNum);
                                				esql.executeUpdate(updateOrderQuery);
                            				}else{
                                				System.out.println("Item does not exist in your order");
                            				}
                            			break;
                        			case 9:
                            				updateOrder = false;
                            			break;
					}
                    		}
                	}else{
                    		System.out.println("Order ID does not exist or is not yours");
                	}
            	}else if(userType.equals("Employee") || userType.equals("Manager ")){
                	System.out.println("Item ID:");
                	String itemID = in.readLine();
                	String query = String.format("UPDATE Orders SET paid = true WHERE orderid = '%s'", itemID);
                	esql.executeUpdate(query);
                	System.out.println("Order changed to paid by employee");
            	}else {
             		System.out.println("Error: invalid choice!");
            	}
        
                
        }catch(Exception e){
                System.err.println(e.getMessage());
                return;
        }
}

}
//end Cafe

