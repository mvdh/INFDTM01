package DataMining;

import java.util.*;


public class ItemItem {


    /**
     * A TreeMap which stores an AvgRanking for each combination
     * of items. The lowest itemId of a pair is stored in the outermost
     * Treemap, so that there are no doubles (e.g. (101, 102) and (102, 101)
     */
    private TreeMap<Integer, TreeMap<Integer, AvgRanking>> avgRankings;

    /**
     * AllItemIds stores the itemIds of all items. A TreeSet is used to because
     * it simplified procedures like calculation the intersection of a bunch of
     * ids.
     */
    public TreeSet<Integer> allItemIds;

    /**
     * Constructor.
     */
    public ItemItem() {
        avgRankings = new TreeMap<Integer, TreeMap<Integer, AvgRanking>>();
        allItemIds = new TreeSet<Integer>();
    }

    /**
     * Constructor.
     * @param uPs
     */
    public ItemItem(TreeMap<Integer, UserPreferences> uPs) {
        avgRankings = new TreeMap<Integer, TreeMap<Integer, AvgRanking>>();
        allItemIds = new TreeSet<Integer>();

        //Build the slope-one table from UserPreferences uPs.
        buildSlopeOneTable(uPs);

        //Retrieve al itemIds
        for (UserPreferences uP : uPs.values()) {
            int[] itemIds = uP.getItemIds();
            for (int i : itemIds){
                allItemIds.add(i);
            }
        }
    }

    /**
     * Build a slopeOneTable.
     * @param uPs
     */
    private void buildSlopeOneTable(TreeMap<Integer, UserPreferences> uPs) {
        for (UserPreferences uP : uPs.values()) {
            addUserPreferences(uP);
        }
    }


    /**
     * Changes in a(n) (existing) UserPreference can be pushed to the slope-one table.
     * @param oldUP
     * @param newUP
     * @throws Exception
     */
    public void updateSlopeOneTable(UserPreferences oldUP, UserPreferences newUP) {
        if(oldUP.getUserId() == newUP.getUserId()) {
            removeUserPreferences(oldUP);
            addUserPreferences(newUP);
        }
    }

    /**
     * Adds a brand new UserPreferences to the slope-one table. That means
     * a whole new user. Changes in a UserPreferences (existing user) should
     * be handled by the method updateSlopeOneTable.
     * @param uP
     */
    public void addUserPreferences(UserPreferences uP) {
        int[] itemIds = uP.getItemIds();
        //omdat we een triangle table gebruiken ....
        for (int i = 0; i < itemIds.length-1; i++){
            if(!avgRankings.containsKey(itemIds[i])){
                avgRankings.put(itemIds[i], new TreeMap<Integer, AvgRanking>());
            }
            // .... vergelijken we alleen met de items die een hoger id hebben.
            for (int j = i+1; j < itemIds.length; j++) {
                if (!avgRankings.get(itemIds[i]).containsKey(itemIds[j])){
                    avgRankings.get(itemIds[i]).put(itemIds[j], new AvgRanking());
                }
                //sla de waarde op van het verschil in rating tussen beide items i en j.
                avgRankings.get(itemIds[i]).get(itemIds[j]).addRanking(uP.getRating(itemIds[j]) - uP.getRating(itemIds[i]));
            }
        }
    }

    /**
     * Removes a UserPreferences from the slope-one table.
     * @param uP
     */
    public void removeUserPreferences(UserPreferences uP) {
        int[] ids = uP.getItemIds();

        for (int i = 0; i < ids.length-1; i++){
            if(!avgRankings.containsKey(ids[i])){
                continue;
            }
            for (int j = i+1; j < ids.length; j++) {
                if (!avgRankings.get(ids[i]).containsKey(ids[j])){
                    continue;
                }

                //reduce totalrating and ratingcount.
                avgRankings.get(ids[i]).get(ids[j]).removeRanking(uP.getRating(ids[j]) - uP.getRating(ids[i]));

                //no user that has rated item i and item j together, so remove the entry.
                if(avgRankings.get(ids[i]).get(ids[j]).getCount() == 0) {
                    avgRankings.get(ids[i]).remove(ids[j]);
                }
            }

            //item i is not compareable, because it's not rated by a user who also
            //rated another item, so remove the entry.
            if(avgRankings.get(ids[i]).size() == 0) {
                avgRankings.remove(ids[i]);
            }
        }
    }


    public String getRecommendation(UserPreferences uP, int number) {
        TreeSet<Integer> ratedItems = new TreeSet<Integer>();
        for (int i : uP.getItemIds()) {
            ratedItems.add(i);
        }
        TreeSet<Integer> notRatedItems = new TreeSet<Integer>(allItemIds);
        notRatedItems.removeAll(ratedItems);

        LinkedHashMap<Integer,Double> predictedRatings = new LinkedHashMap<Integer, Double>();

//        for every item i the user u expresses no preference for
//            for every item j that user u expresses a preference for
//                find the average preference difference between j and i
//                add this diff to u’s preference value for j
//                add this to a running average

        for (int i : notRatedItems){

            double sum = 0.0;
            int count = 0;

            for (int j : ratedItems) {
                if(i < j) {
                    if (avgRankings.containsKey(i) && avgRankings.get(i).containsKey(j)){
                        sum += uP.getRating(j) + avgRankings.get(i).get(j).getAvgDiff();
                        count++;
                    }
                } else {
                    if (avgRankings.containsKey(j) && avgRankings.get(j).containsKey(i)) {
                        sum += uP.getRating(j) + avgRankings.get(j).get(i).getAvgDiff();
                        count++;
                    }
                }
            }

            if(count > 0) {
                predictedRatings.put(i, (sum/(double)count));
            }
        }

        // Reverse sort the final ratings
        predictedRatings = (LinkedHashMap<Integer, Double>) MapUtil.reverseSortByValue(predictedRatings);

        // Geef een mooie string terug van de top van de voorspelde waardes.
        String result = "";
        for (int i : predictedRatings.keySet()) {
            result += "id: " + i + ", voorspelde waarde: " + predictedRatings.get(i) + "\n";
            if(--number <= 0) {
                break;
            }
        }
        return result;
    }

    public void printMatrix() {
        for (int k : avgRankings.keySet()){
            System.out.println(k);
            for(int l : avgRankings.get(k).keySet()){
                AvgRanking r = avgRankings.get(k).get(l);
                System.out.print(k + " - " + l + ": ");
                System.out.println(r.getAvgDiff() + " (" + r.getSumRankings() + "/" + r.getCount() + ")");
            }
            System.out.println();
        }
    }
}
