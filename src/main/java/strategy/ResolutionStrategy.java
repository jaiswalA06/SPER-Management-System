
package strategy;

import chainofresponsibility.PackageIssue;

public interface ResolutionStrategy {

    ResolutionPlan createPlan(PackageIssue issue);
}
