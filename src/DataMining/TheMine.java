package DataMining;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class TheMine {

	static TreeMap<Integer, UserPreferences> userPrefs;
	static Random r;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		userPrefs = new TreeMap<Integer, UserPreferences>();

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

}
