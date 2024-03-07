package sea.CompanyApp;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RequestListener extends Thread {

  private final int port;
  private CompanyApp companyApp;
  private PrintWriter out;

  public RequestListener(int port) {
    this.port = port;
  }

  public void setCompanyApp(CompanyApp companyApp) {
    this.companyApp = companyApp;
  }

  @Override
  public void run() {
    try {
      System.out.println("Versuch Server");

      ServerSocket sSocket = new ServerSocket(port);
      System.out.println("Server started. Waiting for connection...");

      while (!Thread.currentThread().isInterrupted()) {
        Socket cSocket = sSocket.accept();
        System.out.println("Connected to: " + cSocket.getInetAddress());
        InputStream fromClient = cSocket.getInputStream();

        // Lese Nachrichten vom Client
        BufferedReader reader = new BufferedReader(new InputStreamReader(fromClient));
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
          processInput(line);
          // Hier kannst du die empfangenen Nachrichten verarbeiten
        }

        cSocket.close();

      }

    } catch (IOException e) {
      System.out.println("IOException in RequestListener: " + e.getMessage());
    }
  }

  public void processInput(String inputLine) {
    String command = getCommand(inputLine);
    String value = getValue(inputLine);

    switch (command) {
      case "sendPos": // sendPos:12,18
        sendPos(value);
        break;
      case "sendDir": // sendDir:NORTH
        sendDir(value);
        break;
      case "sendProfit": // sendProfit:20000
        int profit = Integer.parseInt(value);
        companyApp.addBalance(profit);
        sendProfit(value);
        break;
      case "sendCost":
        int money = Integer.parseInt(value);
        if(value == "") {money = 1000;}
        companyApp.addBalance(-money);
        sendCost(value);
        break;
      case "receiveOrder": // receiveOrder:AIDA ??
        receiveOrder(value);
        break;
      case "endOrder": // endOrder:19 ??
        endOrder(value);
        break;
      case "loadCargo": // loadCargo:19
        loadCargo(value);
        break;
      case "unloadCargo": // unloadCargo:19
        unloadCargo(value);
        break;
      default:
        break;
    }
  }

  private void sendCost(String value) {
    String companyName = companyApp.getCompName();
    Connection connection = null;
    try {
      connection = connectToDatabase();
      String sql = "UPDATE company SET Balance = Balance - ? WHERE Name = ?";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setString(1, value);
        preparedStatement.setString(2, companyName);

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          out.println("Company balance updated");
        } else {
          out.println("No company found with the specified name: " + companyName);
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error updating balance: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
    }
  }

  private void sendProfit(String value) {
    String companyName = companyApp.getCompName();
    Connection connection = null;
    try {
      connection = connectToDatabase();
      String sql = "UPDATE company SET Balance = Balance + ? WHERE Name = ?";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setString(1, value);
        preparedStatement.setString(2, companyName);

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          out.println("Company balance updated");
        } else {
          out.println("No company found with the specified name: " + companyName);
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error updating balance: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
    }
  }

  private void sendDir(String value) {
    Connection connection = null;
    try {
      connection = connectToDatabase();
      String sql = "UPDATE ship SET Direction = ? WHERE Name = ?";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setString(1, value);
        preparedStatement.setString(2, companyApp.getCompName());

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          out.println("Ship direction updated");
        } else {
          out.println("No ship found with the specified name: " + companyApp.getCompName());
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error updating ship direction: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
    }
  }

  private void sendPos(String value) {
    Connection connection = null;
    try {
      connection = connectToDatabase();
      String[] positionValues = value.split(",");
      String posX = positionValues[0].trim();
      String posY = positionValues[1].trim();
      String sql = "UPDATE ship SET PosX = ?, PosY = ? WHERE Name = ?";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setString(1, posX);
        preparedStatement.setString(2, posY);
        preparedStatement.setString(3, companyApp.getCompName());

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          out.println("Ship position updated");
        } else {
          out.println("No ship found with the specified name: " + companyApp.getCompName());
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error updating ship position: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
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

  private void receiveOrder(String shipName) {
    String message = shipName + " has accepted the Order.";
    displayMessageWindow(message);
  }

  private void endOrder(String value) {
    String message = "Cargo " + value + " has been successfully shipped!";
    displayMessageWindow(message);
  }

  private void loadCargo(String cargoID) {
    Connection connection = null;

    try {
      connection = connectToDatabase();
      String sql = "UPDATE cargo SET Status = 1 WHERE CargoID = ?";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setString(1, cargoID);

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          out.println("Cargo status updated");
        } else {
          out.println("No cargo found with the specified ID: " + cargoID);
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error updating cargo status: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
    }
  }

  private void unloadCargo(String cargoID) {
    Connection connection = null;

    try {
      connection = connectToDatabase();
      String sql = "UPDATE cargo SET Status = 2 WHERE CargoID = ?";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.setString(1, cargoID);

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          out.println("Cargo status updated");
        } else {
          out.println("No cargo found with the specified ID: " + cargoID);
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error updating cargo status: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
    }
  }

  private Connection connectToDatabase() throws SQLException {
// Load the MySQL JDBC driver
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    }

    String jdbcUrl = "jdbc:mysql://localhost:3306/seatradedb";
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

  private void displayMessageWindow(String message) {
    JOptionPane.showMessageDialog(null, message, "Instant Message",
            JOptionPane.INFORMATION_MESSAGE);
  }
}
