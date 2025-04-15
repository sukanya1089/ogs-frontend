package app.ogs.model;


import java.util.StringJoiner;

public class Statistics {

	    private double averageWaterContent;
	    private int samplesAboveUnitWeightThreshold;
	    private int samplesAboveWaterContentThreshold;
	    private int samplesAboveShearStrengthThreshold;

    public Statistics() {}

    public double getAverageWaterContent() {
        return averageWaterContent;
    }

    public void setAverageWaterContent(double averageWaterContent) {
        this.averageWaterContent = averageWaterContent;
    }

    public int getSamplesAboveUnitWeightThreshold() {
        return samplesAboveUnitWeightThreshold;
    }

    public void setSamplesAboveUnitWeightThreshold(int samplesAboveUnitWeightThreshold) {
        this.samplesAboveUnitWeightThreshold = samplesAboveUnitWeightThreshold;
    }

    public int getSamplesAboveWaterContentThreshold() {
        return samplesAboveWaterContentThreshold;
    }

    public void setSamplesAboveWaterContentThreshold(int samplesAboveWaterContentThreshold) {
        this.samplesAboveWaterContentThreshold = samplesAboveWaterContentThreshold;
    }

    public int getSamplesAboveShearStrengthThreshold() {
        return samplesAboveShearStrengthThreshold;
    }

    public void setSamplesAboveShearStrengthThreshold(int samplesAboveShearStrengthThreshold) {
        this.samplesAboveShearStrengthThreshold = samplesAboveShearStrengthThreshold;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Statistics.class.getSimpleName() + "[", "]")
                .add("averageWaterContent=" + averageWaterContent)
                .add("samplesAboveUnitWeightThreshold=" + samplesAboveUnitWeightThreshold)
                .add("samplesAboveWaterContentThreshold=" + samplesAboveWaterContentThreshold)
                .add("samplesAboveShearStrengthThreshold=" + samplesAboveShearStrengthThreshold)
                .toString();
    }
}
