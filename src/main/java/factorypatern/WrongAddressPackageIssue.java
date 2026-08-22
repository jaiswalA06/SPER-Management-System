package factorypatern;

public class WrongAddressPackageIssue implements PackageIssue {

    @Override
    public void displayIssue() {
        System.out.println("Wrong Address Package Issue created.");
    }
}