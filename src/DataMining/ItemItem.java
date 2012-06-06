package DataMining;

import java.util.*;

/**
 *
 */
public class ItemItem {


    private TreeMap<Integer, TreeMap<Integer, AvgRanking>> avgRankings;
    private TreeSet<Integer> allItemIds;

    public ItemItem() {
        avgRankings = new TreeMap<Integer, TreeMap<Integer, AvgRanking>>();
        allItemIds = new TreeSet<Integer>();
    }

    public ItemItem(TreeMap<Integer, UserPreferences> uPs) {
        avgRankings = new TreeMap<Integer, TreeMap<Integer, AvgRanking>>();
        allItemIds = new TreeSet<Integer>();
        buildMatrix(uPs);

        for (UserPreferences uP : uPs.values()) {
            int[] itemIds = uP.getItemIds();
            for (int i : itemIds){
                allItemIds.add(i);
            }
        }
    }

    private void buildMatrix(TreeMap<Integer, UserPreferences> uPs) {
        //voor iedere gebruiker
        for (UserPreferences uP : uPs.values()) {
            int[] itemIds = uP.getItemIds();
            //omdat we een triangle matrix gebruiken ....
            for (int i = 0; i < itemIds.length-1; i++){
                if(!avgRankings.containsKey(itemIds[i])){
                    avgRankings.put(itemIds[i], new TreeMap<Integer, AvgRanking>());
                }
                TreeMap<Integer, AvgRanking> r = avgRankings.get(itemIds[i]);
                // .... vergelijken we alleen met de items die een hoger id hebben.
                for (int j = i+1; j < itemIds.length; j++) {
                    if (!r.containsKey(itemIds[j])){
                        r.put(itemIds[j], new AvgRanking());
                    }
                    r.get(itemIds[j]).addRanking(uP.getRating(itemIds[j]) - uP.getRating(itemIds[i]));
                }
            }
        }
    }

    public void getRecommendation(UserPreferences uP) {
        TreeSet<Integer> ratedItems = new TreeSet<Integer>();
        for (int i : uP.getItemIds()) {
            ratedItems.add(i);
        }
        TreeSet<Integer> notRatedItems = new TreeSet<Integer>(allItemIds);
        notRatedItems.removeAll(ratedItems);

        LinkedHashMap<Integer,Double> predictedRatings = new LinkedHashMap<Integer, Double>();

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

        for (int i : predictedRatings.keySet()) {
            System.out.println("id: " + i + ", voorspelde rating: " + predictedRatings.get(i));
        }
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
