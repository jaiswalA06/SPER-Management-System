package factorypatern;

public class WrongPackageIssue implements PackageIssue {

    @Override
    public void displayIssue() {
        System.out.println("Wrong Package Issue created.");
    }
}