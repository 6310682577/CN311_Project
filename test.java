public class test {
    public static void main(String[] args) {
        String[] test = "1 2/3 5/6 7/1 6/2 8/".split("/");
        System.out.println(test.length);
        for (String tests : test)
            System.out.println(tests);
    }
}
