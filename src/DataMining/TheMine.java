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
		
		connect();		
		
		
		/*
		 * Load and/or print test data
		 */
		
		userPrefs = new TreeMap<Integer, UserPreferences>();
		
		// Import the test data
		if (db.getCollection("test_data").count() == 0)
			loadFileIntoDatabase("testdata.txt", "test_data");

		// Load data from database into userPrefs
		loadFromDatabase("test_data");

		// Print all ratings for all users
		for (UserPreferences uP : userPrefs.values()) {
			System.out.println(uP.toString());
		}

	
		/*
		 * Load and/or print movie data
		 */
		
		userPrefs = new TreeMap<Integer, UserPreferences>();
		
		// Import the test data
		if (db.getCollection("movie_ratings").count() == 0)
			loadFileIntoDatabase("u.data", "movie_ratings");

		// Load data from database into userPrefs
		loadFromDatabase("movie_ratings");

		// Print all ratings for all users
		for (UserPreferences uP : userPrefs.values()) {
			System.out.println(uP.toString());
		}

				
		// Close connection to database
		con.close();

	}

	public static void addRating(int userId, int itemId, double rating) {
		if (!userPrefs.containsKey(userId)) {
			userPrefs.put(userId, new UserPreferences(userId));
		}
		UserPreferences uP = userPrefs.get(userId);
		uP.addRating(itemId, rating);
	}

	
	/*
	 * Loads the sample data from database and passes it directly into the collection of ratings
	 */
	public static void loadFromDatabase(String collection) {

		DBObject doc;
		DBCollection coll = db.getCollection(collection);

		// Build a filter for fields to limit the amount of data sent back from DB
		BasicDBObject subset = new BasicDBObject();
		subset.put("user_id", 1);
		subset.put("item_id", 1);
		subset.put("rating", 1);
		subset.put("_id", 0);
		
		// First argument is an empty document to gain access to the second argument
		// The sort is applied in case a movie has been rated twice by the same user 
		DBCursor cur = coll.find(new BasicDBObject(), subset).sort(new BasicDBObject("date", 1));
		
        while(cur.hasNext()) {
        	doc = cur.next();
        	int userId = (Integer)doc.get("user_id");
        	int itemId = (Integer)doc.get("item_id");
        	double rating = (Double)doc.get("rating");
        	addRating(userId, itemId, rating);
        }
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
	public static void loadFileIntoDatabase(String filename, String collection) {

		DBCollection coll = db.getCollection(collection);
		BasicDBObject doc;
		
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filename);

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
				if (st.hasMoreElements()){
					doc.put("date", new Date(Long.parseLong((String) st.nextElement()) * 1000));
				}
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
	
	/*
	 * Old script to insert random ratings for random products by random users
	 */
	public static void addRandom(){
		addRandom(1138);
	}
	
	public static void addRandom(int seed){
		r = new Random(seed);
		for (int i = 0; i < 70; i++) {
			addRating(r.nextInt(7) + 1, r.nextInt(10) + 1, ((double) r.nextInt(11)) / 2);
		}		
	}

	public static void connect(){
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
	}

}
