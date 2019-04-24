
import java.io.Console;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Regork { //needs constructors?
    static Map<String, PreparedStatement> queries = new HashMap<>();
    static String password;
    static String username;
    public static void main(String[] args) { //make get integer method that works for all, sends msg prompt
        Console console = System.console();
        Scanner scan = new Scanner(System.in);
        boolean isLoggedIn = false;
        System.out.println("Welcome to Regork!");
        DriverManager.setLoginTimeout(20);
        readLogin(scan, console, "Please log in to access the Regork information system");
        while (!isLoggedIn) {
            try (
                    Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                    Statement stmt = con.createStatement();
            ) {
                System.out.println("Successfully Connected");
                insertQueries(con);
                isLoggedIn = true;
                System.out.println("What type of user are you?");
                System.out.println("Type 1 for Manager, 2 for Supplier, or 3 for Recalls");
                boolean isValid = false;
                while (!isValid) {
                    if (scan.hasNextInt()) {
                        int userType = scan.nextInt(); //could make a letter?
                        if(userType >= 1 && userType <= 3) {
                            if (userType == 1) {
                                isValid = true;
                                Manager.manage(con, scan);
                            } else if(userType == 2) {
                                isValid = true;
                                System.out.println("Supplier");
                            } else {
                                isValid = true;
                                System.out.println("Third opt");
                            }
                        } else {
                            System.out.println("Error: Please Enter 1, 2, or 3");
                        }
                    } else {
                        System.out.println("Error: Please Enter 1, 2, or 3");
                        scan.next();
                    }
                }
            } catch (SQLException sqe) {
                if (sqe.getErrorCode() == 2800) {//account locked
                    readLogin(scan, console, "Error: Account Locked");
                }
                if (sqe.getErrorCode() == 1017) {
                    readLogin(scan, console, "Error: Invalid Username or Password");
                }
                if (sqe.getMessage().contains("No suitable driver found")) {
                    readLogin(scan, console, "Error: Could Not Connect, Driver Not Found");
                }
                if (sqe.getErrorCode() == 17002) {
                    readLogin(scan, console, "Error: Could Not Establish Connection");
                }
                isLoggedIn = false;
            } catch (Exception e) {
                if (e.getMessage().contains("String index out of range")) {
                    System.out.println("Error: Username/Password not read properly.");
                    System.out.println("Exiting Program...");
                } else {
                    System.out.println("An Unknown Error Occurred");
                    System.out.println("Exiting Program...");
                    e.printStackTrace();
                }
                isLoggedIn = false;
            }
        }

    }

    static void readLogin(Scanner scan, Console console, String prompt) {
        System.out.println(prompt);
        username = null;
        System.out.print("Username: ");
        username = scan.next();
        password = null;
        password = new String(console.readPassword("Password: "));
        System.out.println("Connecting...");
    }


    public static void insertQueries(Connection con) {
        try {
            PreparedStatement inventoryByID = con.prepareStatement("select SUPPLIER_NAME as Supplier, count(*) as Stock from product inner join GEN_PRODUCT on PARENT_ID = GEN_ID inner join MANUFACTURE on PRODUCT.PRODUCT_ID = MANUFACTURE.PRODUCT_ID inner join SUPPLIER on SUPPLIER.SUPPLIER_ID = MANUFACTURE.MANUFACTURER_ID where gen_id = ? group by SUPPLIER_NAME");
            queries.put("inventoryByID", inventoryByID);
            PreparedStatement productSearchByName = con.prepareStatement("select unique gen_id as ID, product_name as Name from product inner join GEN_PRODUCT on parent_id = gen_id where lower(PRODUCT_NAME) like '%'||?||'%'");
            queries.put("productSearchByName", productSearchByName);
            PreparedStatement productSearchByID = con.prepareStatement("select unique gen_id as ID, product_name as Name from product inner join GEN_PRODUCT on parent_id = gen_id where gen_id like '%'||?||'%'");
            queries.put("productSearchByID", productSearchByID);
        } catch (SQLException sqe) {
            System.out.println("We ran into a sql exception on insert");
        }
    }
}