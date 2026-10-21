
package strategy;

import chainofresponsibility.PackageIssue;

public interface ResolutionEstimationStrategy {
    ResolutionEstimate estimate(PackageIssue issue);
}
