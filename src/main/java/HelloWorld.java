import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class HelloWorld extends HttpServlet {
	private static final long serialVersionUID = 1980594051856701491L;
	static Connection connection;
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	
    	
		try {
		    Statement stmt = null;
		    ResultSet rs;
			stmt = connection.createStatement();
			//rs = stmt.executeQuery("SELECT id, title, xmlurl, htmlurl FROM feeds;");
			rs = stmt.executeQuery("SELECT id, feed_id, description, content, title, published, authorname, category, link_href FROM news;");
			
			resp.getWriter().println("<html><head><title>Мониторинг прессы АК</title><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /></head><body>Тра-ля-ля<br>");
			rs.next();
			while (rs.next()) {
				resp.getWriter().println("Чтение из БД: © " + rs.getString("published") +"  "+ rs.getString("title") + "<br>");
			}		
			resp.getWriter().println("</body></html>");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//resp.getWriter().println("<H1>Тра-ля-ля</H1>");
		System.out.println(System.getProperty("file.encoding"));
		System.out.println("!!! Тра-ля-ля © !!!");
    }

    public static void main(String[] args) throws Exception{
    	Class.forName("org.postgresql.Driver");
    	connection = getConnection();      
/*        Statement stmt = connection.createStatement();
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");

		ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
        while (rs.next()) {
            System.out.println("!!!!! Read from DB: " + rs.getTimestamp("tick"));
        }
*/		
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
