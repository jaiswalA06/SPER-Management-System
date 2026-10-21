package memento;

import chainofresponsibility.PackageIssue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Memento Pattern — Caretaker. Keeps a per-issue undo stack of opaque snapshots it cannot read or modify.
public class IssueHistoryCaretaker {

    private final Map<Integer, Deque<PackageIssue.Memento>> history = new HashMap<>();

    // Asks the issue for a snapshot before its status is changed
    public void save(PackageIssue issue) {
        history.computeIfAbsent(issue.getIssueId(), id -> new ArrayDeque<>()).push(issue.save());
        System.out.println("[Memento] Snapshot saved for Issue #" + issue.getIssueId()
                + " (status: " + issue.getStatus() + ")");
    }

    // Returns the most recent snapshot without removing it, so a failed restore loses nothing
    public Optional<PackageIssue.Memento> peekLast(int issueId) {
        Deque<PackageIssue.Memento> stack = history.get(issueId);
        return stack == null ? Optional.empty() : Optional.ofNullable(stack.peek());
    }

    // Drops the most recent snapshot: after a successful undo, or when the change it guarded never happened
    public void discardLast(int issueId) {
        Deque<PackageIssue.Memento> stack = history.get(issueId);
        if (stack != null) {
            stack.poll();
        }
    }

    public int getHistorySize(int issueId) {
        Deque<PackageIssue.Memento> stack = history.get(issueId);
        return stack == null ? 0 : stack.size();
    }
}
