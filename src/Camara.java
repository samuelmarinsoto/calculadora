import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.Imgcodecs;
import net.sourceforge.tess4j.*;
import org.opencv.highgui.HighGui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Camara {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    static {
        System.load("C:\\Users\\Isaac\\Downloads\\opencv\\build\\java\\x64\\opencv_java480.dll");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Camera OCR");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JButton captureButton = new JButton("Capture & Recognize");
        captureButton.setBounds(100, 50, 200, 30);
        JTextArea textArea = new JTextArea();
        textArea.setBounds(50, 100, 300, 100);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setCaretPosition(textArea.getDocument().getLength());

        frame.add(captureButton);
        frame.add(textArea);

        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Camara camara = new Camara();
                String recognizedText = camara.captureAndRecognize();
                String texto_resultado = camara.enviarExpresionCamara(recognizedText);
                textArea.setText(texto_resultado);
            }
        });

        frame.setVisible(true);
    }

    public String captureAndRecognize() {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
        System.out.println("Error: Camera not found!");
        return "Camera Error!";
    } else {
        System.out.println("Camera opened successfully!");
    }


        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

        Mat frame = new Mat();

        String windowName = "Press 'c' to capture";
        HighGui.namedWindow(windowName);

        String ocrResult = "Error: No image captured.";

        while (true) {
            if (camera.read(frame)) {
                Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2BGR);
                System.out.println("Frame captured successfully!");
                org.opencv.highgui.HighGui.imshow(windowName, frame);
                System.out.println(frame.size());
                System.out.println(frame.type());
                char key = (char) org.opencv.highgui.HighGui.waitKey(20);
                if (key == 'c' || key == 'C') {
                    String filePath = "imagen.jpg";
                    Imgcodecs.imwrite(filePath, frame);
                    System.out.println("Image captured and saved as " + filePath);

                    ocrResult = performOCR(filePath);
                    break;
                } else if (key == 27) { // ESC key
                    ocrResult = "Capture cancelled.";
                    break;
                }
            } else {
                System.out.println("Failed to capture frame!");
            }
        }

        camera.release();
        HighGui.destroyAllWindows();

        return ocrResult;
    }

    private String performOCR(String filePath) {
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
    /**
     * Esto se va ejecutar utilizando el input que le dio la camara despues de hacer el OCR
     * @param expresion la expresion hecha un string
     */
    private String enviarExpresionCamara(String expresion) {

        String resultado = null;
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(expresion);
            resultado = in.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
