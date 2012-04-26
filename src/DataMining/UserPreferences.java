package DataMining;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

/**
 * User: Maarten
 * Date: 25-4-12
 * Time: 11:46
 */
public class UserPreferences {

    private int userId;
    private int[] itemIds;
    private double[] ratings;

    public UserPreferences(int id){
        this.userId = id;
        this.itemIds = new int[0];
        this.ratings = new double[0];
    }

    public void addRatings(Map<Integer, Double> prefs)
    {
        int nextPos = itemIds.length;
        int[] new_itemIds = Arrays.copyOf(itemIds, (nextPos+prefs.size()));
        double[] new_ratings = Arrays.copyOf(ratings, (nextPos+prefs.size()));

        for (Map.Entry<Integer, Double> entry : prefs.entrySet())
        {
            new_itemIds[nextPos] = entry.getKey();
            new_ratings[nextPos++] = entry.getValue();
        }

        itemIds = new_itemIds;
        ratings = new_ratings;
    }


    /**
     *
     */
    public static void main(String[] args) {
    	
    	Mongo m = null;
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	DB db = m.getDB( "mydb" );
    	
    		
        UserPreferences uP = new UserPreferences(1);
        HashMap<Integer, Double> prefs = new HashMap<Integer, Double>();
        prefs.put(1, 2.0);
        prefs.put(2, 3.0);
        prefs.put(3, 9.0);
        prefs.put(4, 7.0);
        prefs.put(5, 1.0);
        prefs.put(6, 6.0);


        uP.addRatings(prefs);
        for (double rating : uP.ratings) {
            System.out.println(rating);
        }

        System.out.print("\n");

        uP.addRatings(prefs);
        for (double rating : uP.ratings) {
            System.out.println(rating);
        }

        System.out.print("\n");

        for (int id : uP.itemIds) {
            System.out.println(id);
        }
    }

}
