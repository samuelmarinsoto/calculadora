import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import net.sourceforge.tess4j.*;
import java.io.File;

public class IntegratedCamera {

    static {
        System.load("C:\\Users\\Isaac\\Downloads\\opencv\\build\\java\\x64\\opencv_java480.dll");
    }

    public static void main(String[] args) {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Error: Camera not found!");
            return;
        } else {
            System.out.println("Camera opened successfully!");
        }

        Mat frame = new Mat();
        String windowName = "Press 'c' to capture";

        String ocrResult = "Error: No image captured.";

        while (true) {
            if (camera.read(frame)) {
                HighGui.imshow(windowName, frame);
                char key = (char) HighGui.waitKey(20);
                if (key == 'c' || key == 'C') {
                    String filePath = "imagen.jpg";
                    Imgcodecs.imwrite(filePath, frame);
                    System.out.println("Image captured and saved as " + filePath);

                    ocrResult = performOCR(filePath);
                    System.out.println(ocrResult);
                    break;
                } else if (key == 27) { // ESC key
                    ocrResult = "Capture cancelled.";
                    System.out.println(ocrResult);
                    break;
                }
            } else {
                System.out.println("Failed to capture frame!");
            }
        }

        camera.release();
        HighGui.destroyAllWindows();
    }

    private static String performOCR(String filePath) {
        ITesseract instance = new Tesseract();
        instance.setDatapath(""); // Replace with the path to your tessdata directory
        instance.setLanguage("equ"); // Set the language as English. Modify if needed.

        try {
            String result = instance.doOCR(new File(filePath));
            return result.trim();
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            return "Error while performing OCR.";
        }
    }
}
