import java.sql.*;
import java.util.Scanner;

public class Manager extends Regork {
    public static int manage(Scanner scan) {
        int option = menu(scan);
        if (option == 0) {
            return -1;
        }
        if (option == 1) {
            option = searchController(scan);
        }
        if (option == 2) {
            option = checkRegorkInventory(scan);
        }
        if (option == 3) {
            option = findManufacturers(scan);
        }
        manage(scan);
        return -1;
    }

    public static int searchController(Scanner scan) {
        Boolean isValid = false;
        getSearchInput(scan);
        while(!isValid) {
            System.out.println("Main Menu > " + "Manager > " + Regork.ANSI_BOLD + "Results" + Regork.ANSI_BRESET );
            System.out.println("Would you like to:");
            System.out.println("--------------------------------------");
            System.out.println("[1] Look up stock for one of the above products");
            System.out.println("[2] Search again");
            System.out.println("[0] Go Back");
            System.out.println("[X] Quit Program");
            if(scan.hasNextInt()) {
                int option = scan.nextInt();
                if(option >= 0 && option < 3) {
                    if(option == 1) {
                        int item = DataIntegrity.safeInt(scan, "Please type a generic product id: ");
                        searchExternalInventory(item);
                    }
                    if(option == 2) {
                        searchController(scan);
                    }
                    isValid = true;
                } else {
                    System.out.println(Regork.ANSI_RED + "\nInvalid input" + Regork.ANSI_RESET);
                    isValid = false;
                }
            } else {
                Regork.checkQuit(scan);
                System.out.println(Regork.ANSI_RED + "\nInvalid input" + Regork.ANSI_RESET);
                isValid = false;
            }
        }
        return 1;
    }

    public static int findManufacturers(Scanner scan) {
        ResultSet result;
        try {
            int input = DataIntegrity.safeInt(scan, "Enter a generic ID to view manufacturers: ");
            Regork.queries.get("manufacturersByGenID").setInt(1, input);
            result = Regork.queries.get("manufacturersByGenID").executeQuery();
            if(!result.next()) {
                System.out.println(ANSI_RED + "\nNo Generic Product Found\n" + ANSI_RESET);
                return 0;
            } else {
                String product;
                ResultSetMetaData setMetaData = result.getMetaData();
                CommandLineTable table = new CommandLineTable();
                table.setHeaders(setMetaData.getColumnLabel(1), setMetaData.getColumnLabel(2));
                do {
                    table.addRow(result.getString("ID"), result.getString("Manufacturer"));
                    product = result.getString("PRODUCT_NAME");
                } while (result.next());
                System.out.println(ANSI_GREEN + "\nShowing Manufacturers for: " + ANSI_BOLD + product + ANSI_BRESET + ANSI_RESET);
                table.print();
            }
        } catch (SQLException sqe) {
            Regork.exitUnknown();
            sqe.printStackTrace();

        }
        return 1;
    }



    public static int checkRegorkInventory(Scanner scan) {
        ResultSet result;
        try {
            System.out.println("Enter number or letter: ");
            if(scan.hasNextInt()) {
                int input = scan.nextInt();
                Regork.queries.get("regorkInventoryByID").setInt(1, input);
                result = Regork.queries.get("regorkInventoryByID").executeQuery();
            } else {
                String input = scan.next();
                Regork.queries.get("regorkInventoryByName").setString(1, input.toLowerCase());
                result = Regork.queries.get("regorkInventoryByName").executeQuery();
            }
            if(!result.next()) {
                System.out.println(ANSI_RED + "\nNo Inventory Found\n" + ANSI_RESET);
                return 0;
            } else {
                ResultSetMetaData setMetaData = result.getMetaData();
                CommandLineTable table = new CommandLineTable();
                table.setHeaders(setMetaData.getColumnLabel(1), setMetaData.getColumnLabel(2), setMetaData.getColumnLabel(3));
                do {
                    table.addRow(result.getString("ID"), result.getString("Product"), result.getString("Stock"));
                } while (result.next());
                table.print();
            }
        } catch (SQLException sqe) {
            Regork.exitUnknown();
            sqe.printStackTrace();
        }
        return 1;
    }


