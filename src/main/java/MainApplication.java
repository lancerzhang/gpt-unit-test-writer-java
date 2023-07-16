public class MainApplication {
    public static void main(String[] args) throws Exception {
        String projectPath = "/Users/lancer/Development/ws/survey-server";
        int limit = 1;
        UnitTestWriter writer = new UnitTestWriter(projectPath, limit);
        writer.generateUnitTest();
    }

}
