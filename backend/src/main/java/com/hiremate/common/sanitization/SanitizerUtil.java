package com.hiremate.common.sanitization;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public final class SanitizerUtil {

    private static final PolicyFactory POLICY_FACTORY = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "strong", "em", "p", "br", "ul", "ol", "li")
            .toFactory();

    private SanitizerUtil() {
        // Prevent instantiation
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return POLICY_FACTORY.sanitize(input);
    }

    public static String sanitizeStrict(String input) {
        if (input == null) {
            return null;
        }
        // Strips ALL HTML tags completely for plain text fields like username/title
        return input.replaceAll("<[^>]*>", "").trim();
    }
}
