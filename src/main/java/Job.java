import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;


public class Job {
	
	
	public static void main(String[] args) throws Exception, SQLException {
    	Class.forName("org.postgresql.Driver");
    	Connection connection = getConnection();      
        Statement feedstmt = connection.createStatement();
        PreparedStatement newsstmt = connection.prepareStatement("DROP TABLE news;"
+ "CREATE TABLE news(id serial NOT NULL, feed_id integer, description character varying, content character varying, title character varying, published timestamp without time zone, authorname character varying, category character varying, link_href character varying, CONSTRAINT news_pkey PRIMARY KEY (id));");
        newsstmt.executeUpdate();
        
        ResultSet rs = feedstmt.executeQuery("SELECT id, title, xmlurl, htmlurl FROM feeds;");
        URL url = null;
        Calendar c = Calendar.getInstance();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        rs.next();
		//while (rs.next()) {
			url = new URL(rs.getString("xmlurl"));
			HttpGet pageGet = new HttpGet(url.toURI());
			HttpResponse response = httpClient.execute(pageGet);
			
	        SyndFeedInput input = new SyndFeedInput();
	        SyndFeed feed = null;
			try {
				feed = input.build(new XmlReader(response.getEntity().getContent()));
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			@SuppressWarnings("unchecked")
	        List<SyndFeed> entries = feed.getEntries();
	        Iterator<SyndFeed> itEntries = entries.iterator();
	 
        
	        newsstmt = connection.prepareStatement("INSERT INTO news(feed_id, description, content, title, published, authorname, category, link_href) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
	        while (itEntries.hasNext()) {
	            SyndEntry entry = (SyndEntry) itEntries.next();


	            newsstmt.setInt(1, rs.getInt("id"));
	            newsstmt.setString(2, entry.getDescription().getValue());
	            newsstmt.setString(3, entry.getContents().toString());
	            newsstmt.setString(4, entry.getTitle());
	            c.setTime(entry.getPublishedDate());
	            newsstmt.setDate(5, new java.sql.Date(c.getTimeInMillis()));
	            newsstmt.setString(6, entry.getAuthor());
	            newsstmt.setString(7, entry.getCategories().toString());
	            newsstmt.setString(8, entry.getLink());

	            newsstmt.executeUpdate();
	        }
			
		}	

	//}

	private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath()+"";

        return DriverManager.getConnection(dbUrl, username, password);
    }


	
}
