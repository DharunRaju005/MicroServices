import java.util.*;

public class Solution {

    public void helper(int n, int o, int c, List<String> ans, StringBuilder temp) {
        System.out.println("Called helper(o=" + o + ", c=" + c + ", temp=" + temp + ")");

        if (o + c == 2 * n) {
            System.out.println("==> Adding to answer: " + temp.toString());
            ans.add(temp.toString());
            return;
        }

        if (o < n) {
            temp.append("(");
            System.out.println("Appended '(': " + temp);
            helper(n, o + 1, c, ans, temp);
            temp.deleteCharAt(temp.length() - 1);
            System.out.println("Backtracked after '(': " + temp);
        }

        if (c < o) {
            temp.append(")");
            System.out.println("Appended ')': " + temp);
            helper(n, o, c + 1, ans, temp);
            temp.deleteCharAt(temp.length() - 1);
            System.out.println("Backtracked after ')': " + temp);
        }
    }

    public List<String> generateParenthesis(int n) {
        List<String> ans = new ArrayList<>();
        helper(n, 0, 0, ans, new StringBuilder());
        return ans;
    }

    // 🔹 Driver function
    public static void main(String[] args) {
        Solution sol = new Solution();
        int n = 3;  // You can change this value to test other cases
        List<String> result = sol.generateParenthesis(n);

        System.out.println("\nAll valid combinations for n = " + n + ":");
        for (String s : result) {
            System.out.println(s);
        }
    }
}
