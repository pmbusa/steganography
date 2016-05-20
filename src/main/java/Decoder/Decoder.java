package Decoder;

import Server.Steganography;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * This is a side app to decode any images that may have leaked. Simply type in the name of the image file and it will
 * be decoded.
 */
public class Decoder {

    public static void main(String[] args) {
        // write your code here

        Scanner sc = new Scanner(System.in);
        while (true) {
            String filepath = "";

            System.out.print("Please enter filename to decode: ");
            while (filepath.length() == 0) {
                filepath = sc.nextLine();
            }

            File imageFile = new File(filepath);
            BufferedImage img = null;
            try {
                img = ImageIO.read(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String hiddenIP = Steganography.retrieveIP(img);

            System.out.println("\nThe hidden ip is: " + hiddenIP);

        }
    }
}
