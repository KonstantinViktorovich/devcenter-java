import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class HelloWorld extends HttpServlet {
	static Connection connection;
	

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

		try {
		    Statement stmt = null;
		    ResultSet rs;
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT tick FROM ticks");
			rs.next();
			//while (rs.next()) {
				resp.getWriter().println("Read from DB: " + rs.getTimestamp("tick"));
			//}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) throws Exception{
    	Class.forName("org.postgresql.Driver");
    	connection = getConnection();      
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");

		ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
        while (rs.next()) {
            System.out.println("Read from DB: " + rs.getTimestamp("tick"));
        }
		
	    Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new HelloWorld()),"/*");
        server.start();
        server.join();   
    }
	
	private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath()+"";
    	/*
    	String dbUrl = "jdbc:postgresql://127.0.0.1:5432/testdb";
    	String username = "postgres";
    	String password = "www";*/
        return DriverManager.getConnection(dbUrl, username, password);
    }
	
}
