package DataMining;

import java.util.TreeMap;

/**
 * User: Maarten
 * Date: 25-4-12
 * Time: 11:46
 */
public class UserPreferences {

    int userId;
    TreeMap<Integer, Double> ratings; //K = item id, V = rate

    public UserPreferences(){
        this.ratings = new TreeMap<Integer, Double>();
        this.userId = -1;
    }

    /**
     * Adds or updates a rating
     */
    public void addRating(int itemId, double rate) {
        ratings.put(itemId, rate);
    }

    /**
     *
     */
    public static void main(String[] args) {
        UserPreferences uP = new UserPreferences();
        uP.addRating(1, 2.4);
        System.out.println(uP.ratings.get(1));
    }

}
