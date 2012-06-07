package DataMining;

/**
 * Created with IntelliJ IDEA.
 * User: Maarten
 * Date: 6-6-12
 * Time: 18:24
 */
public class AvgRanking {

    private int count;
    private double sumRankings;

    public AvgRanking() {
        count = 0;
        sumRankings = 0;
    }

    public int getCount() {
        return count;
    }

    public double getSumRankings() {
        return sumRankings;
    }

    public void addRanking(double r) {
        this.sumRankings += r;
        this.count++;
    }

    public void removeRanking(double r) {
        this.sumRankings -= r;
        this.count--;
    }

    public double getAvgDiff() {
        return this.sumRankings / (double) this.count;
    }
}
