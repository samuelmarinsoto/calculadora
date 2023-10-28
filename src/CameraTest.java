import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;


public class CameraTest {

    static {
System.load("C:\\Users\\Isaac\\Downloads\\opencv\\build\\java\\x64\\opencv_java480.dll");
    }

    public static void main(String[] args) {
        VideoCapture camera = new VideoCapture(0); // Intenta abrir la cámara con índice 0

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not found!");
            return;
        } else {
            System.out.println("Camera opened successfully!");
        }

        Mat frame = new Mat();
        String windowName = "Camera Test";

        while (true) {
            if (camera.read(frame)) {
                HighGui.imshow(windowName, frame);
                char key = (char) HighGui.waitKey(20);
                if (key == 27) { // ESC key
                    break;
                }
            } else {
                System.out.println("Failed to capture frame!");
            }
        }

        camera.release();
        HighGui.destroyAllWindows();
    }
}
