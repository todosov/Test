import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/*
 * The goal of this task is to implement a class that represents a table
 * with filters (like Excel).
 *
 * Already defined: An interface Table and a class skeleton TableImpl.
 *
 * The task is to implement the concrete class - define members, define a
 * constructor (if needed), define how the data is passed to the class,
 * implement the overridden functions.
 *
 * Like in Excel, the filters are inclusive. The rows that are returned are
 * the rows that have the filter values. If there is no filter defined than
 * all rows should be returned.
 *
 * If you need more classes, simply define them inline.
 */

class Solution2 {

    public static void main(String[] args) {

        Table table = TableImpl.builder()
                .addRow("green", "lion", "1")
                .addRow("blue", "dog", "2")
                .addRow("yellow", "cat", "3")
                .addRow("yellow", "tiger", "4")
                .addRow("red", "lion", "5")
                .addRow("red", "tiger", "6")
                .addRow("blue", "tiger", "7")
                .addRow("yellow", "tiger", "8")
                .addRow("red", "lion", "9")
                .addRow("blue", "cat", "10")
                .build();

        // Add some filters - let's say column 0 is colors and column 1 is animals
        table.addFilter(0, "yellow");
        table.addFilter(0, "red");
        table.addFilter(1, "lion");

        // Get the rows
        List<List<String>> rows0 = table.getFilteredRows();
        rows0.forEach(row -> {
            assertThat(row.get(0), anyOf(equalTo("yellow"), equalTo("red")));
            assertEquals("lion", row.get(1));
        });

        // Add and remove some filters
        table.removeFilter(0, "red");
        table.addFilter(1, "tiger");

        // Get the rows
        List<List<String>> rows1 = table.getFilteredRows();
        rows1.forEach(row -> {
            assertEquals("yellow", row.get(0));
            assertThat(row.get(1), anyOf(equalTo("tiger"), equalTo("lion")));
        });
    }

    public static interface Table {

        void addFilter(int columnIndex, String value);

        void removeFilter(int columnIndex, String value);

        List<List<String>> getFilteredRows();
    }

    public static class TableImpl implements Table {

        private List<List<String>> content;

        private Map<Integer, Set<String>> filters;

        private TableImpl(List<List<String>> content) {
            this.content = content;
            filters = new HashMap<>();
        }

        @Override
        public void addFilter(int columnIndex, String value) {
            if (columnIndex >= 0 && columnIndex < content.size()) {
                if (filters.containsKey(columnIndex)) {
                    filters.get(columnIndex).add(value);
                } else {
                    filters.put(columnIndex, new HashSet<>(Arrays.asList(value)));
                }
            } else {
                System.out.println("Column index is out of boundaries");
            }
        }

        @Override
        public void removeFilter(int columnIndex, String value) {
            if (filters.containsKey(columnIndex)) {
                filters.get(columnIndex).remove(value);
            } else {
                System.out.println("There is no filter for column index " + columnIndex);
            }
        }

        @Override
        public List<List<String>> getFilteredRows() {
            List<List<String>> filteredRows = new ArrayList<>();
            content.forEach(row -> {
                boolean isFiltered = true;
                for (Map.Entry<Integer, Set<String>> filter : filters.entrySet()) {
                    isFiltered = isFiltered && isIn(row.get(filter.getKey()), filter.getValue());
                }
                if (isFiltered) {
                    filteredRows.add(row);
                }
            });

            return filteredRows;
        }

        public static TableBuilder builder() {
            return new TableBuilder();
        }

        private static boolean isIn(String columnValue, Set<String> filterValues) {
            for (String filterValue : filterValues) {
                if (filterValue.equals(columnValue)) {
                    return true;
                }
            }

            return false;
        }

        private static class TableBuilder {

            private List<List<String>> content = new ArrayList<>();

            private TableBuilder() {
            }

            public TableBuilder addRow(String... elements) {
                content.add(Arrays.asList(elements));
                return this;
            }

            public Table build() {
                if (content.stream().map(List::size).distinct().count() > 1) {
                    throw new IllegalArgumentException("All rows should have the same length");
                }
                return new TableImpl(content);
            }
        }
    }
}
