import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ClienteGUI {

    private JFrame frame;
    private JTextField textFieldExpresion;
    private JTextArea textAreaResultado;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private JButton btnEvaluar;

    public static void main(String[] args) {
//        IntegratedCamera camara = new IntegratedCamera();
//        camara.captureAndRecognize();
        System.out.println( 5 * 3 / 8 + (95 % 5 - 10));
        EventQueue.invokeLater(() -> {
            try {
                ClienteGUI window = new ClienteGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public ClienteGUI() {
        frame = new JFrame();
        frame.setBounds(100, 100, 750, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(1, 3));

        // Algebraic section
        JPanel algebraicPanel = new JPanel();
        algebraicPanel.setBorder(BorderFactory.createTitledBorder("Algebraica"));
        algebraicPanel.setLayout(new BorderLayout());
        textFieldExpresion = new JTextField();
        algebraicPanel.add(textFieldExpresion, BorderLayout.NORTH);

        JPanel algebraicButtonsPanel = new JPanel(new GridLayout(5, 4));
        String[] algebraicButtons = {"7", "8", "9", "+", "4", "5", "6", "-", "1", "2", "3", "*", "/", "(", ")", "**", "0", "00", "000", "%"};
        for (String text : algebraicButtons) {
            JButton button = new JButton(text);
            button.setBackground(Color.BLACK);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> textFieldExpresion.setText(textFieldExpresion.getText() + text));
            algebraicButtonsPanel.add(button);
        }
        algebraicPanel.add(algebraicButtonsPanel, BorderLayout.CENTER);

        btnEvaluar = new JButton("Evaluar");
        btnEvaluar.addActionListener(e -> enviarExpresion());
        algebraicPanel.add(btnEvaluar, BorderLayout.SOUTH);


//        // Logic section input
//        textFieldExpresionLogica = new JTextField();
//        algebraicPanel.add(textFieldExpresionLogica, BorderLayout.CENTER);  // Use CENTER to avoid overlap

        btnEvaluar = new JButton("Evaluar");
        btnEvaluar.addActionListener(e -> enviarExpresion());
        algebraicPanel.add(btnEvaluar, BorderLayout.SOUTH);
        // Logical section
        JPanel logicPanel = new JPanel();
        logicPanel.setBorder(BorderFactory.createTitledBorder("Lógica"));
        logicPanel.setLayout(new BorderLayout());



        // Buttons for logical operations
        JPanel logicButtonsPanel = new JPanel(new GridLayout(3, 2));
        String[] logicButtons = {"AND (&)", "OR (|)", "XOR (^)", "NOT (~)", "1", "0"};
        String[] logicSymbols = {"&", "|", "^", "~", "1", "0"};
        for (int i = 0; i < logicButtons.length; i++) {
            String text = logicButtons[i];
            String symbol = logicSymbols[i];
            JButton button = new JButton(text);
            button.addActionListener(e -> textFieldExpresion.setText(textFieldExpresion.getText() + symbol));
            button.setForeground(Color.white);
            button.setBackground(Color.black);
            logicButtonsPanel.add(button);
        }

        // Adding the buttons to the center of the logical panel
        logicPanel.add(logicButtonsPanel, BorderLayout.CENTER);


     // History and camera section
        JPanel historyCameraPanel = new JPanel();
        historyCameraPanel.setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1));  // 2 rows, 1 column
        JButton btnHistorial = new JButton("Historial");
        btnHistorial.setBackground(Color.BLACK);
        btnHistorial.setForeground(Color.WHITE);
        buttonsPanel.add(btnHistorial);

        // codigo que crea boton para la camara y que cuando termine retorne el string que logro interceptar
        // ver Camara.java
        Camara camara = new Camara();
        JButton btnCamera = new JButton("Cámara");

//        btnCamera.addActionListener( e -> enviarExpresionCamara(camara.captureAndRecognize()));
        buttonsPanel.add(btnCamera);
        btnCamera.setBackground(Color.BLACK);
        btnCamera.setForeground(Color.WHITE);
        historyCameraPanel.add(buttonsPanel, BorderLayout.WEST);

        textAreaResultado = new JTextArea();
        textAreaResultado.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaResultado);
//        scrollPane.setBackground(Color.BLACK);
//        scrollPane.setForeground(Color.WHITE);
        historyCameraPanel.add(scrollPane, BorderLayout.CENTER);



        frame.getContentPane().add(algebraicPanel);
        frame.getContentPane().add(logicPanel);
        frame.getContentPane().add(historyCameraPanel);
    }

    private void enviarExpresion() {
        // Determine from which textField the expression is coming from (Algebraic or Logic)
        String expresion;
        expresion = textFieldExpresion.getText();


        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(expresion);
            String resultado = in.readLine();
            textAreaResultado.setText("Resultado de " + expresion + ":\n" + resultado);

        } catch (IOException e) {
            e.printStackTrace();
            textAreaResultado.setText("Error al conectarse al servidor.");
        }
    }

    /**
     * Esto se va ejecutar utilizando el input que le dio la camara despues de hacer el OCR
     * @param expresion la expresion hecha un string
     */
    private void enviarExpresionCamara(String expresion){

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(expresion);
            String resultado = in.readLine();
            textAreaResultado.setText("Resultado de " + expresion + ":\n" + resultado);

        } catch (IOException e) {
            e.printStackTrace();
            textAreaResultado.setText("Error al conectarse al servidor.");
        }
    }
}
