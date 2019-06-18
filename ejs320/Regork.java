import java.io.Console;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Regork { //needs constructors?
    static Map<String, PreparedStatement> queries = new HashMap<>();
    static String password;
    static String username;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BOLD = "\033[1m";
    public static final String ANSI_BRESET = "\033[0m";

    public static void main(String[] args) { //make get integer method that works for all, sends msg prompt
        try {

        Console console = System.console();
        Scanner scan = new Scanner(System.in);
        boolean isLoggedIn = false;
        System.out.println("Welcome to Regork!");
        DriverManager.setLoginTimeout(20);
        String defUser = "sys as sysdba";
        String defPassword = "Oradoc_db1";
        readLogin(scan, console, "Please log in to access the Regork information system", false);
        int failedAttempts = 0;
        while (!isLoggedIn) {
            try (
//                    Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
                    Connection con = DriverManager.getConnection("jdbc:oracle:thin:@klondike.ch6uqvsohjtl.us-east-2.rds.amazonaws.com :1521:ORCL", username, password);
                    Statement stmt = con.createStatement()
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
                    System.out.println("[3] Ordering");
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
                                int status = DataIntegrity.check(scan, con);
                                if (status == -1) {
                                    isValid = false;
                                }
                            } else {
                                isValid = true;
                                int status = Ordering.order(scan);
                                if(status == -1) {
                                    isValid = false;
                                }
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
                failedAttempts++;
                if(failedAttempts > 3) {
                    System.out.println(Regork.ANSI_RED + "\nToo Many Failed Login Attempts" + Regork.ANSI_RESET);
                    System.out.println(Regork.ANSI_GREEN + "Program Exiting \n" + Regork.ANSI_RESET);
                    System.exit(0);
                }
                if (sqe.getErrorCode() == 28000) {//account locked
                    readLogin(scan, console, "Error: Account Locked", true);
                } else if (sqe.getErrorCode() == 1017) {
                    readLogin(scan, console, "Error: Invalid Username or Password", true);
                } else if (sqe.getMessage().contains("No suitable driver found")) {
                    readLogin(scan, console, "Error: Could Not Connect, Driver Not Found", true);
                } else if (sqe.getErrorCode() == 17002) {
                    readLogin(scan, console, "Error: Could Not Establish Connection", true);
                } else {
                    readLogin(scan, console, "Unknown Login Error", true);
                }
                isLoggedIn = false;
            } catch (Exception e) {
                if (e.getMessage().contains("String index out of range")) {
                    System.out.println(ANSI_RED + "Error: Username/Password not read properly." + ANSI_RESET);
                    System.out.println(Regork.ANSI_GREEN + "Program Exiting \n" + Regork.ANSI_RESET);
                    System.exit(0);
                } else {
                    exitUnknown();
                }
                isLoggedIn = false;
            }
        }

        } catch (Throwable t) {
            System.out.println(Regork.ANSI_GREEN + "Program Exiting \n" + Regork.ANSI_RESET);
            System.exit(0);
        }
    }

    static void exitUnknown() {
        System.out.println(Regork.ANSI_RED + "An Unknown Error Occurred" + Regork.ANSI_RESET);
        System.out.println(Regork.ANSI_GREEN + "Program Exiting \n" + Regork.ANSI_RESET);
        System.exit(0);
    }

    static void checkQuit(Scanner scan) {
        String input = scan.next();
        if(input.toUpperCase().equals("X")) {
            System.out.println(Regork.ANSI_GREEN + "Program Exiting \n" + Regork.ANSI_RESET);
            System.exit(0);
        }
    }

    static void readLogin(Scanner scan, Console console, String prompt, boolean isError) {
        if(isError) {
            System.out.println(ANSI_RED + "\n" + prompt + "\n" + ANSI_RESET);
        } else {
            System.out.println(ANSI_GREEN + "\n" + prompt + "\n" + ANSI_RESET);
        }
        username = null;
        System.out.print("Username: ");
        username = scan.next();
        password = null;
        password = new String(console.readPassword("Password: "));
        System.out.println("Connecting...");
    }


    public static void insertQueries(Connection con) {
        try {
            PreparedStatement inventoryByID = con.prepareStatement("select SUPPLIER_ID as ID, SUPPLIER_NAME as Supplier, count(TO_ID) as Stock from ejscribner.PRODUCT inner join ejscribner.SHIPMENT on PRODUCT.SHIP_ID = SHIPMENT.SHIP_ID inner join SUPPLIER on SHIPMENT.TO_ID = SUPPLIER.SUPPLIER_ID where PARENT_ID = ? group by SUPPLIER_NAME, SUPPLIER_ID");
            queries.put("inventoryByID", inventoryByID);
            PreparedStatement productSearchByName = con.prepareStatement("select unique gen_id as ID, product_name as Name from ejscribner.product inner join ejscribner.GEN_PRODUCT on parent_id = gen_id where lower(PRODUCT_NAME) like '%'||?||'%'");
            queries.put("productSearchByName", productSearchByName);
            PreparedStatement productSearchByID = con.prepareStatement("select unique gen_id as ID, product_name as Name from ejscribner.product inner join ejscribner.GEN_PRODUCT on parent_id = gen_id where gen_id like '%'||?||'%'");
            queries.put("productSearchByID", productSearchByID);
            PreparedStatement checkShipFromIndiv = con.prepareStatement("select FROM_ID as Ship_From,TO_ID as Ship_To, M.MANUFACTURER_ID as Manufacturer from ejscribner.SHIPMENT inner join ejscribner.PRODUCT on SHIPMENT.SHIP_ID = PRODUCT.SHIP_ID inner join ejscribner.MANUFACTURE M on PRODUCT.PRODUCT_ID = M.PRODUCT_ID where M.PRODUCT_ID = ?");
            queries.put("checkShipFromIndiv", checkShipFromIndiv);
            CallableStatement callAddOrder = con.prepareCall("{call ejscribner.addOrder(?, ?)}");
            queries.put("callAddOrder", callAddOrder);
            CallableStatement callFixShip = con.prepareCall("{call ejscribner.fix_ship(?)}");
            queries.put("callFixShip", callFixShip);
            CallableStatement callFixOffer = con.prepareCall("{call ejscribner.fix_offer(?)}");
            queries.put("callFixOffer", callFixOffer);
            PreparedStatement manufacturedBy = con.prepareStatement("select unique MANUFACTURER_ID from ejscirbner.MANUFACTURE inner join ejscribner.product on PRODUCT.PRODUCT_ID = MANUFACTURE.PRODUCT_ID inner join ejscribner.GEN_PRODUCT on GEN_ID = PARENT_ID where GEN_ID = ? minus (select SUPPLIER_ID from ejscribner.offers where GEN_ID = ?)");
            queries.put("manufacturedBy", manufacturedBy);
            PreparedStatement offeredBy = con.prepareStatement("select SUPPLIER_ID from ejscribner.offers where GEN_ID = ?");
            queries.put("offeredBy", offeredBy);
            PreparedStatement genSearchID = con.prepareStatement("select GEN_ID, PRODUCT_NAME, CURRENT_PRICE from ejscribner.GEN_PRODUCT where GEN_ID like '%'||?||'%'");
            queries.put("genSearchID", genSearchID);
            PreparedStatement genSearchName = con.prepareStatement("select GEN_ID, PRODUCT_NAME, CURRENT_PRICE from ejscribner.GEN_PRODUCT where lower(PRODUCT_NAME) like '%'||?||'%'");
            queries.put("genSearchName", genSearchName);
            PreparedStatement regorkInventoryByName = con.prepareStatement("select GEN_ID as ID, PRODUCT_NAME as Product, count(TO_ID) as Stock from ejscribner.PRODUCT inner join ejscribner.SHIPMENT on PRODUCT.SHIP_ID = SHIPMENT.SHIP_ID inner join ejscribner.SUPPLIER on SHIPMENT.TO_ID = SUPPLIER.SUPPLIER_ID inner join ejscribner.GEN_PRODUCT on PRODUCT.PARENT_ID = GEN_PRODUCT.GEN_ID where SUPPLIER_ID = 11 and lower(PRODUCT_NAME) like '%'||?||'%' group by GEN_ID, PRODUCT_NAME order by PRODUCT_NAME");
            queries.put("regorkInventoryByName", regorkInventoryByName);
            PreparedStatement regorkInventoryByID = con.prepareStatement("select GEN_ID as ID, PRODUCT_NAME as Product, count(TO_ID) as Stock from ejscribner.PRODUCT inner join SHIPMENT on PRODUCT.SHIP_ID = SHIPMENT.SHIP_ID inner join SUPPLIER on SHIPMENT.TO_ID = SUPPLIER.SUPPLIER_ID inner join GEN_PRODUCT on PRODUCT.PARENT_ID = GEN_PRODUCT.GEN_ID where SUPPLIER_ID = 11 and GEN_ID like '%'||?||'%' group by GEN_ID, PRODUCT_NAME order by PRODUCT_NAME");
            queries.put("regorkInventoryByID", regorkInventoryByID);
            PreparedStatement manufacturersByGenID = con.prepareStatement("select unique MANUFACTURER_ID as ID, SUPPLIER_NAME as Manufacturer, PRODUCT_NAME from ejscribner.MANUFACTURE inner join ejscirbner.SUPPLIER on SUPPLIER_ID = MANUFACTURER_ID inner join ejscribner.PRODUCT on PRODUCT.PRODUCT_ID = MANUFACTURE.PRODUCT_ID inner join GEN_PRODUCT on PRODUCT.PARENT_ID = GEN_PRODUCT.GEN_ID where GEN_ID = ?");
            queries.put("manufacturersByGenID", manufacturersByGenID);
            PreparedStatement regorkInventoryResults = con.prepareStatement("select GEN_ID as ID, PRODUCT_NAME as Product, count(TO_ID) as Stock from ejscribner.PRODUCT inner join ejscribner.SHIPMENT on PRODUCT.SHIP_ID = SHIPMENT.SHIP_ID inner join ejscribner.SUPPLIER on SHIPMENT.TO_ID = SUPPLIER.SUPPLIER_ID inner join ejscribner.GEN_PRODUCT on PRODUCT.PARENT_ID = GEN_PRODUCT.GEN_ID where SUPPLIER_ID = 11 and GEN_ID = ? group by GEN_ID, PRODUCT_NAME order by PRODUCT_NAME");
            queries.put("regorkInventoryResults", regorkInventoryResults);
        } catch (SQLException sqe) {
            Regork.exitUnknown();
        }
    }
}