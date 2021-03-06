package net.jgp.labs.db.informix.record;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jgp.labs.db.informix.utils.ConnectionManager;
import net.jgp.labs.db.informix.utils.PrettyFormatter;

public class ReadRecord {
  private static transient Logger log = LoggerFactory.getLogger(
      ReadRecord.class);

  public static void main(String[] args) {
    ReadRecord rr = new ReadRecord();
    rr.executeDbOperation(
        "SELECT * FROM items ORDER BY item_num");
    rr.executeDbOperation(
        "SELECT * FROM manufact ORDER BY manu_code");
    rr.executeDbOperation(
        "SELECT * FROM items, manufact WHERE items.manu_code = manufact.manu_code ORDER BY item_num");
  }

  public boolean executeDbOperation(String query) {
    Statement statement = ConnectionManager.getStatement();
    if (statement == null) {
      return false;
    }

    ResultSet resultSet;
    try {
      resultSet = statement.executeQuery(query);
    } catch (SQLException e) {
      log.error("Could not get a result set: {}", e
          .getMessage(), e);
      return false;
    }

    PrettyFormatter pf = new PrettyFormatter();
    pf.set(resultSet);
    pf.show();

    return true;
  }

  /**
   * This function really only works with the 'items' table, as the display
   * assumes you have only those columns...
   * 
   * @param query
   * @return
   */
  public boolean executeDbOperationDetailed(String query) {
    try {
      Class.forName("com.informix.jdbc.IfxDriver");
    } catch (ClassNotFoundException e) {
      System.err.println(
          "Could not access JDBC driver's class: " + e
              .getMessage());
      return false;
    }

    String hostname = "[::1]";
    int port = 33378;
    String user = "informix";
    String password = "in4mix";
    String database = "stores_demo";
    String informixServer = "lo_informix1210";
    Connection connection;

    String jdbcUrl = "jdbc:informix-sqli://" + hostname
        + ":" + port + "/" + database + ":INFORMIXSERVER="
        + informixServer;
    try {
      connection = DriverManager.getConnection(jdbcUrl,
          user, password);
    } catch (SQLException e) {
      System.err.println(
          "Could not get a connection to the database: " + e
              .getMessage());
      return false;
    }

    Statement statement;
    try {
      statement = connection.createStatement();
    } catch (SQLException e) {
      System.out.println("Could not create a statement: "
          + e.getMessage());
      return false;
    }

    ResultSet resultSet;
    try {
      resultSet = statement.executeQuery(query);
    } catch (SQLException e) {
      System.out.println("Could not get a result set: " + e
          .getMessage());
      return false;
    }

    System.out.println(
        "+--------+---------+---------+---------+--------+-----------+");
    System.out.println(
        "|item_num|order_num|stock_num|manu_code|quantity|total_price|");
    System.out.println(
        "+--------+---------+---------+---------+--------+-----------+");
    try {
      while (resultSet.next()) {
        // Retrieve by column name

        int itemNum = resultSet.getInt("item_num");
        int orderNum = resultSet.getInt("order_num");
        int stockNum = resultSet.getInt("stock_num");
        String manufacturerCode = resultSet.getString(
            "manu_code");
        int quantity = resultSet.getInt("quantity");
        double totalPrice = resultSet.getDouble(
            "total_price");

        // Display values
        System.out.printf("|%8d|%9d|%9d|%-9s|%8d|%11.2f|\n",
            itemNum, orderNum, stockNum, manufacturerCode,
            quantity, totalPrice);
      }
      System.out.println(
          "+--------+---------+---------+---------+--------+-----------+");
    } catch (SQLException e) {
      System.out.println("Error while browsing result set: "
          + e.getMessage());
      return false;
    }
    return true;
  }

}
