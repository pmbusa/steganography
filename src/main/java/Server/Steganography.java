package Server;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

/**
 * Created by Paul Mbusa
 * <p>
 * Basic color pixels are represented at a combination of a red, green and blue, each of which is in the range of
 * 0 - 255. This leads to a 2-digit representation of each color in hexcode. By modifying the least significant digit
 * of each color, the individual pixel change wont differ too much from the original image. This allows one to encode
 * data within the pixel which under an average inspection won't bring up any red flags. This project takes advantage
 * of this to encode data within the different pixels.
 * <p>
 * Given the constraints of finals, I don't have time to flesh this out as much as I would have liked, this version
 * will only encode an IP address into the picture but theoretically you caould store any kind of string you like.
 */
public class Steganography {

    /**
     * This function stores one integer of data in the range of 0 to 16, in one pixel of an image.
     *
     * @param img  The buffered image to modify
     * @param x    The x coordinate of the pixel to modify
     * @param y    The y coordinate of the pixel to modify
     * @param data The integer you wish to store
     */
    public static void setPixel(BufferedImage img, int x, int y, int data) {

        // resources needed to work with pixels
        ColorModel cm = img.getColorModel();
        Random rand = new Random();
        //int pixel = img.getRGB(x, y);

        Color col = new Color(img.getRGB(x, y), cm.hasAlpha());

        // pull the original data values of the pixel

        int redVal = col.getRed();
        int greenVal = col.getGreen();
        int blueVal = col.getBlue();
        int alphaVal = col.getAlpha();

        // calculate the randomized values for hiding the data
        int redSub = rand.nextInt(data + 1);
        int greenSub = rand.nextInt(data + 1 - redSub);
        int blueSub = data - redSub - greenSub;


        // Clear out the least significant digit to prep for hiding data
        redVal = (redVal / 16) * 16;
        greenVal = (greenVal / 16) * 16;
        blueVal = (blueVal / 16) * 16;


        // Fill the true pixel values with hidden data
        redVal += redSub;
        greenVal += greenSub;
        blueVal += blueSub;
        if (alphaVal <= 0) alphaVal = 1;


        // Set the pixel to the new value of with the hidden data in it
        Color c = new Color(redVal, greenVal, blueVal, alphaVal);
        img.setRGB(x, y, c.getRGB());


    }

    /**
     * This function stores one integer of data in the range of 0 to 16 in one pixel of an image. The supplied
     * PixelCoordinate object is also advanced to the next position.
     *
     * @param img   The buffered image to modify
     * @param coord A PixelCoordinate object used to keep track of the current location
     * @param data  The integer you wish to store
     */
    public static void setPixel(BufferedImage img, PixelCoordinate coord, int data) {
        setPixel(img, coord.x, coord.y, data);
        coord.nextPixel();
    }


    /**
     * This function sets the entry point for the hidden data. A decoder searches for this pattern and when it is
     * found, the resulting secret content can be revealed.
     *
     * @param img   The buffered image to modify
     * @param coord A PixelCoordinate object used to keep track of the current location
     */
    public static void setHeader(BufferedImage img, PixelCoordinate coord) {

        // Set the first five pixels to a value of 11
        for (int i = 0; i < 5; i++) {
            setPixel(img, coord, 11);

        }

        // Set the next five pixels to a value of 12
        for (int i = 5; i < 10; i++) {
            setPixel(img, coord, 12);
        }
    }

    /**
     * * This function sets the termination pattern of the hidden data. Currently it is unused due to only one type
     * of information able to be encoded, an IPv4 address. I think it works though
     *
     * @param img   The buffered image to modify
     * @param coord A PixelCoordinate object used to keep track of the current location
     * @return Returns true for successfully setting the delimiter and false for a failed attempt
     */
    public static boolean setDelimiter(BufferedImage img, PixelCoordinate coord) {
        int maxWidth = img.getWidth();
        int maxHeight = img.getHeight();
        int pixelCount = maxHeight * maxWidth;


        // Verify that we're in bounds for the delimiter
        // This isn't really needed in this new versio but it has future applications
        int remainingPixels = pixelCount - (maxWidth * coord.y) - coord.x;
        if (pixelCount - remainingPixels > 5) {

            return false;
        }


        // set the next five pixels to 13 to signify the end of the hidden data
        for (int i = 0; i < 5; i++) {

            System.out.println("Set pixel (" + coord.x + "," + coord.y + ") to value 13");
            setPixel(img, coord, 13);
        }
        return true;
    }

