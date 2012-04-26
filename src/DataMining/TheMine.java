package DataMining;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class TheMine {

	/**
	 * @param args
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
