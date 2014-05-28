import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
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
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
        //stmt.executeUpdate("INSERT INTO feeds(title, xmlUrl, htmlUrl)VALUES ('����', 'http://www.bankfax.ru/rss.xml', 'http://www.bankfax.ru/');");
        //System.out.println("!!!!! It works !!!!!");
        rss2db(stmt);
	}

	private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath()+"";

        return DriverManager.getConnection(dbUrl, username, password);
    }
	
	private static void rss2db(Statement stmt) throws Exception {

			//URL url = new URL("http://amic.ru/rss/");
	        //URL url = new URL("http://feeds.feedburner.com/altapress?format=xml");
	        URL url = new URL("http://www.bankfax.ru/rss.xml");
			//URL url = new URL("http://www.asfera.info/rss/");
			//URL url = new URL("http://listock.ru/index.php?format=feed&type=rss&amp;Itemid=96");
			//URL url = new URL("http://politsib.ru/rss");

			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet pageGet = new HttpGet(url.toURI());
			HttpResponse response = httpClient.execute(pageGet);

	        SyndFeedInput input = new SyndFeedInput();
	        SyndFeed feed = null;
			try {
				feed = input.build(new XmlReader(response.getEntity().getContent()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (FeedException e) {
				e.printStackTrace();
			      LineNumberReader r;
				try {
					r = new LineNumberReader(new
					    InputStreamReader(response.getEntity().getContent()));
			        //InputStreamReader(url.openStream()));
			        String s = r.readLine();
			        while (s!=null) {
			          System.out.println(s);
			          s = r.readLine();
			          }
			        //System.out.println(r.getLineNumber());
			        r.close();
					} catch (IllegalStateException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			} catch (IOException e) {
				e.printStackTrace();
			}
	        List entries = feed.getEntries();
	        Iterator itEntries = entries.iterator();
	 
	        Calendar c = Calendar.getInstance();
	        	        
	        while (itEntries.hasNext()) {
	            SyndEntry entry = (SyndEntry) itEntries.next();
	            c.setTime(entry.getPublishedDate());
	            System.out.println("<<Title:>> " + entry.getTitle());
	            System.out.println("<<Link:>> " + entry.getLink());
	            System.out.println("<<Author:>> " + entry.getAuthor()); 
	            System.out.println("<<Publish Date:>> " + c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH));
	            System.out.println("<<Description:>> " + entry.getDescription().getValue());
	            System.out.println();
	        
	            stmt.executeUpdate("INSERT INTO news(feed_id, description, content, title, published, authorname, category, link_href) VALUES " 
	            		+ "(1, '" + entry.getDescription().getValue()
	            		+ "', '" + entry.getContents()
	            		+"', '"  + entry.getTitle()
	            		+ "', '" + c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH)
	            		+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)
	            		+ "', '"
	            		+ entry.getAuthor()
	            		+ "', '" + entry.getCategories()
	            		+ "', '" + entry.getLink()+"');");
	        }
	        
		}

	
}
