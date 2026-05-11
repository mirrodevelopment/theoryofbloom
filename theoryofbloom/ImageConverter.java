import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ImageConverter {
    public static void main(String[] args) throws Exception {
        File inputFile = new File("c:/Users/girit/Downloads/theoryofbloom/theoryofbloom/src/main/resources/static/images/flower-logo.png");
        BufferedImage img = ImageIO.read(inputFile);
        for(int y=0; y<img.getHeight(); y++){
            for(int x=0; x<img.getWidth(); x++){
                int p = img.getRGB(x,y);
                int a = (p>>24)&0xff;
                if(a > 0) {
                    p = (a<<24) | (255<<16) | (255<<8) | 255;
                    img.setRGB(x, y, p);
                }
            }
        }
        File outputFile = new File("c:/Users/girit/Downloads/theoryofbloom/theoryofbloom/src/main/resources/static/images/flower-logo-white.png");
        ImageIO.write(img, "png", outputFile);
        System.out.println("Converted to " + outputFile.getAbsolutePath());
    }
}
