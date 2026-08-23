package chainofresponsibility;

public class PackageIssue {

    private int issueId;
    private String trackingNumber;
    private String issueType;
    private String description;
    private String severity;
    private double packageValue;
    private String status;

    public PackageIssue(int issueId,String trackingNumber,String issueType,String description,String severity,double packageValue) {
        this.issueId = issueId;
        this.trackingNumber = trackingNumber;
        this.issueType = issueType;
        this.description = description;
        this.severity = severity;
        this.packageValue = packageValue;
        this.status = "REPORTED";
    }

    public int getIssueId() {
        return issueId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public double getPackageValue() {
        return packageValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}