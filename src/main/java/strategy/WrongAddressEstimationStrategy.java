
package strategy;

import chainofresponsibility.PackageIssue;

public class WrongAddressEstimationStrategy
        implements ResolutionEstimationStrategy {

    @Override
    public ResolutionEstimate estimate(PackageIssue issue) {
        double refund = issue.getPackageValue() * 0.10;

        return new ResolutionEstimate(
                2,
                refund,
                "Illustrative compensation if the address error was the company's responsibility."
        );
    }
}
