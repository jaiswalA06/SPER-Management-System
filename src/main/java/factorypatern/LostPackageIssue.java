package factorypatern;

public class LostPackageIssue implements PackageIssue {

    @Override
    public void displayIssue() {
        System.out.println("Lost Package Issue created.");
    }
}