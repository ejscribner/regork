
import java.io.Console;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Regork { //needs constructors?
    static Map<String, PreparedStatement> queries = new HashMap<>();
    static String password;
    static String username;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\033[1m";
    public static final String ANSI_BRESET = "\033[0m";
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
                boolean isValid = false;
                System.out.println(); //padding
                while (!isValid) {
                    System.out.println(ANSI_BOLD + "\nMain Menu" + ANSI_BRESET);
                    System.out.println("Please select your user type: ");
                    System.out.println("-----------------------------");
                    System.out.println("[1] Manager");
                    System.out.println("[2] Data Checker");
                    System.out.println("[3] Recalls");
                    System.out.println("[X] Quit Program");
                    if (scan.hasNextInt()) { //look into abstraction
                        int userType = scan.nextInt();
                        if(userType >= 1 && userType <= 3) {
                            if (userType == 1) {
                                isValid = true;
                                int status = Manager.manage(scan);
                                if(status == -1) {
                                    isValid = false;
                                }
                            } else if(userType == 2) {
                                isValid = true;
                                int status = DataIntegrity.check(scan);
                                if (status == -1) {
                                    isValid = false;
                                    //doesnt get here after going into menu options and walking back
                                }
                            } else {
                                isValid = true;
                                System.out.println("Third opt");
                            }
                        } else {
                            System.out.println(ANSI_RED + "\nInvalid input" + ANSI_RESET);
                            isValid = false;
                        }
                    } else {
                        checkQuit(scan);
                        System.out.println(ANSI_RED + "\nInvalid input" + ANSI_RESET);
                        isValid = false;
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

    static void checkQuit(Scanner scan) {
        String input = scan.next();
        if(input.toUpperCase().equals("X")) {
            System.out.println(Regork.ANSI_GREEN + "Program Exiting \n" + Regork.ANSI_RESET);
            System.exit(0);
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
            PreparedStatement checkShipFromIndiv = con.prepareStatement("select FROM_ID as Ship_From,TO_ID as Ship_To, M.MANUFACTURER_ID as Manufacturer from SHIPMENT inner join PRODUCT on SHIPMENT.SHIP_ID = PRODUCT.SHIP_ID inner join MANUFACTURE M on PRODUCT.PRODUCT_ID = M.PRODUCT_ID where M.PRODUCT_ID = ?");
            queries.put("checkShipFromIndiv", checkShipFromIndiv);
            CallableStatement callFixShip = con.prepareCall("{call ejs320.fix_ship(?)}");
            queries.put("callFixShip", callFixShip);
            CallableStatement callFixOffer = con.prepareCall("{call ejs320.fix_offer(?)}");
            queries.put("callFixOffer", callFixOffer);
            PreparedStatement manufacturedBy = con.prepareStatement("select unique MANUFACTURER_ID from MANUFACTURE inner join product on PRODUCT.PRODUCT_ID = MANUFACTURE.PRODUCT_ID inner join GEN_PRODUCT on GEN_ID = PARENT_ID where GEN_ID = ? minus (select SUPPLIER_ID from offers where GEN_ID = ?)");
            queries.put("manufacturedBy", manufacturedBy);
            PreparedStatement offeredBy = con.prepareStatement("select SUPPLIER_ID from offers where GEN_ID = ?");
            queries.put("offeredBy", offeredBy);
            PreparedStatement genSearchID = con.prepareStatement("select GEN_ID, PRODUCT_NAME, CURRENT_PRICE from GEN_PRODUCT where GEN_ID like '%'||?||'%'");
            queries.put("genSearchID", genSearchID);
            PreparedStatement genSearchName = con.prepareStatement("select GEN_ID, PRODUCT_NAME, CURRENT_PRICE from GEN_PRODUCT where lower(PRODUCT_NAME) like '%'||?||'%'");
            queries.put("genSearchName", genSearchName);
        } catch (SQLException sqe) {
            System.out.println("We ran into a sql exception on insert");
        }
    }
}