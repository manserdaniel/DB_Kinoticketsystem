package com.company;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Connection conn = null;
        Scanner scannerString = new Scanner(System.in);
        Scanner scanner = new Scanner(System.in);

        String url = "jdbc:mysql://localhost:3306/cinema?user=root";
        TicketBooking bookTickets = new TicketBooking();

        try {
            conn = DriverManager.getConnection(url);

            while (true) {
                System.out.println("Möchten sie eine neue Buchung aufgeben (y) oder eine bestehende stornieren (n)?");
                String answer = scannerString.nextLine();

                Statement stmt = null;
                stmt = conn.createStatement();

                if (answer.equals("y")) {

                    String query = "";
                    ResultSet rs;

                    printMovieProgram(stmt);

                    System.out.println("Welchen Film möchten Sie gerne sehen? Geben Sie die Nummer ein: ");
                    Integer movieIDOfOrder = scanner.nextInt();

                    System.out.println("Wie viele Karten?");
                    Integer seatsOfOrder = scanner.nextInt();

                    int freeSeats = getFreeSeats(stmt, movieIDOfOrder);

                    if (freeSeats >= seatsOfOrder) {

                        setBooking(stmt, movieIDOfOrder, seatsOfOrder);
                        printChoosenTheater(stmt, movieIDOfOrder);

                    } else {
                        System.out.println("Für diesen Film sind nicht genügend Plätze mehr frei!");
                    }
                } else if (answer.equals("n")) {

                    System.out.println("Geben sie die Buchungsnummer der Buchung ein die sie stornieren möchten:");
                    Integer orderToDelete = scanner.nextInt();

                    deleteBooking(stmt, orderToDelete);

                } else {
                    System.out.println("Diese Eingabe war nicht korrekt!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Database connection closed");
                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void deleteBooking(Statement stmt, Integer orderToDelete) throws SQLException {
        String updateSeats = "UPDATE theater" +
                " SET seats = seats + (SELECT seats FROM booking WHERE id = '" + orderToDelete + "')" +
                " WHERE id = (SELECT theaterId FROM booking WHERE id = '" + orderToDelete + "');";

        stmt.executeUpdate(updateSeats);

        String deleteBooking = "DELETE FROM booking WHERE booking.id = '" + orderToDelete + "';";
        stmt.executeUpdate(deleteBooking);
        System.out.println("Ihre Buchung wurde storniert");
    }

    private static void printChoosenTheater(Statement stmt, Integer movieIDOfOrder) throws SQLException {
        String query;
        ResultSet rs;
        query = "Select * from theater WHERE id = (SELECT theaterId FROM movie WHERE id = '" + movieIDOfOrder + "');";
        rs = stmt.executeQuery(query);

        while (rs.next()) {
            System.out.println("SAAL: " + rs.getInt("id"));
            System.out.println("Freie Plätze: " + rs.getString("seats"));
        }
    }

    private static void setBooking(Statement stmt, Integer movieIDOfOrder, Integer seatsOfOrder) throws SQLException {
        String query;
        ResultSet rs;// set booking
        query = "INSERT INTO `booking` (`movieId`, `seats`, `theaterId`) " +
                "VALUES ('" + movieIDOfOrder + "','"
                + seatsOfOrder + "'," +
                "(SELECT theaterId FROM movie WHERE id = '" + movieIDOfOrder + "'));";
        stmt.executeUpdate(query);

        // get bookingID
        query = "SELECT LAST_INSERT_ID();";
        rs = stmt.executeQuery(query);
        int bookingID = 0;

        if (rs.next()) {
            bookingID = rs.getInt("LAST_INSERT_ID()");
        }

        System.out.println("Deine bestellnummer ist: " + bookingID);

        // book seats of theater
        query = "UPDATE theater " +
                "SET seats = seats - " + seatsOfOrder +
                " WHERE id = (SELECT theaterId FROM movie WHERE id = '" + movieIDOfOrder + "');";
        stmt.executeUpdate(query);
    }

    private static int getFreeSeats(Statement stmt, Integer movieIDOfOrder) throws SQLException {
        String query;
        ResultSet rs;// check theater for free seats
        query = "SELECT seats FROM theater WHERE id = (SELECT theaterId FROM movie WHERE id = '" + movieIDOfOrder + "');";
        rs = stmt.executeQuery(query);

        int freeSeats = 0;
        if (rs.next()) {
            freeSeats = rs.getInt("seats");
        }
        return freeSeats;
    }

    private static void printMovieProgram(Statement stmt) throws SQLException {
        String query;
        ResultSet rs;
        query = "Select * from movie";
        rs = stmt.executeQuery(query);
        System.out.println("ID\tMovie\t\tSaal");
        while (rs.next()) {
            Integer id = rs.getInt("id");
            String movie = rs.getString("name");
            Integer theater = rs.getInt("theaterId");
            System.out.println(id + "\t" + movie + "  Saal: " + theater);
        }
    }
}
