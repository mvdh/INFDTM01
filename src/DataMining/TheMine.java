package DataMining;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;

// Imports for mongo
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

public class TheMine {

	static TreeMap<Integer, UserPreferences> userPrefs;
	static Random r;
	static Mongo con;
	static DB db;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Get database connection and database instance
		try {
			con = new Mongo( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = con.getDB( "infdtm" );
		
		
		userPrefs = new TreeMap<Integer, UserPreferences>();

		
		/*
		 * Old script to insert random ratings for random products by random users
		 */
		// r = new Random(1138);
		// for (int i = 0; i < 70; i++) {
		// addRating(r.nextInt(7) + 1, r.nextInt(10) + 1,
		// ((double) r.nextInt(11)) / 2);
		// }

		loadFile();

		for (UserPreferences uP : userPrefs.values()) {
			System.out.println(uP.toString());
		}

	}

	public static void addRating(int userId, int itemId, double rating) {
		if (!userPrefs.containsKey(userId)) {
			userPrefs.put(userId, new UserPreferences(userId));
		}
		UserPreferences uP = userPrefs.get(userId);
		uP.addRating(itemId, rating);
	}

	/*
	 * Loads the sample data file and passes it directly into the collection of ratings
	 */
	public static void loadFile() {

		FileInputStream fstream;
		try {
			fstream = new FileInputStream("u.data");

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(strLine, "	");
				int userId = Integer.parseInt((String) st.nextElement());
				int itemId = Integer.parseInt((String) st.nextElement());
				double rating = Double.parseDouble((String) st.nextElement());
				addRating(userId, itemId, rating);
			}
			// Close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Loads the sample data file and stores the data into a mongodb collection
	 */
	public static void loadFileIntoDatabase() {

		DBCollection coll = db.getCollection("movie_ratings");
		BasicDBObject doc;
		
		FileInputStream fstream;
		try {
			fstream = new FileInputStream("u.data");

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(strLine, "	");
				
				// Build new document
				doc = new BasicDBObject();
				doc.put("user_id", Integer.parseInt((String) st.nextElement()));
				doc.put("item_id", Integer.parseInt((String) st.nextElement()));
				doc.put("rating", Double.parseDouble((String) st.nextElement()));
				doc.put("date", new Date(Long.parseLong((String) st.nextElement()) * 1000));
				
				// Store new document
				coll.insert(doc);
			}
			// Close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
