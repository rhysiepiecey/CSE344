package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement flightCapacityStmt;
  private static final String GET_DIRECT_FLIGHTS = "SELECT TOP (?) " +
  "fid,day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
  + "FROM Flights "
  + "WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? " +
  "AND canceled = 0 "
  + "ORDER BY actual_time ASC, fid ASC";
private PreparedStatement getDirectFlightsStatement;

private static final String GET_NONDIRECT_FLIGHTS = "SELECT TOP (?) " +
"A.fid as A_fid,A.day_of_month as A_day_of_month," +
"A.carrier_id as A_carrier_id,A.flight_num as A_flight_num,A.origin_city as A_origin_city," +
"A.dest_city as A_dest_city,A.actual_time as A_actual_time,A.capacity as A_capacity,A.price as A_price, " +
"B.fid as B_fid,B.day_of_month as B_day_of_month," +
"B.carrier_id as B_carrier_id,B.flight_num as B_flight_num,B.origin_city as B_origin_city," +
"B.dest_city as B_dest_city,B.actual_time as B_actual_time,B.capacity as B_capacity,B.price as B_price, " +
"A.actual_time + B.actual_time as total_time "
+ "FROM Flights A, Flights B "
+ "WHERE A.origin_city = ? AND B.dest_city = ? AND A.day_of_month = ? AND A.day_of_month = B.day_of_month" +
" AND A.dest_city = B.origin_city AND A.canceled = 0 and B.canceled = 0 "
+ "ORDER BY total_time ASC, A_fid ASC, B_fid ASC";
private PreparedStatement getNonDirectFlightsStatement;

//private static final String CLEAR_RESERVATIONS = "TRUNCATE TABLE Reservations_rshah04";
private static final String CLEAR_RESERVATIONS = "DELETE FROM Reservations_rshah04";
PreparedStatement clearReservations;
//private static final String CLEAR_USERS = "TRUNCATE TABLE Users_rshah04";
private static final String CLEAR_USERS = "DELETE FROM Users_rshah04";
PreparedStatement clearUsers;
private static final String LOGIN_STRING= "SELECT * FROM USERS_rshah04 WHERE username = ?";
PreparedStatement loginStatement;
private static final String CREATE_USER = "INSERT INTO Users_rshah04 VALUES (?, ?, ?)";
PreparedStatement createUser;
private static final String GET_HASHED_PASSWORD = "SELECT hashedPassword FROM USERS_rshah04 WHERE username = ?";
PreparedStatement getHashedPassword;
private static final String CHECK_USER_RESERVERATIONS = "SELECT * FROM Reservations_rshah04 JOIN Flights ON Reservations_rshah04.fid1 = Flights.fid WHERE username = ? AND day_of_month = ?";
PreparedStatement checkUserReservations;
private static final String COUNT_RESERVATIONS = "SELECT COUNT(*) as Reservation_count FROM Reservations_rshah04";
PreparedStatement count_reservations;
private static final String MATCHING_RESERVATION_COUNT = "SELECT COUNT(*) as Reservation_count FROM Reservations_rshah04 WHERE fid1 = ? OR fid2 = ?";
PreparedStatement matchingReservationCount;
private static final String INSERT_RESERVATION = "INSERT INTO Reservations_rshah04 VALUES(?, 0, ?, ?, ?)";
PreparedStatement insert_reservation;
private static final String CHECK_RESERVATION_FOR_USER = "SELECT * FROM Reservations_rshah04 WHERE username = ? AND res_id = ? AND paid = ?";
PreparedStatement checkReservationForUser;
private static final String CHECK_FID_FROM_RESERVATION = "SELECT * FROM Reservations_rshah04 WHERE res_id = ? AND username = ?";
PreparedStatement checkFidFromReservation;
private static final String GET_PRICE = "SELECT price FROM FLIGHTS WHERE fid = ?";
PreparedStatement getPrice;
private static final String GET_BALANCE = "SELECT balance FROM USERS_rshah04 WHERE username = ?";
PreparedStatement getBalance;
private static final String UPDATE_BALANCE = "UPDATE Users_rshah04 SET balance = ? WHERE username = ?";
private PreparedStatement PayStatusStatement;
private static final String PAY_STATUS = "UPDATE reservations_rshah04 SET paid = ? WHERE username = ? AND res_id = ?";
PreparedStatement updateBalance;
private static final String GET_RESERVATION = "SELECT * FROM Reservations_rshah04 WHERE username = ?";
PreparedStatement getReservation;
private static final String GET_FLIGHT_FROM_FID = "SELECT * FROM FLIGHTS WHERE fid = ?";
PreparedStatement getFlightFromFid;
private static final String GET_INFO_FROM_RESERVATION = "SELECT res_id, paid, fid1, fid2 FROM reservations_rshah04 WHERE username = ?";
PreparedStatement getInfoFromReservation;
private PreparedStatement CheckUnpaidReservationStatement;
  private static final String CHECK_UNPAID_RESERVATIONS = "SELECT * FROM Reservations_rshah04 WHERE res_id = ? AND paid = ?";
