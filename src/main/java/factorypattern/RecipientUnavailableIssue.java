package factorypattern;

public class RecipientUnavailableIssue implements PackageIssue {

    @Override
    public void displayIssue() {
        System.out.println("Recipient Unavailable Issue created.");
    }
}