
package strategy;

import chainofresponsibility.PackageIssue;

public class LostPackageEstimationStrategy
        implements ResolutionEstimationStrategy {

    @Override
    public ResolutionEstimate estimate(PackageIssue issue) {
        double refund = issue.getPackageValue();

        return new ResolutionEstimate(
                4,
                refund,
                "Full package-value refund, subject to company responsibility and policy."
        );
    }
}
