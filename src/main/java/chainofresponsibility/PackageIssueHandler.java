package chainofresponsibility;

public abstract class PackageIssueHandler {

    protected PackageIssueHandler nextHandler;

    public void setNextHandler(PackageIssueHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void handleIssue(PackageIssue issue);

    protected void passToNextHandler(PackageIssue issue) {

        if (nextHandler != null) {
            nextHandler.handleIssue(issue);
        } else {
            System.out.println("No further handler available.");
        }
    }
}