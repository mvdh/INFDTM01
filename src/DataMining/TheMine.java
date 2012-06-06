package DataMining;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
//		if (db.getCollection("test_data").count() == 0)
//			loadFileIntoDatabase("testdata.txt", "test_data");
//
//		// Load data from database into userPrefs
//		loadFromDatabase("test_data");

		// Print all ratings for all users
//		for (UserPreferences uP : userPrefs.values()) {
//			System.out.println(uP.toString());
//		}
		

	
		/*
		 * Load and/or print movie data
		 */
		
//		userPrefs = new TreeMap<Integer, UserPreferences>();
//		
//		// Import the test data
//		if (db.getCollection("movie_ratings").count() == 0)
//			loadFileIntoDatabase("u.data", "movie_ratings");
//
		// Load data from database into userPrefs
		loadFromDatabase("movie_ratings", 10000);

//		// Print all ratings for all users
//		for (UserPreferences uP : userPrefs.values()) {
//			System.out.println(uP.toString());
//		}

//		System.out.println(getRecommendation(712, 5));


//		loadFromDatabase("movie_ratings");

//		System.out.println(getRecommendation(712, 5));

        ItemItem ii = new ItemItem(userPrefs);
        ii.getRecommendation(userPrefs.get(712));
//        ii.printMatrix();
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

    public static String getRecommendation(int userId, int numberOfResults){

        HashMap<Integer, Double[]> items = new HashMap<Integer, Double[]>();	// Here we will store a list possible items to recommend
        String result = "";

        UserPreferences targetP = userPrefs.get(userId);

        int[] targetUserItemIds = targetP.getItemIds();

        for (UserPreferences comparedP : userPrefs.values()){

            // No need to compared the target user to himself
            if (comparedP.getUserId() == userId){
                continue;
            }

            // determine what items have been rated by both users,
            // and what items have only been rated by the compared user
            int[] comparedUserItemIds = comparedP.getItemIds();
            int[] intersection = new int[0];					// items rated by both users
            int[] relativeComplement = new int[0];		// items only rated by compared user

            for (int id : comparedUserItemIds){
                if (Arrays.binarySearch(targetUserItemIds, id) >= 0){
                    intersection = Arrays.copyOf(intersection, intersection.length+1);
                    intersection[intersection.length-1] = id;
                } else {
                    relativeComplement = Arrays.copyOf(relativeComplement, relativeComplement.length+1);
                    relativeComplement[relativeComplement.length-1] = id;
                }
            }



            // skip a user that will yield no recommendations
            if ((intersection.length == 0) || (relativeComplement.length == 0)){
                continue;
            }

            // print the intersection between both users
//            result += comparedP.getUserId()+": ";
//            for (int rating : intersection){
//                result += rating+" ";
//            }
//            result += "\n";

            // find ratings from intersecting items
            double[] targetRatings = new double[intersection.length];
            double[] comparedRatings = new double[intersection.length];

            for (int i=0; i < intersection.length; i++){
                targetRatings[i] = targetP.getRating(intersection[i]);		// append and add to target user
                comparedRatings[i] = comparedP.getRating(intersection[i]);	// append and add to compared user
            }

            // calculate Pearson's correlation between target user and compared user
            double pearsonsCorrelation = pearsonCorrelationCoefficient(targetRatings, comparedRatings);

            // Skip where Pearson's correlation will render NaN
            if (pearsonsCorrelation == 2){
//                result += "\n";
                continue;
            }

//            result += "Pearson's correlation: "+pearsonsCorrelation+"\n";

            // for each item in the relative complement, add correlation and weighted rating
            for (int itemId : relativeComplement){
                if (items.containsKey(itemId)){
                    Double[] newValues = items.get(itemId);
                    newValues[0] += pearsonsCorrelation;
                    newValues[1] += pearsonsCorrelation*comparedP.getRating(itemId);
                    items.put(itemId, newValues);
                } else {
                    items.put(itemId, new Double[]{pearsonsCorrelation,pearsonsCorrelation*comparedP.getRating(itemId)});
                }
            }
//            result += "\n";

        }

        // Calculate final ratings
        LinkedHashMap<Integer, Double> recommendations = new LinkedHashMap<Integer, Double>();
        for (Integer key : items.keySet()){
            Double[] oldValues = items.get(key);

            // Skip if new rating would render NaN
            if ((oldValues[0] == 0) && (oldValues[1] == 0)){
                continue;
            }


            Double newValue = oldValues[1]/oldValues[0];
            recommendations.put(key, newValue);
        }

        // Reverse sort the final ratings
        recommendations = (LinkedHashMap<Integer, Double>) MapUtil.reverseSortByValue(recommendations);

        int i = 1;
        for (Integer itemId : recommendations.keySet()){
            result += itemId+": "+recommendations.get(itemId)+"\n";
            if ((i+=1) > numberOfResults){
                break;
            }
        }



        result += "\n\n";

        return result;
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
	 * Loads the sample data from database and passes it directly into the collection of ratings
	 */
	public static void loadFromDatabase(String collection, int count) {

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
		DBCursor cur = coll.find(new BasicDBObject(), subset).sort(new BasicDBObject("date", 1)).limit(count);
		
        while(cur.hasNext()) {
        	doc = cur.next();
        	int userId = (Integer)doc.get("user_id");
        	int itemId = (Integer)doc.get("item_id");
        	double rating = (Double)doc.get("rating");
        	addRating(userId, itemId, rating);
        }
	}	
	
    public static void getItemItemRecommendation(int userId, int itemId, int numberOfResults) {
        //load userPrefs

        //complete matrix of all users against all items
            //get all userids
            //get all itemids

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
				StringTokenizer st = new StringTokenizer(strLine, "\t::");
				
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

    public static double pearsonCorrelationCoefficient(double[] x, double[] y) {
        double xAvg = 0.0;
        double yAvg = 0.0;
        for (int i = 0; i < x.length; i++) {
            xAvg += x[i];
            yAvg += y[i];
        }
        xAvg /= x.length;
        yAvg /= x.length;


        double numerator = 0.0;
        double denominator;
        double denomPartX = 0.0;
        double denomPartY = 0.0;
        for (int i = 0; i < x.length; i++){
            numerator += (x[i]-xAvg) * (y[i]-yAvg);
            denomPartX += Math.pow(x[i]-xAvg, 2.0);
            denomPartY += Math.pow(y[i]-yAvg, 2.0);
        }
        denominator = Math.sqrt(denomPartX * denomPartY);


        if (denominator == 0){
            return 2;
        }

        return numerator/denominator;
    }





}