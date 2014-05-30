import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.impl.DateParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HelloWorld extends HttpServlet {
	private static final long serialVersionUID = 1980594051856701491L;
	static Connection connection;
	
    @SuppressWarnings("unchecked")
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

    	SyndFeed feed = new SyndFeedImpl();
    	String feedType = "rss_2.0";
    	feed.setFeedType(feedType);
    	feed.setTitle("Мониторинг СМИ АК и РА");
    	feed.setLink("http://localhost:5000");
    	feed.setDescription("This feed has been created using ROME (Java syndication utilities");
   
        @SuppressWarnings("rawtypes")
		List entries = new ArrayList();
        SyndEntry entry;
        SyndContent description;	
       
		try {
		    Statement stmt = null;
		    ResultSet rs;
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT id, feed_id, description, content, title, published, authorname, category, link_href FROM news;");
			
			//resp.getWriter().println("<html><head><title>Мониторинг прессы АК</title><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" /></head><body>Тра-ля-ля<br>");
			rs.next();
			while (rs.next()) {
				
		        entry = new SyndEntryImpl();
		        entry.setTitle("title");
		        entry.setLink(rs.getString("link_href"));
		        entry.setPublishedDate(DateParser.parseDate(rs.getString("published")));
		        description = new SyndContentImpl();
		        description.setType("text/plain");
		        description.setValue("description");
		        entry.setDescription(description);
		        entries.add(entry);
				
				//resp.getWriter().println("Чтение из БД: © " + rs.getString("published") +"  "+ rs.getString("title")+"  "+ rs.getString("link_href") + "<br>");
			}		
			//resp.getWriter().println("</body></html>");
	        feed.setEntries(entries);
	        
	        StringWriter writer = new StringWriter();
	        SyndFeedOutput output = new SyndFeedOutput();
	        try {
				output.output(feed,writer);
			} catch (FeedException e1) {
				e1.printStackTrace();
			}
	        resp.getWriter().println(writer.toString());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//resp.getWriter().println("<H1>Тра-ля-ля</H1>");
		//System.out.println(System.getProperty("file.encoding"));
		//System.out.println("!!! Тра-ля-ля © !!!");
    }

    public static void main(String[] args) throws Exception{
    	Class.forName("org.postgresql.Driver");
    	connection = getConnection();      

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
