import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;

public class Ordering {
    static int order(Scanner scan) {
        int option = menu(scan, 1);
        if (option == 0) {
            return -1;
        }
        if (option == 1) {
            option = Manager.checkRegorkInventory(scan);
        }
        if (option == 2) {
            option = Manager.findManufacturers(scan);
        }
        if (option == 3) {
            option = orderStock(scan);
        }
        order(scan);
        return -1;
    }

    static int orderStock(Scanner scan) {
        boolean isValidGen = false;
        boolean isValidMan = false;
        ResultSet before;
        ResultSet after;
        int genericID = -1;
        int manufacturerID = -1;
        while(!isValidGen) {
            genericID = DataIntegrity.safeInt(scan, "Please enter the " + Regork.ANSI_BOLD + "generic product id " + Regork.ANSI_BRESET + "you would like to order: ");
            if (genericID > 0 && genericID < 41) {
                isValidGen = true;
            } else {
                System.out.println(Regork.ANSI_RED + "\nNo matching generic product for id: " + Regork.ANSI_BOLD + manufacturerID + Regork.ANSI_BRESET + Regork.ANSI_RESET + "\n");
            }
        }
        while(!isValidMan) {
            manufacturerID = DataIntegrity.safeInt(scan, "Please enter the " + Regork.ANSI_BOLD + "manufacturer " + Regork.ANSI_BRESET + "you would like to order from: ");
            if (manufacturerID > 0 && manufacturerID < 11) {
                isValidMan = true;
            } else {
                System.out.println(Regork.ANSI_RED + "\nNo matching manufacturer for id: " + Regork.ANSI_BOLD + manufacturerID + Regork.ANSI_BRESET + Regork.ANSI_RESET + "\n");
            }
        }

        try {

            Regork.queries.get("regorkInventoryResults").setInt(1, genericID);
            before = Regork.queries.get("regorkInventoryResults").executeQuery();

            ResultSetMetaData beforeMeta = before.getMetaData();
            CommandLineTable beforeTable = new CommandLineTable();

            while (before.next()) {
                beforeTable.addRow(before.getString("ID"), before.getString("Product"), before.getString("Stock"));
            }


            Regork.queries.get("callAddOrder").setInt(1, genericID);
            Regork.queries.get("callAddOrder").setInt(2, manufacturerID);
            Regork.queries.get("callAddOrder").execute();

            Regork.queries.get("regorkInventoryResults").setInt(1, genericID);
            after = Regork.queries.get("regorkInventoryResults").executeQuery();

            CommandLineTable afterTable = new CommandLineTable();
            ResultSetMetaData afterMeta = after.getMetaData();
            beforeTable.setHeaders(beforeMeta.getColumnLabel(1), beforeMeta.getColumnLabel(2), beforeMeta.getColumnLabel(3));
            afterTable.setHeaders(afterMeta.getColumnLabel(1), afterMeta.getColumnLabel(2), afterMeta.getColumnLabel(3));

            while (after.next()){
                afterTable.addRow(after.getString("ID"), after.getString("Product"), after.getString("Stock"));
            }

            beforeTable.print();
            System.out.print(Regork.ANSI_GREEN);
            System.out.println("        |           |");
            System.out.println("        |           |");
            System.out.println("        |   Order   |");
            System.out.println("        | Confirmed |");
            System.out.println("        |           |");
            System.out.println("        |           |");
            System.out.println("        **         **");
            System.out.println("          **     **");
            System.out.println("            ** **");
            System.out.println("              *");
            System.out.print(Regork.ANSI_RESET);
            afterTable.print();

        } catch (SQLException sqe) {
            Regork.exitUnknown();
        }
        return 1;
    }

    static int menu(Scanner scan, int context) {
        int selection = -1;
        boolean isValid = false;
        System.out.println(); //padding
        while (!isValid) {
            if(context == 1) {
                System.out.println("Main Menu > " + Regork.ANSI_BOLD + "Ordering" + Regork.ANSI_BRESET);
                System.out.println("Please select an option from the menu:");
                System.out.println("--------------------------------------");
                System.out.println("[1] Check Regork Inventory");
                System.out.println("[2] Find Manufacturers");
                System.out.println("[3] Place an Order");
                System.out.println("[0] Go Back");
                System.out.println("[X] Quit Program");


            } else if (context == 2) {
                System.out.println("Main Menu > " + "Ordering > " + Regork.ANSI_BOLD + "Shipping Anomaly Found" + Regork.ANSI_BRESET);
            } else if (context == 3) {
                System.out.println("Main Menu > " + "Ordering > " + Regork.ANSI_BOLD + "Offers Anomaly Found" + Regork.ANSI_BRESET);
            }
            if(context == 2 || context == 3) {
                System.out.println("Would you like to:");
                System.out.println("------------------");
                System.out.println("[1] Order");
                System.out.println("[2] Leave this anomaly");
                System.out.println("[0] Go Back");
                System.out.println("[X] Quit Program");
            }

            if (scan.hasNextInt()) {
                selection = scan.nextInt();
                if (selection >= 0 && selection <= 3) {
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
}
