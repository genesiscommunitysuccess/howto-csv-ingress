package csv_ingress.utilities.json_utilities;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when there are mismatches in JSON comparison.
 */
public class JSONComparisonException extends AssertionError {
    private final List<Map<String, JsonNode>> mismatchedRows;

    /**
     * Constructs a new JSONComparisonException with the specified detail message and mismatched rows.
     *
     * @param message        the detail message
     * @param mismatchedRows the list of mismatched rows
     */
    public JSONComparisonException(String message, List<Map<String, JsonNode>> mismatchedRows) {
        super(message + "\n" + formatMismatchedRows(mismatchedRows));
        this.mismatchedRows = mismatchedRows;
    }

    /**
     * Returns the list of mismatched rows.
     *
     * @return the list of mismatched rows
     */
    public List<Map<String, JsonNode>> getMismatchedRows() {
        return mismatchedRows;
    }

    /**
     * Formats the mismatched rows into a string.
     *
     * @param mismatchedRows the list of mismatched rows
     * @return the formatted string
     */
    private static String formatMismatchedRows(List<Map<String, JsonNode>> mismatchedRows) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, JsonNode> mismatch : mismatchedRows) {
            sb.append("Expected: ").append(mismatch.get("expected")).append("\n");
            sb.append("Actual: ").append(mismatch.get("actual")).append("\n");
        }
        return sb.toString();
    }
}
