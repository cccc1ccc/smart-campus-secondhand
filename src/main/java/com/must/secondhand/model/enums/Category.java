package com.must.secondhand.model.enums;

/** Fixed category list used by listings and want-to-buy requests (REQ-2.1 / REQ-4.1). */
public enum Category {
    BOOKS("Books & Notes"),
    ELECTRONICS("Electronics"),
    FURNITURE("Furniture"),
    DAILY_SUPPLIES("Daily Supplies"),
    CLOTHING("Clothing"),
    SPORTS("Sports & Fitness"),
    OTHER("Other");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
