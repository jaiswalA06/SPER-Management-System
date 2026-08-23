import java.util.Scanner;
import enums.IssueType;
import factorypatern.ConcretePackageIssueFactory;
import factorypatern.PackageIssue;
import factorypatern.PackageIssueFactory;
import proxy.ProxyDemo;
import chainofresponsibility.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Factory Method
        PackageIssueFactory factory = new ConcretePackageIssueFactory();

        System.out.println("===== Smart Package Exception & Resolution System =====");
        System.out.println("Select the delivery issue:");
        System.out.println("1. Lost Package");
        System.out.println("2. Damaged Package");
        System.out.println("3. Delayed Package");
        System.out.println("4. Wrong Address");
        System.out.println("5. Wrong Package");
        System.out.println("6. Recipient Unavailable");

        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();

        IssueType issueType;

        switch (choice) {

            case 1:
                issueType = IssueType.LOST;
                break;

            case 2:
                issueType = IssueType.DAMAGED;
                break;

            case 3:
                issueType = IssueType.DELAYED;
                break;

            case 4:
                issueType = IssueType.WRONG_ADDRESS;
                break;

            case 5:
                issueType = IssueType.WRONG_PACKAGE;
                break;

            case 6:
                issueType = IssueType.RECIPIENT_UNAVAILABLE;
                break;

            default:
                System.out.println("Invalid choice.");
                sc.close();
                return;
        }

        PackageIssue issue = factory.createIssue(issueType);

        issue.displayIssue();

        sc.close();

        ProxyDemo.run();

        CORDemo.main(args);

    }
}