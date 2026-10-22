package prototype;

import chainofresponsibility.PackageIssue;

// Prototype Pattern — Prototype: declares how a pre-configured issue blueprint copies itself.
// New issues are produced by cloning a blueprint and filling in only what differs (tracking number, customer),
// instead of re-entering every field from scratch.
public interface IssuePrototype extends Cloneable {

    // Returns an independent copy: changing the copy never affects the original
    IssuePrototype clone();

    // Turns the blueprint into a real, validated PackageIssue ready to be saved
    PackageIssue createIssue(String trackingNumber, int customerId);
}