    /**
     * This is a wrapper function for the more explicit form of setDelimiter(). It allows for the use of a
     * PixelCoordinate object to streamline implementation
     *
     * @param img    The buffered image to modify
     * @param xStart The X-coordinate of where to begin the delimination pattern
     * @param yStart The Y-coordinate of where to begin the delimination pattern
     * @return This is unused at the moment.
     */
    public static boolean setDelimiter(BufferedImage img, int xStart, int yStart) {
        PixelCoordinate coord = new PixelCoordinate(img, xStart, yStart);

        return setDelimiter(img, coord);
    }

    /**
     * The most important function of them all! Pass a BufferedImage and an IPv4 address in String form to this
     * function and it will be encoded into the image file. The color values of the pixels are modified to sum up to
     * individual digits in the IP address. The color values are slightly randomized such that two images encoded
     * with the same information might not have the same exact color values. In a more developed form, this could make
     * detecting the steganography more difficult.
     * <p>
     * Time constraints limit this to only IPv4 for now. REQUIRES the use of 127.0.0.1 in the web browser unless
     * the browser is configured to use strictly IPv4
     *
     * @param img   The buffered image to modify
     * @param ipStr The IPv4 address in a standard string. This will be encoded into the image
     * @return This is unused at the moment.
     */
    public static boolean hideIP(BufferedImage img, String ipStr) {

        // pull out each individual octet from the ip address
        int octet[] = new int[4];
        for (int i = 0; ipStr.contains("."); i++) {
            String subIP = ipStr.substring(0, ipStr.indexOf('.'));
            octet[i] = Integer.parseInt(subIP);

            ipStr = ipStr.substring(ipStr.indexOf('.') + 1);
        }
        octet[3] = Integer.parseInt(ipStr);


        // Encode the IP into the image in a random location.
        Random rand = new Random();


        int x;
        int y;
        int pixelCount = img.getHeight() * img.getWidth();

        while (true) {
            x = rand.nextInt(img.getWidth());
            y = rand.nextInt(img.getHeight());

            // Exit on: total pixel count has enough space from start to end so it doesn't overflow

            System.out.println("x: " + x + ", y: " + y);

            int remainingPixels = pixelCount - (y * img.getWidth()) - x;

            if (remainingPixels > 30) {
                break;
            }


        }

        PixelCoordinate coord = new PixelCoordinate(img, x, y);

        // set the header.
        setHeader(img, coord);

        // Set the ip address into the pixels
        for (int i = 0; i < 4; i++) {
            int hundreds = octet[i] / 100;
            setPixel(img, coord, hundreds);

            int tens = (octet[i] % 100) / 10;
            setPixel(img, coord, tens);

            int ones = octet[i] % 10;
            setPixel(img, coord, ones);
        }


        // Set the delimiter
        if (setDelimiter(img, coord)) {
            System.out.println("Set the delimteterererer");
        }

        return true;
    }

    /**
     * This is the opposite of hideIP(). This function pulls out the secret data stored in the image and returns it as
     * a String. Right now the function is hard coded only to detect an IPv4 address and will fail for anything else.
     *
     * @param img The buffered image to modify
     * @return Returns a string containing the IP that was hidden in the image. Otherwise it returns a null string
     */
    public static String retrieveIP(BufferedImage img) {

        PixelCoordinate coord = new PixelCoordinate(img);

        boolean foundSecret = false;

        while (!foundSecret && coord.inBounds()) {
            PixelCoordinate tmpCoord = new PixelCoordinate(img, coord.x, coord.y);

            if (getPixelData(img, coord) == 11) {

                // flag to avoid second check
                boolean stillValid = true;

                for (int i = 0; i < 5; i++) {
                    // check if the current pixel is valid
                    if (getPixelData(img, tmpCoord) != 11) {
                        stillValid = false;
                        break;
                    }
                    // advance to the next pixel
                    tmpCoord.nextPixel();
                }

                if (stillValid) {
                    for (int i = 0; i < 5; i++) {
                        if (getPixelData(img, tmpCoord) != 12) {
                            stillValid = false;
                        }
                        tmpCoord.nextPixel();
                    }

                    // If we've passed all the tests and made it this far, the secret begins at the current pixel in
                    // tmpCoord
                    if (stillValid) {
                        foundSecret = true;
                    }
                }
            }

            // Set the coordinate marker for the appropriate following action
            if (foundSecret) {
                coord.x = tmpCoord.x;
                coord.y = tmpCoord.y;
                break;
            } else {

                try {
                    coord.nextPixel();
                }
                catch (IndexOutOfBoundsException e) {
                    return "";
                }
            }
        }


        if (!coord.inBounds()) {
            // The pixel coordinate is out of bounds, there is nothing to find
            System.out.println("The search for secrets has failed");
            return "Found nothing!";
        }


        // Build the output string for the IP address
        String outStr = "";

        for (int j = 0; j < 4; j++) {
            int octet = 0;

            // restore hundreds
            int digit = getPixelData(img, coord);
            digit = digit * 100;
            octet += digit;
            coord.nextPixel();

            // restore tens
            digit = getPixelData(img, coord);
            digit = digit * 10;
            octet += digit;
            coord.nextPixel();

            // restore ones
            digit = getPixelData(img, coord);
            octet += digit;
            coord.nextPixel();

            outStr = outStr + Integer.toString(octet) + ".";

        }

        // delete the last period from the ip
        if (outStr.contains(".")) {
            outStr = outStr.substring(0, outStr.lastIndexOf('.'));
        }

        return outStr;

    }

