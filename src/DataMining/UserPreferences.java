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
 * User: Maarten Date: 25-4-12 Time: 11:46
 */
public class UserPreferences {

	private int userId;
	private int[] itemIds;
	private double[] ratings;

	public UserPreferences(int id) {
		this.userId = id;
		this.itemIds = new int[0];
		this.ratings = new double[0];
	}

	public void addRatings(int newId, Double newRating) {
			// zoek de eerstvolgende logische locatie X
			int newIndex = Arrays.binarySearch(itemIds, newId);
						
			if (itemIds.length == 0){					// De eerste keer maken we gewoon een nieuwe array aan
				itemIds = new int[]{newId};
				ratings = new double[]{newRating};
			} else if (newIndex >= 0){ 	// Als de itemId al bestaat slaan we alleen de nieuwe rating op
				ratings[newIndex] = newRating;
			} else {									// Anders wordt een nieuw item tussen gevoegd
				
				newIndex *= -1;
				newIndex -= 1;
								
				// Kopie�n met extra ruimte
				int[] newItemIds = Arrays.copyOf(itemIds, itemIds.length+1);
				double[] newRatings = Arrays.copyOf(ratings, ratings.length+1);
				
				if (newItemIds.length > newIndex+1){	// Opschuiven, tenzij de nieuwe op het einde moet
					System.arraycopy(itemIds, newIndex, newItemIds, newIndex+1, newItemIds.length-newIndex-1);
					System.arraycopy(ratings, newIndex, newRatings, newIndex+1, newRatings.length-newIndex-1);					
				}
				
				// Beschikbaar gekomen ruimte vullen
				newItemIds[newIndex] = newId;
				newRatings[newIndex] = newRating;
				
				// nieuwe lijsten terugplaatsen
				itemIds = newItemIds;
				ratings = newRatings;
			}

	}
	
	public String toString(){
		String result = "";
		
		for (int i=0; i<itemIds.length; i++){
			result += itemIds[i] +" - "+ ratings[i] +"\n";
		}
		result += "\n";
		
		return result;
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

		DB db = m.getDB("mydb");

		UserPreferences uP = new UserPreferences(1);

		uP.addRatings(3, 4.0);
		System.out.print(uP.toString());

		uP.addRatings(5, 3.5);
		System.out.print(uP.toString());

		uP.addRatings(2, 1.0);
		System.out.print(uP.toString());

		uP.addRatings(3, 3.5);
		System.out.print(uP.toString());

		uP.addRatings(1, 5.0);
		System.out.print(uP.toString());

		uP.addRatings(4, 5.0);
		System.out.print(uP.toString());

	}

}
