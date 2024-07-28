package org.enginehub.linbus.gui.util;

/**
 * Pluralizes words. Should probably use a real internationalization library if this program ever gets translated.
 */
public record PluralizerRule(String singular, String plural) {
    public String applyWithCount(int count) {
        return count + " " + apply(count);
    }

    public String apply(int count) {
        return count == 1 ? singular : plural;
    }
}
