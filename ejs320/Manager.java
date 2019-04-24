/*
    Created By: Elliot J Scribner on 2019-04-23
    Student ID: ejs320
    Lab #: **Num**
    Manager: **Description**
 */

import java.sql.*;
import java.util.Scanner;

public class Manager extends Regork {
    public static void manage(Connection con, Scanner scan) {
        searchInput(scan);
        Boolean valid = false;
        while(!valid) { //runs twice?
            System.out.print("Enter an ID from the search results to show suppliers with the product in stock, \nor type S to search again: ");
            if(scan.hasNextInt()) {
                searchExternalInventory(scan.nextInt());
                valid = true;
            } else if ((scan.next().toUpperCase()).equals("S")) {
                System.out.println("Trying to search again");
                searchInput(scan);
            } else {
                System.out.println("invalid input"); //make recursive somehow, or add option for Quit?
            }
        }
    }

    public static void searchInput(Scanner scan) {
        System.out.println("Product lookup checks for name or product ID containing search key");
        System.out.print("Please enter a string or number to search products: ");
        String searchKey = scan.next(); //should be next line?
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
                Regork.queries.get("productSearchByName").setString(1, searchKey);
                result = Regork.queries.get("productSearchByName").executeQuery();
            } else {
                System.out.println("not str");
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
                table.print();

            }
        } catch (SQLException sqE) {
            System.out.println("Sql exception");
            System.out.println(sqE);
            sqE.printStackTrace();
        }
    }

    public static void searchExternalInventory(int productID) {
        ResultSet result;
        try{
            Regork.queries.get("inventoryByID").setInt(1, productID);
            result = Regork.queries.get("inventoryByID").executeQuery();
            if (!result.next()) {
                System.out.println("Result empty");
            } else {
                ResultSetMetaData setMetaData = result.getMetaData();
                CommandLineTable table = new CommandLineTable();
                table.setHeaders(setMetaData.getColumnLabel(1), setMetaData.getColumnLabel(2));
                do {
                    table.addRow(result.getString("Supplier"), result.getString("Stock"));
                } while (result.next());
                table.print();

            }
        } catch (SQLException sqE) {
            System.out.println("Sql exception");
            System.out.println(sqE); //** remove before turn in
            sqE.printStackTrace();
        }
    }
}
