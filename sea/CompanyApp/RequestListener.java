package sea.CompanyApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

public class RequestListener extends Thread {

  private ServerSocket sSocket;
  private Socket cSocket;
  private final int port;
  private CompanyApp companyApp;
  private PrintWriter out;

  public RequestListener(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try {
      sSocket = new ServerSocket(port);
      System.out.println("Server started. Waiting for connection...");

      while (!Thread.currentThread().isInterrupted()) {
        try (Socket clientSocket = sSocket.accept();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

          synchronized (this) {
            cSocket = clientSocket;
            this.out = out;
          }

          System.out.println("Connected to: " + clientSocket);
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            System.out.println("Message from client: " + inputLine);
            if (out != null) {
              processInput(inputLine, out);
            }
          }
        } catch (IOException e) {
          System.out.println("IOException in RequestListener run: " + e.getMessage());
          // Handle exception or continue to accept new connections
        }
      }
    } catch (IOException e) {
      System.out.println("IOException in RequestListener: " + e.getMessage());
    } finally {
      closeServerSocket();
    }
  }

  private void processInput(String inputLine, PrintWriter out) {
    String command = getCommand(inputLine);
    String value = getValue(inputLine);

    switch (command) {
      case "login":        //login:AIDA
        //TODO: Login und Logout gehören in ShipSession login(value);
        break;
      case "sendPos":        //sendPos:12,18
        sendPos(value);
        break;
      case "sendDir":        //sendDir:NORTH
        sendDir(value);
        break;
      case "sendProfit":      //sendProfit:20000
        sendProfit(value);
        break;
      case "logout":        //logout:AIDA
        //TODO: Login und Logout gehören in ShipSession
        logoutFromCompany(value);
        break;
      case "receiveOrder":    //receiveOrder:AIDA		??
        receiveOrder(value);
        break;
      case "endOrder":      //endOrder:AIDA			??
        endOrder(value);
        break;
      case "loadCargo":      //loadCargo:19
        loadCargo(value);
        break;
      case "unloadCargo":      //unloadCargo:19
        unloadCargo(value);
        break;
      default:
        break;
    }
  }
  //TODO: Login und Logout gehören in ShipSession
	/*private void login(String shipName) {
	    out.println(shipName + " has logged in.");
	    ShipSession shipSession = new ShipSession(companyApp.getCompanyApp(), cSocket);
	    shipSession.AddShip(shipSession.GetSocket());
	}*/

  private void sendProfit(String value) {
    String companyName = companyApp.getCompName();
    String sql = "UPDATE company SET Balance = Balance + " + value + " WHERE Name = " + companyName;
    Connection connection;
    try {
      connection = connectToDatabase();
      updateCompanyBalance(connection, sql);
      closeDatabaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void sendDir(String value) {
    String sql =
        "UPDATE ship SET Direction = '" + value + "' WHERE Name = '" + companyApp.getCompName()
            + "'";
    Connection connection;
    try {
      connection = connectToDatabase();
      updateShipDirection(connection, sql);
      closeDatabaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void sendPos(String value) {
    String[] positionValues = value.split(",");
    String PosX = positionValues[0].trim();
    String PosY = positionValues[1].trim();
    String sql = "UPDATE ship SET PosX = " + PosX + ", PosY = " + PosY + " WHERE Name = "
        + companyApp.getCompName();
    Connection connection;
    try {
      connection = connectToDatabase();
      updateShipPosition(connection, sql);
      closeDatabaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private String getCommand(String inputLine) {
    String[] splitCommand = inputLine.split(":");
    return (splitCommand.length > 0) ? splitCommand[0] : "";
  }

  private String getValue(String inputLine) {
    String[] splitCommand = inputLine.split(":");
    return (splitCommand.length > 1) ? splitCommand[1] : "";
  }

  private void logoutFromCompany(String shipName) {
    out.println(shipName + " has logged out.");
    companyApp.addShipSession(null);
  }

  private void receiveOrder(String shipName) {
    String message = shipName + " has accepted the Order.";
    displayMessageWindow(message);
  }

  private void endOrder(String value) {
    String message = "Cargo " + value + " has been successfully shipped!";
    displayMessageWindow(message);
  }

  private void loadCargo(String cargoID) {
    String sql = "UPDATE cargo SET Status = 1 WHERE CargoID = " + cargoID;
    Connection connection;

    try {
      connection = connectToDatabase();
      updateCargoStatus(connection, sql);
      closeDatabaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void unloadCargo(String cargoID) {
    String sql = "UPDATE cargo SET Status = 2 WHERE CargoID = " + cargoID;
    Connection connection;

    try {
      connection = connectToDatabase();
      updateCargoStatus(connection, sql);
      closeDatabaseConnection(connection);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Connection connectToDatabase() throws SQLException {
    String jdbcUrl = "jdbc:mysql://localhost:3305/seatradedb";
    String username = "root";
    String password = "";

    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  private void closeDatabaseConnection(Connection connection) {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      System.out.println("Error closing database connection: " + e.getMessage());
    }
  }

  private void updateCompanyBalance(Connection connection, String sql) {
    try {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(sql);
        out.println("Company balance updated");
      }
    } catch (SQLException e) {
      System.out.println("Error updating company balance: " + e.getMessage());
    }
  }

  private void updateCargoStatus(Connection connection, String sql) {
    try {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(sql);
        out.println("Cargo status updated");
      }
    } catch (SQLException e) {
      System.out.println("Error updating cargo status: " + e.getMessage());
    }
  }

  private void updateShipDirection(Connection connection, String sql) {
    try {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(sql);
        out.println("Ship direction updated");
      }
    } catch (SQLException e) {
      System.out.println("Error updating ship direction: " + e.getMessage());
    }
  }

  private void updateShipPosition(Connection connection, String sql) {
    try {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(sql);
        out.println("Ship position updated");
      }
    } catch (SQLException e) {
      System.out.println("Error updating ship position: " + e.getMessage());
    }
  }

  private void displayMessageWindow(String message) {
    JOptionPane.showMessageDialog(null, message, "Instant Message",
        JOptionPane.INFORMATION_MESSAGE);
  }

  public void closeServerSocket() {
    try {
      if (sSocket != null && !sSocket.isClosed()) {
        sSocket.close();
      }
    } catch (IOException e) {
      System.out.println("IOException while closing server socket: " + e.getMessage());
    }
  }
}