    /**
     * This function will retrieve a single Int stored within a specified pixel of the supplied image
     *
     * @param img    The buffered image to modify
     * @param xCoord The X-coordinate of the pixel to be inspected
     * @param yCoord The Y-coordinate of the pixel to be inspected
     * @return The integer data stored within the specified pixel
     */
    public static int getPixelData(BufferedImage img, int xCoord, int yCoord) {

        // resources needed to work with pixels
        ColorModel cm = img.getColorModel();

        int pixel = img.getRGB(xCoord, yCoord);
        Color col = new Color(img.getRGB(xCoord, yCoord), cm.hasAlpha());

        // pull the original data values of the pixel
        int redVal = col.getRed();
        int greenVal = col.getGreen();
        int blueVal = col.getBlue();
        int alphaVal = col.getAlpha();

        redVal = redVal % 16;
        greenVal = greenVal % 16;
        blueVal = blueVal % 16;

        int data = redVal + greenVal + blueVal;

        return data;

    }

    /**
     * A wrapper for the more explicit getPixelData() class, allows for the use of a PixelCoordinate object
     *
     * @param img   The buffered image to modify
     * @param coord A PixelCoordinate object used to keep track of the current location
     * @return The integer data stored within the specified pixel
     */
    public static int getPixelData(BufferedImage img, PixelCoordinate coord) {
        if (!coord.inBounds()) {
            return -1;
        }
        return getPixelData(img, coord.x, coord.y);
    }


    public static void printPixels(BufferedImage img) {
        int height = img.getHeight();
        int width = img.getWidth();
        int size = height * width;

        File outFile = new File("./pixeldata.txt");
        PrintStream ps = null;
        try {
            ps = new PrintStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int data = getPixelData(img, x, y);
                int pixelNum = offset + x;

                ps.printf("Pixel %3d == %2d%n", pixelNum, data);
            }
        }

        ps.close();

    }
}


/**
 * PixelCoordinate is a helper class for the Steganography class. It's main purpose is to keep track of the
 * current position being operated on from within the Steganography operations. It also holds a little information
 * about the image and can properly advance to the next pixel in the image
 */
class PixelCoordinate {

    /**
     * Default constructor. Sets the coordinate value to (0,0).
     *
     * @param image The buffered image to that is being inspected
     */
    PixelCoordinate(BufferedImage image) {
        img = image;

        x = 0;
        y = 0;
        width = image.getWidth();
        height = image.getHeight();

    }

    /**
     * @param image  The buffered image to that is being inspected
     * @param xCoord The X-coordinate of the pixel to be inspected
     * @param yCoord The Y-coordinate of the pixel to be inspected
     */
    PixelCoordinate(BufferedImage image, int xCoord, int yCoord) {
        img = image;
        x = xCoord;
        y = yCoord;
        width = image.getWidth();
        height = image.getHeight();
        //System.out.println(image.toString());
    }


    BufferedImage img;
    int x;
    int y;
    int width;
    int height;

    /**
     * This function simply moves the PixelCoordinate object to the next pixel location. It handles wraparounds over
     * image boundaries properly
     *
     * @return
     */
    public boolean nextPixel() {
        //System.out.println("X: " + Integer.toString(x) + ", Y: " + Integer.toString(y) + ", advancing to ");
        if (x >= width - 1) {
            x = 0;
            y++;
            if (y >= height) {
                throw new IndexOutOfBoundsException("Coordinates went off the image");
            }
        } else {
            x++;
        }
        //System.out.println("X: " + x + ", Y: " + y + ".");
        return true;
    }

    /**
     * This function returns a boolean regarding if the current location of the pixel is in bounds or not
     *
     * @return
     */
    public boolean inBounds() {
        if (height < y) return false;
        if (width < x) return false;
        return true;
    }

}