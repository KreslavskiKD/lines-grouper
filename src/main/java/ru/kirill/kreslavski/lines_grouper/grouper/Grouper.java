package ru.kirill.kreslavski.lines_grouper.grouper;


import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Grouper {

    private int currentLineNumber;
    private HashMap<String, Node> map;
    private List<String> lines;

    public Grouper() {
        currentLineNumber = 0;
        map = new HashMap<>();
        lines = new ArrayList<>();
    }

    public void writeStats(BufferedWriter buff) throws IOException {
        HashMap<Node, Set<Integer>> groups = new HashMap<>();

        for (Node n : map.values()) {
            Node group = findSet(n);
            if (!groups.containsKey(group)) {
 //               if (group.lineNumbers.size() > 1) {
                    groups.put(group, n.lineNumbers);
 //               }
            } else {
                groups.get(group).addAll(n.lineNumbers);
            }
        }

        List<Node> sorted = groups.keySet().stream()
                .filter((Node a) -> groups.get(a).size() > 1)
                .sorted(Comparator.comparingInt((Node a) -> -a.rank))
                .collect(Collectors.toList());

        buff.write("" + sorted.size());
        buff.newLine();

        int i = 1;
        for (Node n : sorted) {
            buff.write("Группа " + i);
            buff.newLine();
            for (Integer num : groups.get(n)) {
                buff.write(lines.get(num));
                buff.newLine();
            }
            buff.newLine();
            i++;
        }
    }

    public void consume(String line) {
        int column = 0;
        List<String> list = Arrays.stream(line.split(";")).collect(Collectors.toList());
        List<Node> nodes = new ArrayList<>();
        List<Node> newNodes = new ArrayList<>();

        for (String l : list) {
            // Check if the substring is empty
            if (l == null || l.isEmpty()) {
                column++;
                continue;
            }

            // Check if the substring is correct
            if (!stringValidation(l)) {
                return;
            }

            String newKey = l + "-" + column;
            if (map.containsKey(newKey)) {
                nodes.add(map.get(newKey));
            } else {
                Node node = new Node(newKey, currentLineNumber);
                newNodes.add(node);
            }
            column++;
        }

        if (newNodes.isEmpty()) {
            if (nodes.isEmpty()) {
                return;
            }

            Set<Integer> initialSet = new HashSet(nodes.get(0).lineNumbers);

            for (int i = 1; i < nodes.size(); i++) {
                initialSet.retainAll(nodes.get(i).lineNumbers);
            }

            if (!initialSet.isEmpty()) {
                return;
            }
        }

        for (Node n : newNodes) {
            map.put(n.stringPart, n);
        }

        for (Node n : nodes) {
            n.lineNumbers.add(currentLineNumber);
        }

        if (!nodes.isEmpty()) {
            if (nodes.size() > 1) {
                Node start = nodes.get(0);
                for (int i = 1; i < nodes.size(); i++) {
                    union(start, nodes.get(i));
                }
            }
        }

        if (!newNodes.isEmpty()) {
            if (newNodes.size() > 1) {
                Node start = newNodes.get(0);
                for (int i = 1; i < newNodes.size(); i++) {
                    union(start, newNodes.get(i));
                }
            }
        }

        if (!nodes.isEmpty() && !newNodes.isEmpty()) {
            union(nodes.get(0), newNodes.get(0));
        }

        lines.add(line);
        currentLineNumber++;
    }

    private boolean stringValidation(String s) {
        if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            // Check if the middle part of the string contains any quotes
            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) == '"') {
                    return false;
                }
            }
        } else if (s.charAt(0) == '"' && s.charAt(s.length() - 1) != '"' || s.charAt(0) != '"' && s.charAt(s.length() - 1) == '"') {
            return false;
        }
        return true;
    }

    private Node findSet(Node a) {
        Node topmostParent = a;
        while (topmostParent.parent != topmostParent) {
            topmostParent = topmostParent.parent;
        }
        return topmostParent;
    }

    private Node union(Node a, Node b) {
        a = findSet(a);
        b = findSet(b);
        if (a == b) {
            return a;
        }
        int newRank = a.rank + b.rank;
        if (a.rank >= b.rank) {
            b.parent = a;
            a.rank = newRank;
            return a;
        } else {
            a.parent = b;
            b.rank = newRank;
            return b;
        }
    }

    class Node {
        String stringPart;
        Set<Integer> lineNumbers;
        int rank;

        Node parent;

        public Node(String stringPart, int lineNumber) {
            this.stringPart = stringPart;
            this.lineNumbers = new HashSet<>();
            lineNumbers.add(lineNumber);
            rank = 1;
            parent = this;
        }
    }
}
