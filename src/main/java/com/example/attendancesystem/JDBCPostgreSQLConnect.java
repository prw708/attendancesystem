package com.example.attendancesystem;

import at.favre.lib.crypto.bcrypt.BCrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JDBCPostgreSQLConnect {
    private String path = System.getProperty("user.dir");
    private String dbUrl = "jdbc:h2:" + path + "\\attendancesystem;CIPHER=AES";
    private String dbUser = "admin";
    private String dbPassword = null;
    private final String configFileName = path + "\\attendancesystem_config.txt";
    private static String configKey;

    public JDBCPostgreSQLConnect() {
        readDBPassword(configKey);
    }

    public JDBCPostgreSQLConnect(String k) {
        setConfigKey(k);
        readDBPassword(k);
    }

    public static void setConfigKey(String k) {
        configKey = k;
    }

    public void readDBPassword(String key) {
        if (key != null) {
            try {
                byte[] cipherText = Files.readAllBytes(Paths.get(configFileName));
                SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] plainText = cipher.doFinal(cipherText);
                dbPassword = key + " " + new String(plainText);
            } catch (IOException e) {
                dbPassword = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            dbPassword = null;
        }
    }

    public boolean writeDBPassword(String password) {
        if (configKey != null) {
            try {
                SecretKey secretKey = new SecretKeySpec(configKey.getBytes(), "AES");
                byte[] passwordInBytes = password.getBytes();
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] cipherTextInBytes = cipher.doFinal(passwordInBytes);
                Files.write(Paths.get(configFileName), cipherTextInBytes);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean register(String firstName, String lastName, String username, String password) {
        if (dbPassword == null) {
            dbPassword = configKey + " " + password;
        }
        firstName = firstName.replace("\'", "\'\'");
        lastName = lastName.replace("\'", "\'\'");
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            Integer resultCount = statement.executeUpdate("CREATE TABLE IF NOT EXISTS attendanceusers(" +
                    "id IDENTITY PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(100) NOT NULL, " +
                    "firstName VARCHAR(50) NOT NULL, lastName VARCHAR(50) NOT NULL, " +
                    "lastLogin TIMESTAMP, admin BOOLEAN)");
            statement = connection.createStatement();
            resultCount = statement.executeUpdate("CREATE TABLE IF NOT EXISTS attendance(" +
                    "id IDENTITY PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "checkIn TIMESTAMP, " +
                    "checkOut TIMESTAMP)");
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS count FROM attendanceusers");
            Integer count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt("count");
            }
            String bcryptHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            LocalDateTime lastLogin = LocalDateTime.now();
            statement = connection.createStatement();
            if (count > 0) {
                resultCount = statement.executeUpdate("INSERT INTO " +
                        "attendanceusers(username, password, firstName, lastName, lastLogin, admin)" +
                        "VALUES ('" +
                        username + "', '" +
                        bcryptHash + "', '" +
                        firstName + "', '" +
                        lastName + "', '" +
                        lastLogin + "', " +
                        "false)");
            } else {
                writeDBPassword(password);
                resultCount = statement.executeUpdate("INSERT INTO " +
                        "attendanceusers(username, password, firstName, lastName, lastLogin, admin)" +
                        "VALUES ('" +
                        username + "', '" +
                        bcryptHash + "', '" +
                        firstName + "', '" +
                        lastName + "', '" +
                        lastLogin + "', " +
                        "true)");
            }
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login(String username, String password) {
        if (dbPassword == null) {
            return false;
        }
        boolean check = checkPassword(username, password);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ATTENDANCEUSERS", null);
            if (tables.next()) {
                if (check) {
                    Statement statement = connection.createStatement();
                    LocalDateTime lastLogin = LocalDateTime.now();
                    Integer count = statement.executeUpdate(
                            "UPDATE attendanceusers SET lastLogin='" + lastLogin + "' WHERE username='" + username + "'"
                    );
                    return true;
                } else {
                    return false;
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkPassword(String username, String password) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ATTENDANCEUSERS", null);
            if (tables.next()) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT password FROM attendanceusers WHERE username='" + username + "'"
                );
                if (resultSet.next()) {
                    String hash = resultSet.getString("password");
                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
                    if (result.verified) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean usernameInUse(String username) {
        if (dbPassword == null) {
            return false;
        }
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ATTENDANCEUSERS", null);
            if (tables.next()) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT username FROM attendanceusers WHERE username = '" + username + "'"
                );
                if (resultSet.next()) {
                    return true;
                }
            }
            return false;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getFirstName(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ATTENDANCEUSERS", null);
            if (tables.next()) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT firstName FROM attendanceusers WHERE username = '" + username + "'"
                );
                if (resultSet.next()) {
                    return resultSet.getString("firstName");
                }
            }
            return null;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getAdmin(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT admin FROM attendanceusers WHERE username = '" + username + "'"
            );
            if (resultSet.next()) {
                return resultSet.getBoolean("admin");
            }
            return false;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAdminUser() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT username FROM attendanceusers WHERE admin = 'true'"
            );
            if (resultSet.next()) {
                return resultSet.getString("username");
            }
            return null;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getCheckedIn(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT username FROM attendance WHERE username = '" + username + "' AND checkout IS NULL"
            );
            if (resultSet.next()) {
                return true;
            }
            return false;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LocalDateTime checkIn(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            LocalDateTime current = LocalDateTime.now();
            Integer count = statement.executeUpdate(
                    "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + current + "', NULL)"
            );
            return current;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LocalDateTime checkOut(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            LocalDateTime checkInTime = null;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT checkin FROM attendance WHERE username = '" + username + "' AND checkout IS NULL"
            );
            if (resultSet.next()) {
                Timestamp checkIn = resultSet.getTimestamp("checkin");
                if (checkIn != null) {
                    checkInTime = checkIn.toLocalDateTime();
                }
            }
            if (checkInTime != null) {
                LocalDateTime current = LocalDateTime.now();
                LocalDateTime moveTo = LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 0, 0, 0);
                LocalDateTime midnight = LocalDateTime.of(checkInTime.getYear(), checkInTime.getMonth(), checkInTime.getDayOfMonth(), 0, 0, 0);
                if (!moveTo.equals(midnight)) {
                    midnight = midnight.plusHours(24);
                    Integer count = statement.executeUpdate(
                            "UPDATE attendance SET checkout='" + midnight + "' WHERE username = '" + username + "' AND checkout IS NULL"
                    );
                    while (!moveTo.equals(midnight)) {
                        LocalDateTime nextMidnight = midnight.plusHours(24);
                        count = statement.executeUpdate(
                                "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + midnight + "', '" + nextMidnight + "')"
                        );
                        midnight = midnight.plusHours(24);
                    }
                    count = statement.executeUpdate(
                            "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + midnight + "', '" + current + "')"
                    );
                } else {
                    Integer count = statement.executeUpdate(
                            "UPDATE attendance SET checkout='" + current + "' WHERE username = '" + username + "' AND checkout IS NULL"
                    );
                }
                return current;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean timeEntry(String username, LocalDateTime checkin, LocalDateTime checkout) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            LocalDateTime moveTo = LocalDateTime.of(checkout.getYear(), checkout.getMonthValue(), checkout.getDayOfMonth(), 0, 0, 0);
            LocalDateTime midnight = LocalDateTime.of(checkin.getYear(), checkin.getMonthValue(), checkin.getDayOfMonth(), 0, 0, 0);
            if (!moveTo.equals(midnight)) {
                midnight = midnight.plusHours(24);
                Integer count = statement.executeUpdate(
                        "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + checkin + "', '" + midnight + "')"
                );
                while (!moveTo.equals(midnight)) {
                    LocalDateTime nextMidnight = midnight.plusHours(24);
                    count = statement.executeUpdate(
                            "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + midnight + "', '" + nextMidnight + "')"
                    );
                    midnight = midnight.plusHours(24);
                }
                count = statement.executeUpdate(
                        "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + midnight + "', '" + checkout + "')"
                );
            } else {
                Integer count = statement.executeUpdate(
                        "INSERT INTO attendance(username, checkin, checkout) VALUES('" + username + "', '" + checkin + "', '" + checkout + "')"
                );
            }
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public HashMap<Integer, ArrayList<LocalDateTime>> getAttendance(String username, int day, int month, int year) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ATTENDANCE", null);
            if (tables.next()) {
                GregorianCalendar gc = new GregorianCalendar(year, month, day);
                gc.add(Calendar.DATE, 1);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT id, checkin, checkout FROM attendance WHERE username = '" + username + "' AND " +
                                "checkin >= timestamp '" + year + "-" + (month + 1) + "-" + day + " 00:00:00' AND " +
                                "checkin < timestamp '" + gc.get(Calendar.YEAR) + "-" + (gc.get(Calendar.MONTH) + 1) + "-" + gc.get(Calendar.DATE) + " 00:00:00' " +
                                "ORDER BY checkin ASC, checkout ASC;"
                );
                HashMap<Integer, ArrayList<LocalDateTime>> times = new HashMap<>();
                while (resultSet.next()) {
                    ArrayList<LocalDateTime> time = new ArrayList<>();
                    Integer id = resultSet.getInt("id");
                    Timestamp checkIn = resultSet.getTimestamp("checkin");
                    Timestamp checkOut = resultSet.getTimestamp("checkout");
                    if (checkIn != null) {
                        time.add(checkIn.toLocalDateTime());
                    } else {
                        time.add(null);
                    }
                    if (checkOut != null) {
                        time.add(checkOut.toLocalDateTime());
                    } else {
                        time.add(null);
                    }
                    times.put(id, time);
                }
                return times;
            }
            return null;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<LocalDateTime, HashMap<Integer, ArrayList<LocalDateTime>>> getAttendanceByMonth(String username, int month, int year) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "ATTENDANCE", null);
            if (tables.next()) {
                GregorianCalendar gc = new GregorianCalendar(year, month, 1);
                gc.add(Calendar.MONTH, 1);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT id, checkin, checkout FROM attendance WHERE username = '" + username + "' AND " +
                                "checkin >= timestamp '" + year + "-" + (month + 1) + "-01" + " 00:00:00' AND " +
                                "checkin < timestamp '" + gc.get(Calendar.YEAR) + "-" + (gc.get(Calendar.MONTH) + 1) + "-" + gc.get(Calendar.DATE) + " 00:00:00' " +
                                "ORDER BY checkin ASC, checkout ASC;"
                );
                HashMap<LocalDateTime, HashMap<Integer, ArrayList<LocalDateTime>>> monthlyEntries = new HashMap<>();
                while (resultSet.next()) {
                    HashMap<Integer, ArrayList<LocalDateTime>> entry = null;
                    ArrayList<LocalDateTime> time = new ArrayList<>();
                    Integer id = resultSet.getInt("id");
                    Timestamp checkIn = resultSet.getTimestamp("checkin");
                    Timestamp checkOut = resultSet.getTimestamp("checkout");
                    if (checkIn != null) {
                        time.add(checkIn.toLocalDateTime());
                    } else {
                        time.add(null);
                    }
                    if (checkOut != null) {
                        time.add(checkOut.toLocalDateTime());
                    } else {
                        time.add(null);
                    }
                    LocalDateTime dayKey = LocalDateTime.of(
                            checkIn.toLocalDateTime().getYear(),
                            checkIn.toLocalDateTime().getMonth(),
                            checkIn.toLocalDateTime().getDayOfMonth(),
                            0,
                            0
                    );
                    if (!monthlyEntries.containsKey(dayKey)) {
                        entry = new HashMap<>();
                        entry.put(id, time);
                        monthlyEntries.put(dayKey, entry);
                    } else {
                        entry = monthlyEntries.get(dayKey);
                        entry.put(id, time);
                    }
                }
                return monthlyEntries;
            }
            return null;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteTime(Integer id) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            Integer count = statement.executeUpdate(
                    "DELETE FROM attendance WHERE id=" + id
            );
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<ArrayList<String>> getUsers() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT username, firstName, lastName FROM attendanceusers ORDER BY firstName ASC, lastName ASC"
                );
                ArrayList<ArrayList<String>> users = new ArrayList<ArrayList<String>>();
                while (resultSet.next()) {
                    ArrayList<String> data = new ArrayList<>();
                    data.add(resultSet.getString("username"));
                    data.add(resultSet.getString("firstName"));
                    data.add(resultSet.getString("lastName"));
                    users.add(data);
                }
                return users;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean resetPassword(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String bcryptHash = BCrypt.withDefaults().hashToString(12, "Temp1234".toCharArray());
            Statement statement = connection.createStatement();
            Integer count = statement.executeUpdate(
                    "UPDATE attendanceusers SET password='" + bcryptHash + "' WHERE username='" + username + "'"
            );
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(String username, String password, String newPassword) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            if (checkPassword(username, password)) {
                String bcryptHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
                Statement statement = connection.createStatement();
                Integer count = statement.executeUpdate(
                        "UPDATE attendanceusers SET password='" + bcryptHash + "' WHERE username='" + username + "'"
                );
                return true;
            } else {
                return false;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT username FROM attendanceusers WHERE admin = true");
            String adminUsername = "";
            if (resultSet.next()) {
                adminUsername = resultSet.getString("username");
                if (username.equals(adminUsername)) {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery("SELECT COUNT(*) AS count FROM attendanceusers WHERE admin != true");
                    if (resultSet.next()) {
                        Integer count = resultSet.getInt("count");
                        if (count == 0) {
                            statement = connection.createStatement();
                            count = statement.executeUpdate(
                                    "DELETE FROM attendance WHERE username='" + username + "'"
                            );
                            statement = connection.createStatement();
                            count = statement.executeUpdate(
                                    "DELETE FROM attendanceusers WHERE username='" + username + "'"
                            );
                        } else {
                            return false;
                        }
                    }
                } else {
                    statement = connection.createStatement();
                    Integer count = statement.executeUpdate(
                            "DELETE FROM attendance WHERE username='" + username + "'"
                    );
                    statement = connection.createStatement();
                    count = statement.executeUpdate(
                            "DELETE FROM attendanceusers WHERE username='" + username + "'"
                    );
                }
                return true;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetData() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            Integer count = statement.executeUpdate(
                    "DROP TABLE attendance CASCADE"
            );
            statement = connection.createStatement();
            count = statement.executeUpdate(
                    "DROP TABLE attendanceusers CASCADE"
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFiles() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            Statement statement = connection.createStatement();
            Integer count = statement.executeUpdate(
                    "DROP ALL OBJECTS DELETE FILES"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        try  {
            Path configFilePath = Paths.get(configFileName);
            Files.deleteIfExists(configFilePath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}