// private static final String SET_FOREIGN_STRING = "SET FOREIGN_KEY_CHECKS=0;";
// PreparedStatement foreignKeysOff;
// private static final String SET_FOREIGN_ON = "SET FOREIGN_KEY_CHECKS=1;";
// PreparedStatement foreignKeysOn;

  //
  // Instance variables
  //
  PasswordUtils n = new PasswordUtils();
  private String loggedInUser;
  private Itinerary[] searchResults;
  private int searchResultCount;
  List<Itinerary> list;

  protected Query() throws SQLException, IOException {
    prepareStatements();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      //clearReservations.clearParameters(); MAYBE??
      //foreignKeysOff.executeUpdate();
      clearReservations.executeUpdate();
      clearUsers.executeUpdate();
      //clearUsers.clearParameters(); MAYBE??


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);

    // TODO: YOUR CODE HERE
    getDirectFlightsStatement = conn.prepareStatement(GET_DIRECT_FLIGHTS);
    clearReservations = conn.prepareStatement(CLEAR_RESERVATIONS);
    clearUsers = conn.prepareStatement(CLEAR_USERS);
    getNonDirectFlightsStatement = conn.prepareStatement(GET_NONDIRECT_FLIGHTS);
    loginStatement = conn.prepareStatement(LOGIN_STRING);
    createUser = conn.prepareStatement(CREATE_USER);
    getHashedPassword= conn.prepareStatement(GET_HASHED_PASSWORD);
    checkUserReservations = conn.prepareStatement(CHECK_USER_RESERVERATIONS);
    count_reservations = conn.prepareStatement(COUNT_RESERVATIONS);
    insert_reservation = conn.prepareStatement(INSERT_RESERVATION);
    matchingReservationCount = conn.prepareStatement(MATCHING_RESERVATION_COUNT);
    checkReservationForUser = conn.prepareStatement(CHECK_RESERVATION_FOR_USER);
    checkFidFromReservation = conn.prepareStatement(CHECK_FID_FROM_RESERVATION);
    getPrice = conn.prepareStatement(GET_PRICE);
    getBalance = conn.prepareStatement(GET_BALANCE);
    updateBalance = conn.prepareStatement(UPDATE_BALANCE);
    getReservation = conn.prepareStatement(GET_RESERVATION);
    getFlightFromFid = conn.prepareStatement(GET_FLIGHT_FROM_FID);
    getInfoFromReservation = conn.prepareStatement(GET_INFO_FROM_RESERVATION);
    CheckUnpaidReservationStatement = conn.prepareStatement(CHECK_UNPAID_RESERVATIONS);
    PayStatusStatement = conn.prepareStatement(PAY_STATUS);
    // foreignKeysOff = conn.prepareStatement(SET_FOREIGN_STRING);
    // foreignKeysOn = conn.prepareStatement(SET_FOREIGN_ON);
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_login(String username, String password){
    if(loggedInUser != null) {
      return  "User already logged in\n";
    }
    try {
      conn.setAutoCommit(false);
      byte[] hash = n.saltAndHashPassword(password);
      
      getHashedPassword.setString(1, username);
      ResultSet rs = getHashedPassword.executeQuery();
      rs.next();
      byte[] hashFromTable = rs.getBytes("hashedPassword");

      if(n.plaintextMatchesSaltedHash(password,hashFromTable)) {
        loggedInUser = username;
        conn.commit();
        conn.setAutoCommit(true);
        return "Logged in as " + username + "\n";
      }
      // } else {
      //   return "Login failed\n no result set";
      // }
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        conn.rollback();
        conn.setAutoCommit(true);
        if(isDeadlock(e)) {
          return transaction_login(username, password);
        }
      } catch(SQLException e2) {
        e2.printStackTrace();
      }
    }
    //check the user table for if that username and password exists
    //check if a user is already logged in
    //return 
    return "Login failed\n";
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    // TODO: YOUR CODE HERE
    //create new used
    if(initAmount < 0) {
      throw new IllegalArgumentException();
    }
    try {
      conn.setAutoCommit(false);
      //check if that username already exists
      createUser.setString(1, username); 
      byte[] hash = n.saltAndHashPassword(password);
      createUser.setBytes(2, hash);
      createUser.setInt(3,initAmount);
      createUser.executeUpdate();
      conn.commit();
      conn.setAutoCommit(true);
      return "Created user " + username +"\n";
  } catch(SQLException e) {
    //e.printStackTrace();
    try {
      conn.rollback();
      conn.setAutoCommit(true);
      if(isDeadlock(e)) {
        return transaction_createCustomer(username, password, initAmount);
      }
    } catch(SQLException e2) {
      e2.printStackTrace();
    }
    return "Failed to create user\n";
  }

  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {
    // WARNING: the below code is insecure (it's susceptible to SQL injection attacks) AND only
    // handles searches for direct flights.  We are providing it *only* as an example of how
    // to use JDBC; you are required to replace it with your own secure implementation.
    //
    // TODO: YOUR CODE HERE

    String resultStr = "";

    try {
      conn.setAutoCommit(false);
      getDirectFlightsStatement.clearParameters();
      getDirectFlightsStatement.setInt(1, numberOfItineraries);
      getDirectFlightsStatement.setString(2, originCity);
      getDirectFlightsStatement.setString(3, destinationCity);
      getDirectFlightsStatement.setInt(4, dayOfMonth);

      //searchResults = new Itinerary[numberOfItineraries];
      list = new ArrayList<>();
      int countOfItineraries = 0;
      ResultSet results = getDirectFlightsStatement.executeQuery();
      while(results.next() && list.size() < numberOfItineraries)
      {
        //searchResults[countOfItineraries] = new Itinerary(new Flight(results, ""));
        list.add(new Itinerary(new Flight(results, "")));
      }
      Collections.sort(list);

      results.close();

      if (directFlight == false)
      {
        // find one stop flights also
        getNonDirectFlightsStatement.clearParameters();
        getNonDirectFlightsStatement.setInt(1, numberOfItineraries - list.size());
        getNonDirectFlightsStatement.setString(2, originCity);
        getNonDirectFlightsStatement.setString(3, destinationCity);
        getNonDirectFlightsStatement.setInt(4, dayOfMonth);

        ResultSet resultsND = getNonDirectFlightsStatement.executeQuery();
        while(resultsND.next() && list.size() < numberOfItineraries)
        {
          list.add(new Itinerary(new Flight(resultsND, "A_"),
                   new Flight(resultsND, "B_")));
          // searchResults[countOfItineraries] = new Itinerary(new Flight(resultsND, "A_"),
          //         new Flight(resultsND, "B_"));
          //countOfItineraries++;
        }
        Collections.sort(list);

        resultsND.close();
      }


      if (list.size() == 0)
      {
        return "No flights match your selection\n";
      }
          
      //Collections.sort(list);

      for (int i = 0; i < list.size(); i++)
      {
        resultStr += "Itinerary " + i + ": ";
        resultStr += list.get(i).toString();
      }

      searchResultCount = list.size();
      conn.commit();
      conn.setAutoCommit(true);
      return resultStr;
    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.setAutoCommit(true);
        if(isDeadlock(e)) {
          return transaction_search(originCity, destinationCity, 
          directFlight, dayOfMonth, numberOfItineraries);
        }
      } catch(SQLException e2) {
        e2.printStackTrace();
      }
      e.printStackTrace();
      return "Failed to search\n";
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_book(int itineraryId) {
    // TODO: YOUR CODE HERE
    if(loggedInUser == null) {
      return "Cannot book reservations, not logged in\n";
    }
    if(list == null || list.size() <= itineraryId) {
      return "No such itinerary " + itineraryId + "\n";
    }
    Itinerary currentItinerary = list.get(itineraryId);
    int dayOfMonth = list.get(itineraryId).flight1.dayOfMonth;

    try {
      conn.setAutoCommit(false);
      matchingReservationCount.clearParameters();
      matchingReservationCount.setInt(1, currentItinerary.flight1.fid);
      matchingReservationCount.setInt(2, currentItinerary.flight1.fid);
      ResultSet match = matchingReservationCount.executeQuery();
      //we have the number of reservations for flight1
      int count;
      if(!match.next()) {
        count = 0;
      } else {
        count = match.getInt("Reservation_count");
      }
      if(checkFlightCapacity(currentItinerary.flight1.fid) - count <= 0) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "Booking failed\n";
      }
      if(currentItinerary.flight2 != null) {
        matchingReservationCount.clearParameters();
        matchingReservationCount.setInt(1, currentItinerary.flight2.fid);
        matchingReservationCount.setInt(2, currentItinerary.flight2.fid);
        ResultSet match2 = matchingReservationCount.executeQuery();
        if(!match2.next()) {
          count = 0;
        } else {
          count = match2.getInt("Reservation_count");
        }
        if(checkFlightCapacity(currentItinerary.flight2.fid) - count <= 0) {
          conn.rollback();
          conn.setAutoCommit(true);
          return "Booking failed\n";
        }
      }
      checkUserReservations.clearParameters();
      checkUserReservations.setString(1, loggedInUser);
      checkUserReservations.setInt(2, dayOfMonth);
      ResultSet reservations = checkUserReservations.executeQuery();
      if(reservations.next()) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "You cannot book two flights in the same day\n";
      }
      //SELECT COUNT(*) FROM RESERVATIONS_rshah04
      ResultSet rs = count_reservations.executeQuery();
      int resId;
      if(!rs.next()) {
        resId = 1;
      } else {
        resId = rs.getInt("Reservation_count") + 1;
      }
      insert_reservation.clearParameters();
      insert_reservation.setInt(1, resId);
      insert_reservation.setString(2, loggedInUser);
      insert_reservation.setInt(3, currentItinerary.flight1.fid);
      if(currentItinerary.flight2 == null) {
        insert_reservation.setNull(4, java.sql.Types.INTEGER);
      } else { 
        insert_reservation.setInt(4,currentItinerary.flight2.fid);
      }
     insert_reservation.executeUpdate();
     conn.commit();
     conn.setAutoCommit(true);
      return "Booked flight(s), reservation ID: " + resId + "\n";
    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.setAutoCommit(true);
        if(isDeadlock(e)) {
          return transaction_book(itineraryId);
        }
      } catch(SQLException e2) {
        e2.printStackTrace();
      }
      e.printStackTrace();
      return "Booking failed\n";
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_pay(int reservationId) {
    if(loggedInUser == null) {
      return "Cannot pay, not logged in\n";
    }
    try {
      conn.setAutoCommit(false);
      checkReservationForUser.clearParameters();
      checkReservationForUser.setString(1, loggedInUser);
      checkReservationForUser.setInt(2, reservationId);
      checkReservationForUser.setInt(3, 0);
      ResultSet rSet = checkReservationForUser.executeQuery();
      if(!rSet.next()){
        conn.rollback();
        conn.setAutoCommit(true);
        return "Cannot find unpaid reservation " + reservationId + " under user: " + loggedInUser + "\n";
      }

      //take the fid1 and fid2 from reservations, check them in flights, and then add their price
      checkFidFromReservation.clearParameters();
      checkFidFromReservation.setInt(1, reservationId);
      checkFidFromReservation.setString(2, loggedInUser);
      ResultSet fids = checkFidFromReservation.executeQuery();
      int fid1 = fids.getInt("fid1");
      int fid2 = fids.getInt("fid2");

      //get flight1 price
      getPrice.clearParameters();
      getPrice.setInt(1, fid1);
      ResultSet prices = getPrice.executeQuery();
      int price1 = prices.getInt("price");

      getPrice.clearParameters();
      getPrice.setInt(1, fid2);
      prices = getPrice.executeQuery();
      int price2 = prices.getInt("price");

      int overallPrice = price1 + price2;
      //get user balance
      getBalance.clearParameters();
      getBalance.setString(1, loggedInUser);
      ResultSet r = getBalance.executeQuery();
      int balance = r.getInt("balance");

      if(overallPrice > balance) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "User has only " + balance + "in account but itinerary costs " + overallPrice + "\n";
      }
      
      updateBalance.clearParameters();
      updateBalance.setInt(1, balance - overallPrice);
      updateBalance.setString(2, loggedInUser);
      updateBalance.executeUpdate();

      PayStatusStatement.setInt(1,1);
      PayStatusStatement.setString(2,loggedInUser);
      PayStatusStatement.setInt(3,reservationId);
      PayStatusStatement.executeUpdate();
      int remaining = balance- overallPrice;
      conn.commit();
      conn.setAutoCommit(true);
      return "Paid reservation: " + reservationId + " remaining balance: " + remaining  + "\n"; 

    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.setAutoCommit(true);
        if(isDeadlock(e)) {
          return transaction_pay(reservationId);
        }
      } catch(SQLException e2) {
        e2.printStackTrace();
      }
      e.printStackTrace();
      return  "Failed to pay for reservation " + reservationId + "\n";
    }
    // pay allows a user to pay for an existing reservation.
    // It first checks whether the user has enough money to pay for all the flights in the given reservation.
    // If successful, it updates the reservation to be paid
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_reservations() {
    // TODO: YOUR CODE HERE
    if(loggedInUser == null) {
      return "Cannot view reservations, not logged in\n";
    }
      try {
        conn.setAutoCommit(false);
        getInfoFromReservation.clearParameters();
        getInfoFromReservation.setString(1, loggedInUser);
        ResultSet resultSet = getInfoFromReservation.executeQuery();
        // if(!resultSet.next()) {
        //   conn.rollback();
        //   conn.setAutoCommit(true);
        //   return "No reservations found (EMPTY)\n";
        // }
        
        StringBuffer sbReservation = new StringBuffer();
        while(resultSet.next()){
          int rid = resultSet.getInt("res_id");
          boolean paid = resultSet.getBoolean("paid");
          int fid1 = resultSet.getInt("fid1");
          int fid2 = resultSet.getInt("fid2");
  
          String FirstLine = "Reservation "+rid+" paid: "+paid+":\n";

          Flight FlightInfo = new Flight(fid1);
          String SecondLine = FlightInfo.toString()+"\n";
  
          if(fid2 == 0){
            sbReservation.append(FirstLine+SecondLine);
          }
          else {
            FlightInfo = new Flight(fid2);
            String ThirdLine = FlightInfo.toString()+"\n";
            sbReservation.append(FirstLine+SecondLine+ThirdLine);
          }
        }
  
        if (sbReservation.toString().isEmpty()){
          conn.rollback();
          conn.setAutoCommit(true);
          return "No reservations found\n";
        }
        conn.commit();
        conn.setAutoCommit(true);
        return sbReservation.toString();
      }
      catch(SQLException e){
        
        e.printStackTrace();
        try {
          conn.rollback();
          conn.setAutoCommit(true);
          if(isDeadlock(e)) {
            return transaction_reservations();
          }
        } catch(SQLException e2) {
          e2.printStackTrace();
        }
        
        return "Failed to retrieve reservations\n";
      }
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();
    return capacity;
  }

  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return e.getErrorCode() == 1205;
  }

  /**
   * A class to store information about a single flight
   *
   * TODO(hctang): move this into QueryAbstract
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
    }

    public Flight(ResultSet r, String prefix) throws SQLException
    {
      this.fid = r.getInt(prefix + "fid");
      this.dayOfMonth = r.getInt(prefix + "day_of_month");
      this.carrierId = r.getString(prefix + "carrier_id");
      this.flightNum = r.getString(prefix + "flight_num");
      this.originCity = r.getString(prefix + "origin_city");
      this.destCity = r.getString(prefix + "dest_city");
      this.time = r.getInt(prefix + "actual_time");
      this.capacity = r.getInt(prefix + "capacity");
      this.price = r.getInt(prefix + "price");
    }
    public Flight(int fid) {
      try {

        getFlightFromFid.setInt(1,fid);
        ResultSet Flight_Info = getFlightFromFid.executeQuery();
        Flight_Info.next();
        this.fid = Flight_Info.getInt("fid");
        this.dayOfMonth = Flight_Info.getInt("day_of_month");
        this.carrierId = Flight_Info.getString("carrier_id");
        this.flightNum = Flight_Info.getString("flight_num");
        this.originCity = Flight_Info.getString("origin_city");
        this.destCity = Flight_Info.getString("dest_city");
        this.time = Flight_Info.getInt("actual_time");
        this.capacity = Flight_Info.getInt("capacity");
        this.price = Flight_Info.getInt("price");
        Flight_Info.close();
      }
      catch (SQLException e){

      }
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price;
    }
  }
  class Itinerary implements Comparable<Itinerary>
  {
    public Flight flight1;
    public Flight flight2;
    public boolean direct;
    public int totalDuration;

    public Itinerary(Flight f)
    {
      direct = true;
      flight1 = f;
      flight2 = null;
      totalDuration = flight1.time;
    }

    public Itinerary(Flight f1, Flight f2)
    {
      direct = false;
      flight1 = f1;
      flight2 = f2;
      totalDuration = flight1.time + flight2.time;
    }

    @Override
    public String toString()
    {
      int flightCount = direct ? 1 : 2;
      int flightTime = direct ? flight1.time : flight1.time + flight2.time;
      return flightCount + " flight(s), " + flightTime + " minutes\n" + flight1.toString() + "\n" +
              (direct == false ? flight2.toString() + "\n" : "");
    }

    @Override
    public int compareTo(Itinerary other) 
      { 
        if(this.totalDuration == other.totalDuration) {
          if(this.flight1.fid == other.flight1.fid) {
            if(this.flight2!= null && other.flight2 != null) {
              return this.flight2.fid - other.flight2.fid;
            } else if(this.flight2 != null && other.flight2 == null) {
              return 1;
            } else {
              return -1;
            }
          } else {
            return this.flight1.fid - other.flight1.fid;
          }
        } else {
          return this.totalDuration - other.totalDuration;
        }
      }
  
  }

}


