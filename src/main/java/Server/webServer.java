package Server;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;

import static spark.Spark.*;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import spark.ModelAndView;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * This contains all of the functions necessary for running the webserver. It also specifies all of the pages that
 * can be accessed. The default port is 4567. Save both the images from the index page to the same folder as the
 * decoder .jar file and run it. Both images will be in png format and require the full file name.
 * <p>
 * Sites to look at:
 * <p>
 * 127.0.0.1:4567/index     This is the main page with both an unmodified and modified image. Start here.
 * <p>
 * 127.0.0.1:4567/stega     The raw result of the project is here. This contains a dynamically generated image with a
 * hidden IP address in it. If this image is saved as a PNG (jpegs are lossy), one can run
 * it through the included decoder to determine what the address the host who saved the file
 * is.
 * <p>
 * The images are located in /Proxy/src/main/resources
 * The templates for freemarker are located in /Proxy/src/main/resources/spark/template/freemarker
 */
public class webServer {


    // local port on which the server is to bind
    private int PORT = 4567;
    Random rand = new Random();
    String filepath = "./src/main/resources/";

    /**
     * The default constructor. Binds the server to port 4567
     */
    public webServer() {
    }

    /**
     * The alternate constructor if you would like to set a specific port to bind the server on
     *
     * @param Port An integer specifying where the server should bind to.
     */
    public webServer(int Port) {
        port(Port);
        PORT = Port;
    }

    /**
     * Launches the server
     *
     * @return This doesn't return anything
     */
    public boolean start() {

        staticFileLocation("/res");

        // Hello world
        get("/hello", (request, response) -> {

            int num = rand.nextInt(1000) + 1;

            String helloWorld = "Hello World, your number is " + num;

            String usragent = request.userAgent();
            String agentout = "Your user agent is: " + usragent + ". ";
            String ip = "Your IP address is " + request.ip() + ". ";
            String host = "The host is " + request.host() + ". ";

            String out = helloWorld + "\n" + agentout + ip + host;
            return out;
        });

        // Returns the IP address of the client in a raw String form. Useful for debugging?
        get("/ip", (request, response) -> {
            String ip = request.ip();
            return ip;
        });

        // A web page with random stuff on it. This was used mostly to figure out how this package works
        get("/index", (req, res) -> {
            Map<String, Object> map = new HashMap<>();

            map.put("adjective", "super sweet");

            String husky = "Husky.jpg";
            map.put("husky", husky);

            String dogs = "dogs.jpg";
            map.put("dogs", "./dogs");
            System.out.println(dogs);

            File dogFile = new File(filepath + dogs);
            BufferedImage img = ImageIO.read(dogFile);
            int height = img.getHeight();
            int width = img.getWidth();



            /* This code is no longer used but it shows how to convert an image to base 64 and embed that in a page
            // Dynamically modified masterchief picture
            String mc = imgToBase64(filepath + "mc.jpg");
            System.out.println("This was produced by the base64 conversion:");
            System.out.println(mc);
            map.put("mc", mc);
            */

            return new ModelAndView(map, "index.html");
        }, new FreeMarkerEngine());


        // Test for doing a direct http get request to handle image manipulation
        get("/picture.png", (request, response) -> {

            // Prep the HTTP response for an image file.
            OutputStream outputStream = response.raw().getOutputStream();
            response.raw().setContentType("image/png");


            // Open the Buffered Image to allow modification of it's contents
            File inputImageFile = new File(filepath + "mc.jpg");
            BufferedImage bufImg = null;
            try {
                bufImg = ImageIO.read(inputImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // modify image to test code
            int height = bufImg.getHeight();
            int width = bufImg.getWidth();
            for (int i = 0; i < height / 2; i++) {
                for (int j = 0; j < width / 2; j++) {
                    bufImg.setRGB(j, i, bufImg.getRGB(j, i) / 2);
                }
            }

            // Write the image back to the HTTP response
            try {
                ImageIO.write(bufImg, "png", outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        });


        /**
         * Steganography request. This returns an image which has been modified to have a hidden IP address in the
         * file.
         */
        get("/stega", (request, response) -> {

            // Build a string for the IP address we'll be encoding
            String clientIP = request.ip();

            // Prep the HTTP response for an image file.
            OutputStream outputStream = response.raw().getOutputStream();
            response.raw().setContentType("image/png");

            // Open the Buffered Image to allow modification of it's contents
            File inputImageFile = new File(filepath + "dogs.jpg");
            BufferedImage img = null;
            try {
                img = ImageIO.read(inputImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Hide the given IPv4 address into the image
            Steganography.hideIP(img, clientIP);

            // Write the image back to the HTTP response
            try {
                ImageIO.write(img, "png", outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }


            System.out.println("The hidden IP is: " + Steganography.retrieveIP(img));

            return response;
        });


        get("/test", (request, response) -> {

            File imgFile = null;

            imgFile = new File("./stega.png");


            BufferedImage img = ImageIO.read(imgFile);

            Steganography.printPixels(img);

            return response;
        });


        // Retrieve an unmodified dog image
        get("/dogs", (request, response) -> {

            // Prep the HTTP response for an image file.
            OutputStream outputStream = response.raw().getOutputStream();
            response.raw().setContentType("image/png");

            // Open the Buffered Image to allow modification of it's contents
            File inputImageFile = new File(filepath + "dogs.jpg");
            BufferedImage img = null;
            try {
                img = ImageIO.read(inputImageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Write the image back to the HTTP response
            try {
                ImageIO.write(img, "png", outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;

        });


        // The route distributes the decoder to the user
        get("/decoder.jar", (request, response) -> {

            File dec = new File("./decoder.jar");

            /*
            ResourceHandler rh = new ResourceHandler();
            Resource decoder = rh.getResource("./decoder.jar");
            response.raw().setContentType("application/jar");
            */


            response.type("application/jar");

            BufferedOutputStream buffOut = new BufferedOutputStream(response.raw().getOutputStream());

            byte[] byteArr = Files.readAllBytes(dec.toPath());
            buffOut.write(byteArr);
            buffOut.flush();
            buffOut.close();

            return response;
        });


        return true;
    }


    /**
     * This function isn't actually useful in this context. It encodes an image to Base64 so it can be sent easily over
     * HTTP without multiple requests. It works for dynamic modification of the images but is difficult to save
     * the file once it is loaded client side.
     *
     * @param source The filepath to the image to be sent to the client
     * @return Returns a string that can be decoded into an image
     */
    public String imgToBase64(String source) {
        File inputImageFile = new File(source);
        BufferedImage bufImg = null;
        try {
            bufImg = ImageIO.read(inputImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // modify image to test code
        int height = bufImg.getHeight();
        int width = bufImg.getWidth();
        for (int i = 0; i < height / 2; i++) {
            for (int j = 0; j < width / 2; j++) {
                bufImg.setRGB(j, i, bufImg.getRGB(j, i) / 2);
            }
        }

        // create output stream for a byte array
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufImg, "png", outStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read file to byte array
        byte[] byteArr = outStream.toByteArray();

        // create a base 64 encoder and encode the file into a string
        Base64.Encoder encoder = Base64.getEncoder();
        String byteStr = encoder.encodeToString(byteArr);

        // create the output string of the fully encoded image.
        String outStr = "data:image/png;base64," + byteStr;


        return outStr;
    }
}