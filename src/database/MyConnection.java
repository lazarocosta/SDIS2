package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    public static Connection createConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager
                .getConnection("jdbc:postgresql://telmo20.ddns.net:5432/sdis",
                        "javaApp", "p5+Z`Hj;(qhV<eP;");

        c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        System.out.println("Opened database successfully");
        return c;
    }
}
