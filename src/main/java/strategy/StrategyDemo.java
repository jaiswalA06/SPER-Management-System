
package strategy;

import chainofresponsibility.PackageIssue;

public class StrategyDemo {

    public static void main(String[] args) {

        PackageIssue issue = new PackageIssue(
                101,
                "SPER123456",
                "LOST",
                "Package has not been located",
                "HIGH",
                12000.00
        );

        ResolutionEstimator estimator = new ResolutionEstimator();

        ResolutionEstimate estimate = estimator.estimate(issue);

        System.out.println("========================================");
        System.out.println("       SPER COMPENSATION ESTIMATOR");
        System.out.println("========================================");

        System.out.println("Issue ID          : " + issue.getIssueId());
        System.out.println("Tracking Number   : " + issue.getTrackingNumber());
        System.out.println("Issue Type        : " + issue.getIssueType());
        System.out.printf("Package Value     : Rs. %.2f%n",
                issue.getPackageValue());

        System.out.println("----------------------------------------");

        System.out.println("Selected Strategy : "
                + estimator.getSelectedStrategyName(issue));

        System.out.println("Estimated Resolution Time : "
                + estimate.getEstimatedDays() + " days");

        System.out.printf("Estimated Customer Refund : Rs. %.2f%n",
                estimate.getEstimatedRefund());

        System.out.println("Reason            : " + estimate.getReason());

        System.out.println("----------------------------------------");
        System.out.println("Note: Estimates use illustrative project rules.");
        System.out.println("Final compensation requires policy verification.");
        System.out.println("========================================");
    }
}
