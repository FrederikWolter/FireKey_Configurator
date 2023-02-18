package com.firekey.configurator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Test {
    public static void main(String[] args) {
        String input = "if(test(keyboard.data)";

        // Create a HashMap to store the indices of opening brackets and their corresponding closing brackets
        Map<Integer, Integer> bracketMap = new HashMap<>();

        // Create a stack to keep track of the indices of opening brackets
        Stack<Integer> openingBrackets = new Stack<>();

        // Loop through each character in the input string
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // If the character is an opening bracket, push its index onto the stack
            if (c == '(' || c == '[' || c == '{') {
                openingBrackets.push(i);
            }

            // If the character is a closing bracket, pop the last opening bracket index off the stack and add it to the map
            if (c == ')' || c == ']' || c == '}') {
                if (!openingBrackets.empty()) {
                    int openIndex = openingBrackets.pop();
                    bracketMap.put(openIndex, i);
                }
            }
        }

        // Print the map of opening and closing brackets
        for (Map.Entry<Integer, Integer> entry : bracketMap.entrySet()) {
            System.out.println("Opening bracket at index " + entry.getKey() + " corresponds to closing bracket at index " + entry.getValue());
        }
    }
}
