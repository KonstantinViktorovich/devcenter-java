import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;


public class Job {
	
	
	public static void main(String[] args) throws Exception, SQLException {
    	Class.forName("org.postgresql.Driver");
    	Connection connection = getConnection();      
        Statement feedstmt = connection.createStatement();
        Statement newsstmt = connection.createStatement();

        ResultSet rs = feedstmt.executeQuery("SELECT id, title, xmlurl, htmlurl FROM feeds;");
        URL url = null;
        Calendar c = Calendar.getInstance();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        rs.next();
		while (rs.next()) {
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
	 
        
	        while (itEntries.hasNext()) {
	            SyndEntry entry = (SyndEntry) itEntries.next();
	            c.setTime(entry.getPublishedDate());

	            newsstmt.executeUpdate("INSERT INTO news(feed_id, description, content, title, published, authorname, category, link_href) VALUES " 
	            		+ "("+rs.getString("id")
	            		+ ", '" + entry.getDescription().getValue()
	            		+ "', '" + entry.getContents()
	            		+ "', '"  + entry.getTitle()
	            		+ "', '" + c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH)
	            		+ " "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)
	            		+ "', '"
	            		+ entry.getAuthor()
	            		+ "', '" + entry.getCategories()
	            		+ "', '" + entry.getLink()+"');");
	        }
			
		}	

	}

	private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath()+"";

        return DriverManager.getConnection(dbUrl, username, password);
    }


	
}
