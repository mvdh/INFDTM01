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
    private double avgDiff;

    public AvgRanking() {
        count = 0;
        sumRankings = 0;
        avgDiff = 0;
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
        this.avgDiff = this.sumRankings / (double) this.count;
    }

    public double getAvgDiff() {
        return this.avgDiff;
    }
}
