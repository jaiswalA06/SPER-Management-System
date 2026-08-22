package factorypatern;

public class DelayedPackageIssue implements PackageIssue {

    @Override
    public void displayIssue() {
        System.out.println("Delayed Package Issue created.");
    }
}