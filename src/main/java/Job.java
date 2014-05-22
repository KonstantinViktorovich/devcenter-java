import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class Job {
	
	public static void main(String[] args) throws Exception, SQLException {
    	Class.forName("org.postgresql.Driver");
    	Connection connection = getConnection();      
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");

        System.out.println("!!!!! It works !!!!!");
	}

	private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath()+"";

        return DriverManager.getConnection(dbUrl, username, password);
    }
	
}
