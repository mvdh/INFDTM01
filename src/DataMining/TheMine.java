package DataMining;

import java.util.TreeMap;
import java.util.Random;

public class TheMine {
	
	static TreeMap<Integer, UserPreferences> userPrefs;
	static Random r;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		userPrefs = new TreeMap<Integer, UserPreferences>();
		r = new Random(1138);
		
		for (int i=1; i<=7; i++){
			userPrefs.put(i, new UserPreferences(i));
		}
		
		for (int i=0; i<70; i++){
			userPrefs.get(r.nextInt(7)+1).addRatings(r.nextInt(10)+1, ((double)r.nextInt(11))/2);
		}
		
		for (UserPreferences uP : userPrefs.values()){
			System.out.println(uP.toString());
		}


	}

}
