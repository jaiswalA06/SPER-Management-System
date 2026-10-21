
package strategy;

public class ResolutionEstimate {
    private final int estimatedDays;
    private final double estimatedRefund;
    private final String reason;

    public ResolutionEstimate(int estimatedDays, double estimatedRefund, String reason) {
        this.estimatedDays = estimatedDays;
        this.estimatedRefund = estimatedRefund;
        this.reason = reason;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    public double getEstimatedRefund() {
        return estimatedRefund;
    }

    // Retained for compatibility with older code.
    public double getEstimatedCost() {
        return estimatedRefund;
    }

    public String getReason() {
        return reason;
    }
}
