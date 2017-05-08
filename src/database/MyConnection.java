package database;
import java.sql.Connection;
import java.sql.DriverManager;

public class MyConnection {
	
   public static Connection createConnection() {
      Connection c = null;
      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager
            .getConnection("jdbc:postgresql://telmo20.ddns.net:5432/sdis",
            "javaApp", "p5+Z`Hj;(qhV<eP;");
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      System.out.println("Opened database successfully");
      return c;
   }
}
