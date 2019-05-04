import java.sql.*;
import java.util.Scanner;

public class DataIntegrity {
    public static int check(Scanner scan) {
        //from id != to id
        int option = menu(scan, 1);
        if (option == 0) {
            return -1;
        }
        if (option == 1) {
            option = findShipInconsistency(scan);
        }
        if (option == 2) {
            findOffersInconsistency(scan);
        }
        check(scan);
        return -1;
    }

    static int safeInt(Scanner scan, String msg)  {
        int num;
        boolean isValid = false;
        while(!isValid) {
            System.out.print(msg);
            if(scan.hasNextInt()) { //should check bounds?
                num = scan.nextInt();
                return num;
            } else {
                System.out.println(Regork.ANSI_RED + "\nInvalid input" + Regork.ANSI_RESET);
                isValid = false;
                scan.next();
            }
        }
        return -1;
    }

    public static int findOffersInconsistency(Scanner scan) {
        ResultSet resultManufacturedBy;
        ResultSet resultOfferedBy;
        System.out.print("To search for a product enter a string or number: ");
        String searchKey = scan.next();
        Manager.searchGenerics(searchKey);
        int genID = safeInt(scan, "Please enter a generic product ID: ");
        try {
            Regork.queries.get("manufacturedBy").setInt(1, genID);
            Regork.queries.get("manufacturedBy").setInt(2, genID);
            Regork.queries.get("offeredBy").setInt(1, genID);
            resultManufacturedBy = Regork.queries.get("manufacturedBy").executeQuery();
            int option = -1;
            if(!resultManufacturedBy.next()) {
                if(genID > 40 || genID < 1) {
                    System.out.println(Regork.ANSI_RED + "\nNo generic products found" + Regork.ANSI_RESET);
                } else {
                    System.out.println(Regork.ANSI_GREEN + "\nNo anomaly found" + Regork.ANSI_RESET);
                    return 0;
                }
                findOffersInconsistency(scan); //better in while loop?
            } else {
                CommandLineTable manTable = new CommandLineTable();
                CommandLineTable offerTable = new CommandLineTable();
                manTable.setHeaders("Manufacturers");
                offerTable.setHeaders("Offered By");
                resultOfferedBy = Regork.queries.get("offeredBy").executeQuery();
                resultOfferedBy.next();
                int countOffer = 0;
                int countMan = 0;
                do {
                    countMan++;
                    manTable.addRow(resultManufacturedBy.getString("MANUFACTURER_ID"));
                } while (resultManufacturedBy.next());
                while (resultOfferedBy.next()){
                    countOffer++;
                    offerTable.addRow(resultOfferedBy.getString("SUPPLIER_ID"));
                }
                if(countOffer == 0) {
                    offerTable.addRow("None");
                }
                if(countMan == 0) {
                    offerTable.addRow("None");
                }
                manTable.print();
                offerTable.print();
                System.out.println(Regork.ANSI_RED + "Anomaly Found" + Regork.ANSI_RESET);
                option = menu(scan, 3);
                if(option == 1) {
                    Regork.queries.get("callFixOffer").setInt(1, genID);
                    Regork.queries.get("callFixOffer").execute();
                    System.out.println(Regork.ANSI_GREEN + "Anomaly Fixed... Returning to Data Analyst Menu" + Regork.ANSI_RESET);
                } else if(option == 2 || option == 0) {
                    return 0;
                }
                return option;
            }
        } catch (SQLException sqe) {
            Regork.exitUnknown();
        }
        return -1;
    }

    public static int findShipInconsistency(Scanner scan) {
        ResultSet result;
        //search beforehand?
//        System.out.print("To search for a product enter a string or number: ");
//        String searchKey = scan.next();
//        Manager.searchExternalProducts(searchKey);
        int productID = safeInt(scan, "Please enter a product id: ");
        int manufacturer;
        int shipFrom;
        try {
            Regork.queries.get("checkShipFromIndiv").setInt(1, productID);
            result = Regork.queries.get("checkShipFromIndiv").executeQuery();
            if (!result.next()) {
                System.out.println(Regork.ANSI_RED + "\nNo products found" + Regork.ANSI_RESET);
                findShipInconsistency(scan);
            } else {
                manufacturer = result.getInt("Manufacturer");
                shipFrom = result.getInt("Ship_From");
                System.out.println(); //padding
                System.out.println("Product: " + Regork.ANSI_BOLD + productID + Regork.ANSI_BRESET);
                System.out.println("is manufactured by supplier: " + Regork.ANSI_BOLD + manufacturer + Regork.ANSI_BRESET);
                System.out.println("and is shipped by supplier: " + Regork.ANSI_BOLD + shipFrom + Regork.ANSI_BRESET);
                int option = -2;
                if(manufacturer != shipFrom) {
                    System.out.println(Regork.ANSI_RED + "Anomaly Found" + Regork.ANSI_RESET);
                    option = menu(scan, 2);
                } else {
                    System.out.println(Regork.ANSI_GREEN + "No Anomaly Found" + Regork.ANSI_RESET);
                    //should return to search?
                    return 0;
                }
                if(option == 1) {
                    Regork.queries.get("callFixShip").setInt(1, productID);
                    Regork.queries.get("callFixShip").execute();
                    System.out.println(Regork.ANSI_GREEN + "Anomaly Fixed... Returning to Data Checker Menu" + Regork.ANSI_RESET);
                } else if(option == 2 || option == 0) {
                    return 0;
                }
            }
        } catch (SQLException sqe) {
            Regork.exitUnknown();
        }
        return -1;
    }

    public static int menu(Scanner scan, int context) {
        int selection = -1;
        boolean isValid = false;
        System.out.println(); //padding
        while (!isValid) {
            if(context == 1) { //for data analyst main menu
                System.out.println("Main Menu > " + Regork.ANSI_BOLD + "Data Analyst" + Regork.ANSI_BRESET);
                System.out.println("Please select an option from the menu:");
                System.out.println("--------------------------------------");
                System.out.println("[1] View and fix shipping inconsistencies");
                System.out.println("[2] View and fix offering inconsistencies");
                System.out.println("[0] Go Back");
                System.out.println("[X] Quit Program");
            } else if (context == 2) {
                System.out.println("Main Menu > " + "Data Analyst > " + Regork.ANSI_BOLD + "Shipping Anomaly Found" + Regork.ANSI_BRESET);
            } else if (context == 3) {
                System.out.println("Main Menu > " + "Data Analyst > " + Regork.ANSI_BOLD + "Offers Anomaly Found" + Regork.ANSI_BRESET);
            }
            if(context == 2 || context == 3) {
                System.out.println("Would you like to:");
                System.out.println("------------------");
                System.out.println("[1] Fix this anomaly");
                System.out.println("[2] Leave this anomaly"); //maybe cut this option
                System.out.println("[0] Go Back");
                System.out.println("[X] Quit Program");
            }

            if (scan.hasNextInt()) {
                selection = scan.nextInt();
                if (selection >= 0 && selection < 3) {
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