    public static int menu(Scanner scan) {
        int selection = -1;
        boolean isValid = false;
        System.out.println(); //padding
        while (!isValid) {

                System.out.println("Main Menu > " + Regork.ANSI_BOLD + "Manager" + Regork.ANSI_BRESET);
                System.out.println("Please select an option from the menu:");
                System.out.println("--------------------------------------");
                System.out.println("[1] Search products and look up stock by supplier");
                System.out.println("[2] Check Regork inventory");
                System.out.println("[3] Search for manufacturers");
                System.out.println("[0] Go Back");
                System.out.println("[X] Quit Program");

            if (scan.hasNextInt()) {
                selection = scan.nextInt();
                if (selection >= 0 && selection < 4) {
                    isValid = true;
                } else {
                    System.out.println(Regork.ANSI_RED + "\nInvalid input" + Regork.ANSI_RESET);
                    isValid = false;
                }
            } else {
                Regork.checkQuit(scan);
                System.out.println(Regork.ANSI_RED + "\nInvalid input" + Regork.ANSI_RESET);
                isValid = false;
            }
        }
        return selection;
    }


    public static void getSearchInput(Scanner scan) {
        System.out.println("\nProduct lookup checks for name or product ID containing search key");
        System.out.print("Please enter a string or number to search products: ");
        String searchKey = scan.next();
        searchExternalProducts(searchKey);
    }

    public static void searchExternalProducts(String searchKey) { //searches all products in existence

        ResultSet result;
        int searchID = -1;
        Boolean isString = false;
        try {
            searchID = Integer.parseInt(searchKey);
        } catch (NumberFormatException nfe) {
            isString = true;
        }
        try{
            if(isString) {
                Regork.queries.get("productSearchByName").setString(1, searchKey.toLowerCase());
                result = Regork.queries.get("productSearchByName").executeQuery();
            } else {
                Regork.queries.get("productSearchByID").setInt(1, searchID);
                result = Regork.queries.get("productSearchByID").executeQuery();
            }

            if (!result.next()) {
                System.out.println("Result empty");
            } else {
                ResultSetMetaData setMetaData = result.getMetaData();
                CommandLineTable table = new CommandLineTable();
                table.setHeaders(setMetaData.getColumnLabel(1), setMetaData.getColumnLabel(2));

                do {
                    table.addRow(result.getString("ID"), result.getString("Name"));
                } while (result.next());
                System.out.println(); //padding
                table.print();
                System.out.println(); //padding
            }
        } catch (SQLException sqE) {
            Regork.exitUnknown();
            sqE.printStackTrace();
        }
    }

    public static void searchGenerics(String searchKey) {
        ResultSet result;
        Boolean isString = false;
        int searchID = -1;
        try {
            searchID = Integer.parseInt(searchKey);
        } catch (NumberFormatException nfe) {
            isString = true;
        }
        try{
            if(isString) {
                Regork.queries.get("genSearchName").setString(1, searchKey.toLowerCase());
                result = Regork.queries.get("genSearchName").executeQuery();
            } else {
                Regork.queries.get("genSearchID").setInt(1, searchID);
                result = Regork.queries.get("genSearchID").executeQuery();
            }

            if (!result.next()) {
                System.out.println("Result empty");
            } else {
                ResultSetMetaData setMetaData = result.getMetaData();
                CommandLineTable table = new CommandLineTable();
                table.setHeaders(setMetaData.getColumnLabel(1), setMetaData.getColumnLabel(2), setMetaData.getColumnLabel(3));

                do {
                    table.addRow(result.getString("Gen_ID"), result.getString("Product_name"), result.getString("Current_price"));
                } while (result.next());
                table.print();

            }
        } catch (SQLException sqE) {
            Regork.exitUnknown();
            sqE.printStackTrace();

        }
    }

    public static void searchExternalInventory(int productID) { //this inventory works on who manufacturedd something
        //shoudl work on who has recieved shipments of someething? harder though
        //Maybe only for regork inventory
        ResultSet result;
        try{
            Regork.queries.get("inventoryByID").setInt(1, productID);
            result = Regork.queries.get("inventoryByID").executeQuery();
            if (!result.next()) {
                System.out.println("Result empty");
            } else {
                ResultSetMetaData setMetaData = result.getMetaData();
                CommandLineTable table = new CommandLineTable();
                table.setHeaders(setMetaData.getColumnLabel(1), setMetaData.getColumnLabel(2), setMetaData.getColumnLabel(3));
                do {
                    table.addRow(result.getString("ID"), result.getString("Supplier"), result.getString("Stock"));
                } while (result.next());
                table.print();

            }
        } catch (SQLException sqE) {
            Regork.exitUnknown();
            sqE.printStackTrace();

        }
    }
}
