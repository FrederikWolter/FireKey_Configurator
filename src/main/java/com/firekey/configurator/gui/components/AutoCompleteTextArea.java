package com.firekey.configurator.gui.components;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCompleteTextArea extends TextArea {

    /**
     * The autocomplete entries.
     */
    private SortedSet<String> autoCompleteEntries;

    /**
     * The maximum Number of entries displayed in the popup.<br>
     * Default: 10
     */
    private int maxEntries = 10;

    /**
     * The popup used to select an entry.
     */
    private final ContextMenu autoCompletePopUp;

    /**
     * To prevent recommendation after update
     */
    private boolean updateCall;

    public AutoCompleteTextArea() {
        super();
        setWrapText(true);
        autoCompleteEntries = new TreeSet<>();
        autoCompletePopUp = new ContextMenu();

        textProperty().addListener((observable, oldValue, newValue) -> {
            if (getText().isEmpty() || updateCall) {
                autoCompletePopUp.hide();
                updateCall = false;
            } else {
                List<String> searchResult = new LinkedList<>();
                // Get the current entered line / function
                String enteredText = getCurrentWord();
                if (enteredText == null || enteredText.isEmpty()) {
                    autoCompletePopUp.hide();
                    return;
                }

                // Check if the current entered text line as suggestions in the autocomplete entries
                Pattern pattern = Pattern.compile(".*" + Pattern.quote(enteredText) + ".*\\s*.*", Pattern.CASE_INSENSITIVE);
                for (String entry : autoCompleteEntries) {
                    Matcher matcher = pattern.matcher(entry);
                    if (matcher.matches()) {
                        searchResult.add(entry);
                    }
                }

                if (searchResult.isEmpty()) {
                    autoCompletePopUp.hide();
                } else {
                    populatePopup(searchResult, enteredText);
                    if (!autoCompletePopUp.isShowing()) {
                        Point2D point2D = localToScreen(0, 0);
                        Text text = new Text(getText());
                        text.setFont(getFont());
                        autoCompletePopUp.show(this, point2D.getX(), point2D.getY() + text.getLayoutBounds().getHeight() + 10);
                    }
                }

            }
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> autoCompletePopUp.hide());

    }

    /**
     * Gets the current line a user is typing a word in. <br>
     * A line new line is started with space or \n
     *
     * @return The current word/function a user is typing.
     */
    private String getCurrentWord() {
        // Split on each space or new line but add empty "lines" to the result (spaces are counted as new line)
        String[] lines = getText().split("\\n|\\s");

        int caretPos = getCaretPosition();

        int lineStart = 0;

        for (String line : lines) {
            int lineEnd = lineStart + line.length();
            // check if the current line is the line where the caret is at
            if (lineEnd >= caretPos + 1) {

                // check if is inside parenthesis
                // check if is inside parenthesis
                Point2D bracketIndexes = getSelectedBracketIndexes(lineStart, line, true);
                if (bracketIndexes.getX() != -1) {
                    return line.substring((int) bracketIndexes.getX() + 1, (int) bracketIndexes.getY());
                }

                return line.trim();
            }
            lineStart = lineEnd + 1; // add 1 to skip the newline character
        }
        return null;
    }

    /**
     * Get the current word start index inside the textarea. <br>
     * A line new line is started with space or \n
     *
     * @return A {@link ReplaceStartIndex}
     */
    private ReplaceStartIndex getCurrentWordLineStart() {
        // Split on each space or new line but add empty "lines" to the result (spaces are counted as new line)
        String[] lines = getText().split("\\n|\\s");

        int caretPos = getCaretPosition();

        int lineStart = 0;

        for (String line : lines) {
            int lineEnd = lineStart + line.length();
            // check if the current line is the line where the caret is at
            if (lineEnd >= caretPos) {

                // check if is inside parenthesis
                Point2D bracketIndexes = getSelectedBracketIndexes(lineStart, line, false);
                if (bracketIndexes.getX() != -1) {
                    // if we are inside of parenthesis the start index is the starting parenthesis
                    return new ReplaceStartIndex(lineStart + (int) bracketIndexes.getX() + 1, true);
                }

                return new ReplaceStartIndex(lineStart, false);
            }
            lineStart = lineEnd + 1; // add 1 to skip the newline character
        }
        return new ReplaceStartIndex(0, false);
    }

    /**
     * Populate the entry set with the given search results. Display is limited
     * to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<String> searchResult, String text) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int count = Math.min(searchResult.size(), this.maxEntries);
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            int occurence = result.toLowerCase().indexOf(text.toLowerCase());

            if (occurence < 0) {
                continue;
            }
            // Part before occurence (might be empty)
            Text pre = new Text(result.substring(0, occurence));
            // Part of (first) occurence
            Text in = new Text(result.substring(occurence, occurence + text.length()));
            //Part after occurence
            Text post = new Text(result.substring(occurence + text.length()));

            TextFlow entryFlow = new TextFlow(pre, in, post);

            CustomMenuItem item = new CustomMenuItem(entryFlow, true);
            item.setOnAction((ActionEvent actionEvent) ->
            {
                String replacement = result;
                int textLen = text.length();
                ReplaceStartIndex replaceStartIndex = getCurrentWordLineStart();
                if(replaceStartIndex.insideBracket){
                    // if we replace inside brackets, remove a possible semicolons at the end
                    replacement = replacement.replaceAll(";$", "");
                }
                updateCall = true;
                replaceText(replaceStartIndex.startIndex, replaceStartIndex.startIndex + textLen, replacement);
                autoCompletePopUp.hide();
            });
            menuItems.add(item);
        }
        autoCompletePopUp.getItems().clear();
        autoCompletePopUp.getItems().addAll(menuItems);

    }

    /**
     * Gets the indices of the caret surrounding brackets
     *
     * @param lineStart The start of the current line
     * @param input     The input text
     * @param update    true, if this function is called before the caret is updated
     * @return A {@link Point2D} where x is the opening bracket and y the closing bracket
     */
    private Point2D getSelectedBracketIndexes(Integer lineStart, String input, boolean update) {

        Map<Integer, Integer> bracketMap = getBracketMap(input);
        // If it is an update, the caret is moved after the text is appended. so we need to add +1 manually
        int caretPos = update ? getCaretPosition() + 1 : getCaretPosition();

        Point2D bracketIndexes = new Point2D(-1, -1);

        for (Map.Entry<Integer, Integer> entry : bracketMap.entrySet()) {

            if (entry.getKey() + lineStart < caretPos && entry.getValue() + lineStart >= caretPos && entry.getKey().compareTo((int) bracketIndexes.getX()) > 0) {
                bracketIndexes = new Point2D(entry.getKey(), entry.getValue());
            }

        }
        return bracketIndexes;
    }

    /**
     * Checks if a given bracket pair is inside another bracket pair
     *
     * @param bracket The bracket pair to check
     * @param input   The input text to calculate the other brackets
     * @return true, if the bracket is inside other brackets.
     */
    private boolean isInsideOtherBrackets(Point2D bracket, String input) {
        Map<Integer, Integer> bracketMap = getBracketMap(input);

        if (bracket.getX() != -1)
            return false;

        for (Map.Entry<Integer, Integer> entry : bracketMap.entrySet()) {
            if (bracket.getX() > entry.getKey()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the brackets map. <br>
     * Key is the index of the opening bracket, value is the key of the closing bracket.
     *
     * @param input The input text on which the calculation happens.
     * @return The calculated brackets map
     */
    private Map<Integer, Integer> getBracketMap(String input) {
        // Create a HashMap to store the indices of opening brackets and their corresponding closing brackets
        Map<Integer, Integer> bracketMap = new HashMap<>();

        // Create a stack to keep track of the indices of opening brackets
        Deque<Integer> openingBrackets = new ArrayDeque<>();

        // Loop through each character in the input string
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // If the character is an opening bracket, push its index onto the stack
            if (c == '(' || c == '[' || c == '{') {
                openingBrackets.push(i);
            }

            // If the character is a closing bracket, pop the last opening bracket index off the stack and add it to the map
            if ((c == ')' || c == ']' || c == '}') && !openingBrackets.isEmpty()) {
                int openIndex = openingBrackets.pop();
                bracketMap.put(openIndex, i);
            }
        }
        return bracketMap;
    }

    public void setAutoCompleteEntries(SortedSet<String> entries) {
        this.autoCompleteEntries = entries;
    }

    public AutoCompleteTextArea addAutoCompleteEntry(String entry) {
        this.autoCompleteEntries.add(entry);
        return this;
    }

    public AutoCompleteTextArea addAutoCompleteEntry(Set<String> entries) {
        this.autoCompleteEntries.addAll(entries);
        return this;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }


    /**
     * Used to collect a start index of a replacement text and if it is inside a bracket.
     * @param startIndex
     * @param insideBracket
     */
    private record ReplaceStartIndex(int startIndex, boolean insideBracket) {}

}
