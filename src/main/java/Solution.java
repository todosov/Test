import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class Solution {

    /**
     * This task deals with string manipulation using the example of IPv6
     * address representation.
     * <p>
     * The task is to implement the method getSimplifiedAddress in the class
     * IPv6Address. To test your code run the main method.
     * <p>
     * Format of an IPv6 address: 8 hexadecimal numbers separated by colons.
     * <p>
     * For convenience, an IPv6 address may be simplified to shorter notations
     * by application of the following rules:
     * <p>
     * 1. Leading zeros of each hexadecimal number are suppressed.
     * For example, 2001:0db8::0001 is rendered as 2001:db8::1.
     * <p>
     * 2. Two or more consecutive groups containing zeros only may be replaced
     * with a single empty group, using two consecutive colons (::).
     * For example, 2001:db8:0:0:0:0:2:1 is shortened to 2001:db8::2:1
     * (but 2001:db8:0:1:1:1:1:1 is rendered as 2001:db8:0:1:1:1:1:1).
     * <p>
     * 3. Representations are shortened as much as possible.
     * The longest sequence of consecutive all-zero fields is replaced with
     * double-colon.
     * If there are multiple longest runs of all-zero fields, then it is the
     * leftmost that is compressed.
     * E.g., 2001:db8:0:0:1:0:0:1 is rendered as 2001:db8::1:0:0:1 rather than
     * as 2001:db8:0:0:1::1.
     */

    // Test examples - a map of full IPv6 addresses and their simplified representation
    public static final List<Entry<String, String>> IPV6_ADDRESSES = new ArrayList<Entry<String, String>>() {
        private static final long serialVersionUID = 7170906660651650870L;

        {
            add(new SimpleEntry<>("1111:2222:3333:4444:5555:0ab9:0e0f:0010", "1111:2222:3333:4444:5555:ab9:e0f:10")); // rule 1
            add(new SimpleEntry<>("1111:2222:3333:4444:0000:0006:0070:0800", "1111:2222:3333:4444:0:6:70:800")); // rule 1 - checking that a single group of all zeroes is replaced by 0
            add(new SimpleEntry<>("1111:2222:0000:0000:0000:0000:7777:8888", "1111:2222::7777:8888")); // rule 1+2
            add(new SimpleEntry<>("1111:2222:3330:0000:0000:0666:7777:8888", "1111:2222:3330::666:7777:8888")); // rule 1+2
            add(new SimpleEntry<>("1111:2222:0000:0001:0001:0001:0001:0001", "1111:2222:0:1:1:1:1:1")); // rule 1+2 - checking that consecutive groups of 1 are not replaced with ::
            add(new SimpleEntry<>("1111:2222:0000:0000:5555:0000:0000:0000", "1111:2222:0:0:5555::")); // rule 1+2+3
            add(new SimpleEntry<>("1111:2222:0000:0000:5555:0000:0000:8888", "1111:2222::5555:0:0:8888")); // rule 1+2+3
            add(new SimpleEntry<>("0000:0000:3333:4444:5555:6666:7777:8888", "::3333:4444:5555:6666:7777:8888")); // rule 1+2+3
        }
    };

    public static void main(String[] args) {

        for (Entry<String, String> addressEntry : IPV6_ADDRESSES) {
            final IPv6Address address = new IPv6Address(addressEntry.getKey());
            final String expectedSimplified = addressEntry.getValue();
            final String actualSimplified = address.getSimplifiedRepresentation();
            if (expectedSimplified.equals(actualSimplified)) {
                System.out.println("Correct! Original: [" + address + "] Expected: [" + expectedSimplified + "] Actual: [" + actualSimplified + "] ");
            } else {
                System.err.println("INCORRECT! Original: [" + address + "] Expected: [" + expectedSimplified + "] Actual: [" + actualSimplified + "] ");
            }
        }
    }

    public static class IPv6Address {

        private final String address;

        public IPv6Address(final String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return address;
        }

        public String getSimplifiedRepresentation() {
            return applyRulesTwoAndThree(applyRuleOne(address));
        }

        private String applyRuleOne(String ip) {
            String[] parts = ip.split(":");
            List<String> resIp = new ArrayList<>();
            for (String part : parts) {
                String res = part;
                for (int i = 0; i < 4; i++) {
                    if (part.charAt(i) == '0') {
                        if (res.length() > 1) {
                            res = part.substring(i + 1);
                        }
                    } else {
                        break;
                    }
                }
                resIp.add(res);
            }
            return String.join(":", resIp);
        }

        private String applyRulesTwoAndThree(String ip) {
            List<String> parts = Arrays.asList(ip.split(":"));

            // define all consecutive groups containing zeros
            Map<Integer, Integer> emptyGroupPositions = new HashMap<>();
            int count = 0;
            int startPosition = 0;
            for (int i = 0; i < parts.size() - 1; i++) {
                if ("0".equals(parts.get(i)) && "0".equals(parts.get(i + 1))) {
                    if (count > 0) {
                        count++;
                    } else {
                        startPosition = i;
                        count++;
                    }
                } else if (count > 0) {
                    emptyGroupPositions.put(startPosition, ++count);
                    count = 0;
                }
                if (i == parts.size() - 2 && count > 0) {
                    emptyGroupPositions.put(startPosition, ++count);
                }
            }

            // get either the smallest consecutive group or the leftest one
            Optional<Entry<Integer, Integer>> emptyGroup = emptyGroupPositions.entrySet().stream().min((o1, o2) -> {
                int compare = o2.getValue().compareTo(o1.getValue());
                if (compare != 0) {
                    return compare;
                } else {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            // remove appropriate consecutive group if exists
            String result = null;
            if (emptyGroup.isPresent()) {
                Entry<Integer, Integer> entry = emptyGroup.get();
                String left = String.join(":", parts.subList(0, entry.getKey()));
                String right = String.join(":", parts.subList(entry.getKey() + entry.getValue(), parts.size()));
                result = left + "::" + right;
            } else {
                result = String.join(":", parts);
            }

            return result;
        }

        public String getAddress() {
            return address;
        }
    }
}
