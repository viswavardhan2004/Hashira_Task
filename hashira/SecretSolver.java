import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecretSolver {

    public static void main(String[] args) {
        try {
            String secret1 = solveFromJson("testcase1.json");
            String secret2 = solveFromJson("testcase2.json");

            System.out.println("Secret from Test Case 1: " + secret1);
            System.out.println("Secret from Test Case 2: " + secret2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String solveFromJson(String filePath) throws Exception {
        String content = readFile(filePath);
        
        int k = extractKValue(content);
        List<Point> points = extractPoints(content);
        return lagrangeInterpolation(points, k).toString();
    }

    private static String readFile(String filePath) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static int extractKValue(String json) {
        Pattern pattern = Pattern.compile("\"k\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new RuntimeException("Could not find k value in JSON");
    }

    private static List<Point> extractPoints(String json) {
        List<Point> points = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"\\s*\\}");
        Matcher matcher = pattern.matcher(json);
        
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int base = Integer.parseInt(matcher.group(2));
            String value = matcher.group(3);
            
            BigInteger y = decodeValue(value, base);
            points.add(new Point(x, y));
        }
        
        return points;
    }

    private static BigInteger decodeValue(String value, int base) {
        value = value.toLowerCase();
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBig = BigInteger.valueOf(base);
        
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int digit;
            
            if (c >= '0' && c <= '9') {
                digit = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                digit = 10 + (c - 'a');
            } else {
                throw new IllegalArgumentException("Invalid character for base " + base + ": " + c);
            }
            
            if (digit >= base) {
                throw new IllegalArgumentException("Digit " + digit + " is invalid for base " + base);
            }
            
            result = result.multiply(baseBig).add(BigInteger.valueOf(digit));
        }
        
        return result;
    }

    private static BigInteger lagrangeInterpolation(List<Point> points, int k) {
        if (points.size() > k) {
            points = points.subList(0, k);
        }
        
        BigInteger secret = BigInteger.ZERO;
        
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            BigInteger xi = BigInteger.valueOf(point.x);
            BigInteger yi = point.y;
            
            BigInteger term = yi;
            
            for (int j = 0; j < points.size(); j++) {
                if (j == i) continue;
                
                Point other = points.get(j);
                BigInteger xj = BigInteger.valueOf(other.x);
                BigInteger numerator = BigInteger.ZERO.subtract(xj);
                BigInteger denominator = xi.subtract(xj);
                term = term.multiply(numerator).divide(denominator);
            }
            
            secret = secret.add(term);
        }
        
        return secret;
    }

    static class Point {
        int x;
        BigInteger y;
        
        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}