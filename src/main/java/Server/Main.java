package Server;

public class Main {

    /**
     * Launch this function to start the server
     *
     * @param args Don't argue with the server
     */
    public static void main(String[] args) {
        // write your code here

        webServer server = new webServer();
        server.start();

        /*
        get("/hello", (req, res) -> {

            int = rand.nextInt(1000) +1;

            String out = "Hello World";
            return out;
        });
        */
    }
}
