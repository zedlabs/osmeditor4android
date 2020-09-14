package de.blau.android.osm;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Small container for results of operations that can result in multiple errors that need to be reported
 * 
 * @author Simon
 *
 */
public class Result implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2L;

    private OsmElement          element;
    private Set<Issue>          issues = null;
    private Map<String, String> tags   = null;
    private String              elementType;
    private long                elementId;

    /**
     * Empty default constructor
     */
    public Result() {
    }

    /**
     * Construct a new MergeResult
     * 
     * @param element the OsmElement we are returning
     */
    public Result(@NonNull OsmElement element) {
        this.element = element;
    }

    /**
     * Add an issue to the list of issues
     * 
     * @param issue the MergeIssue to add
     */
    public void addIssue(@NonNull Issue issue) {
        if (issues == null) {
            issues = new HashSet<>();
        }
        issues.add(issue);
    }

    /**
     * Add a Collection of issues
     * 
     * @param issues a Collection containing Issues
     */
    public void addAllIssues(@NonNull Collection<Issue> issues) {
        if (this.issues == null) {
            this.issues = new HashSet<>();
        }
        this.issues.addAll(issues);
    }

    /**
     * Check if the merge had issues
     * 
     * @return true if there are issues
     */
    public boolean hasIssue() {
        return issues != null && !issues.isEmpty();
    }

    /**
     * Get the current Issues
     * 
     * @return a Collection of Issues
     */
    @Nullable
    public Collection<Issue> getIssues() {
        return issues;
    }

    /**
     * Get the stored OsmElement
     * 
     * @return the element
     */
    public OsmElement getElement() {
        return element;
    }

    /**
     * Set the stored OsmElement
     * 
     * @param element the element to set
     */
    public void setElement(OsmElement element) {
        this.element = element;
    }

    /**
     * Get any relevant tags or null
     * 
     * @return the tags
     */
    @Nullable
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Add relevant tags for the result
     * 
     * @param tags the tags to add
     */
    public void addTags(@NonNull Map<String, String> tags) {
        if (this.tags == null) {
            this.tags = tags;
        } else {
            this.tags.putAll(tags);
        }
    }

    /**
     * Save type and id of element, and zap element
     */
    public void saveElement() {
        elementType = element.getName();
        elementId = element.getOsmId();
        element = null;
    }

    /**
     * Restore the element from the saved values
     * 
     * @param delegator the current StorageDelegator element
     */
    public void restoreElement(@NonNull StorageDelegator delegator) {
        element = delegator.getOsmElement(elementType, elementId);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(element.getDescription());
        if (hasIssue()) {
            for (Issue issue : issues) {
                b.append(" ");
                b.append(issue.toString());
            }
        }
        return b.toString();
    }
}
