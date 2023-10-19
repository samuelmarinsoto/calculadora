import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ClienteGUI {

    private JFrame frame;
    private JTextField textFieldExpresion;
    private JTextArea textAreaResultado;
    private JButton btnEvaluar;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
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
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        textFieldExpresion = new JTextField();
        textFieldExpresion.setBounds(10, 11, 414, 20);
        frame.getContentPane().add(textFieldExpresion);
        textFieldExpresion.setColumns(10);

        btnEvaluar = new JButton("Evaluar");
        btnEvaluar.addActionListener(e -> enviarExpresion());
        btnEvaluar.setBounds(335, 42, 89, 23);
        frame.getContentPane().add(btnEvaluar);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 76, 414, 174);
        frame.getContentPane().add(scrollPane);

        textAreaResultado = new JTextArea();
        textAreaResultado.setEditable(false);
        scrollPane.setViewportView(textAreaResultado);
    }

    private void enviarExpresion() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String expresion = textFieldExpresion.getText();
            out.println(expresion);
            String resultado = in.readLine();
            textAreaResultado.setText("Resultado de " + expresion + ":\n" + resultado);

        } catch (IOException e) {
            e.printStackTrace();
            textAreaResultado.setText("Error al conectarse al servidor.");
        }
    }
}
