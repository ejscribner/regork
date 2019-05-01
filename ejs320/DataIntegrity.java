/*
    Created By: Elliot J Scribner on 2019-04-30
    Student ID: ejs320
    Lab #: **Num**
    DataIntegrity: **Description**
 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class DataIntegrity {
    public static int check(Connection con, Scanner scan) {
        //from id != to id
        int option = menu(scan);
        if (option == 0) {
          //go back
          return -1; //should return a signal to a main controller to step backwards succesfully
        } else if(option == 1) {
            //shipping
            System.out.println("ship");
        } else if (option == 2) {
            System.out.println("offer");
            //offering
        }




        checkIfShipUpdated(4);
        checkShippingIndividual(con, 4);
        checkIfShipUpdated(4);
        return 1;
    }

    public static int menu(Scanner scan) {
        int selection = -1;

        boolean isValid = false;
        System.out.println(); //padding
        while (!isValid) {
            System.out.println("Main Menu > " + Regork.ANSI_BOLD + "Data Analyst" + Regork.ANSI_BRESET);
            System.out.println("Please select an option from the menu:");
            System.out.println("--------------------------------------");
            System.out.println("[1] View and fix shipping inconsistencies");
            System.out.println("[2] View and fix offering inconsistencies");
            System.out.println("[0] Go Back");
            System.out.println("[X] Quit Program");
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

    public static void checkShippingIndividual(Connection con, int productID) {
        ResultSet result;
        int manufacturer = -1;
        int shipFrom = -1;
        try {
            Regork.queries.get("checkShipFromIndiv").setInt(1, productID);
            result = Regork.queries.get("checkShipFromIndiv").executeQuery();
            if (!result.next()) {
                System.out.println("Result empty");
            } else {
                manufacturer = result.getInt("Manufacturer");
                shipFrom = result.getInt("Ship_From");
            }
            if (manufacturer != shipFrom) {
                ///update by calling procedure fix_ship
                Regork.queries.get("callFixShip").setInt(1, productID);
                Regork.queries.get("callFixShip").execute();
            }
        } catch (SQLException sqe) {
            System.out.println("sql exception");
            sqe.printStackTrace();
        }
    }

    public static void checkIfShipUpdated(int productID) {
        ResultSet result;
        try {
            Regork.queries.get("checkShipFromIndiv").setInt(1, productID);
            result = Regork.queries.get("checkShipFromIndiv").executeQuery();
            if(!result.next()) {
                System.out.println("Empty");
            } else {
                CommandLineTable table = new CommandLineTable();
                table.setHeaders("From", "To", "Manufacturer");
                do {
                    table.addRow(result.getString("Ship_From"), result.getString("Ship_To"), result.getString("Manufacturer"));
                } while (result.next());
                table.print();
            }
        } catch (SQLException sqe) {
            System.out.println("sql exception");
            sqe.printStackTrace();
        }
    }
}
