package DataMining;

public class ScaledUserPreferences extends UserPreferences {

	private double pearsonsCorrelation;
	
	public ScaledUserPreferences(int id, double pearsonsCorrelation) {
		super(id);
		this.pearsonsCorrelation = pearsonsCorrelation;
	}

}
