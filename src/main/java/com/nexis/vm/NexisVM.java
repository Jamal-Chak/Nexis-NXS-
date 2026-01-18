package com.nexis.vm;

import java.util.Stack;

public class NexisVM {

    public static int execute(String script, int input) {
        Stack<Integer> stack = new Stack<>();
        stack.push(input);

        String[] tokens = script.split(" ");
        for (String token : tokens) {
            try {
                switch (token) {
                    case "ADD":
                        stack.push(stack.pop() + stack.pop());
                        break;
                    case "SUB":
                        int a = stack.pop();
                        int b = stack.pop();
                        stack.push(b - a);
                        break;
                    case "MUL":
                        stack.push(stack.pop() * stack.pop());
                        break;
                    case "DUP":
                        stack.push(stack.peek());
                        break;
                    default:
                        if (token.startsWith("PUSH:")) {
                            stack.push(Integer.parseInt(token.substring(5)));
                        }
                        break;
                }
            } catch (Exception e) {
                System.err.println("VM Execution Error: " + e.getMessage());
                return -1;
            }
        }

        return stack.isEmpty() ? 0 : stack.pop();
    }
}
