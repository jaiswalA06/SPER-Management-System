
package strategy;

import chainofresponsibility.PackageIssue;

public final class ResolutionPriority {

    private ResolutionPriority() {
    }

    public static String calculate(PackageIssue issue) {
        String severity = issue.getSeverity();

        if ("CRITICAL".equalsIgnoreCase(severity)
                || issue.getPackageValue() > 50000) {
            return "CRITICAL";
        }

        if ("HIGH".equalsIgnoreCase(severity)
                || issue.getPackageValue() > 25000) {
            return "HIGH";
        }

        if ("MEDIUM".equalsIgnoreCase(severity)
                || issue.getPackageValue() > 5000) {
            return "MEDIUM";
        }

        return "LOW";
    }
}
