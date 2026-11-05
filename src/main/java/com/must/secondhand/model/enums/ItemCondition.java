package com.must.secondhand.model.enums;

/**
 * Item condition. The rank is used by the matching rule: a listing satisfies a buyer
 * when its rank is lower than or equal to the buyer's acceptable rank.
 */
public enum ItemCondition {
    NEW("New", 0),
    LIKE_NEW("Like new", 1),
    GOOD("Good", 2),
    FAIR("Fair", 3);

    private final String label;
    private final int rank;

    ItemCondition(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    public String getLabel() {
        return label;
    }

    public int getRank() {
        return rank;
    }
}
