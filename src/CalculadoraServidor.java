import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Stack;

public class CalculadoraServidor {

    private static final int PUERTO = 8080;
    static class NodoExpresion {
        String dato;
        NodoExpresion izquierdo, derecho;

        NodoExpresion(String dato) {
            this.dato = dato;
            izquierdo = derecho = null;
        }
    }
    static class ArbolExpresion {
        NodoExpresion raiz;

        public ArbolExpresion() {
            this.raiz = null;
        }

        // Función para construir el árbol a partir de una expresión en notación postfija
        public void construirDesdePostfija(String postfija) {
            Stack<NodoExpresion> pila = new Stack<>();
            for (char c : postfija.toCharArray()) {
                if (Character.isDigit(c)) {
                    pila.push(new NodoExpresion(String.valueOf(c)));
                } else {
                    NodoExpresion nodo = new NodoExpresion(String.valueOf(c));
                    nodo.derecho = pila.pop();
                    nodo.izquierdo = pila.pop();
                    pila.push(nodo);
                }
            }
            this.raiz = pila.pop();
        }

        // Función para evaluar el árbol
        public double evaluar() {
            return evaluarRecursivo(raiz);
        }

        private double evaluarRecursivo(NodoExpresion nodo) {
            if (nodo == null) return 0;

            if (nodo.izquierdo == null && nodo.derecho == null) {
                return Double.parseDouble(nodo.dato);
            }

            double izquierdo = evaluarRecursivo(nodo.izquierdo);
            double derecho = evaluarRecursivo(nodo.derecho);

            switch (nodo.dato.charAt(0)) {
                case '+': return izquierdo + derecho;
                case '-': return izquierdo - derecho;
                case '*': return izquierdo * derecho;
                case '/': return izquierdo / derecho;
            }
            return 0; // Para simplificar, solo estamos manejando operadores básicos por ahora.
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                new Thread(new ClienteHandler(clienteSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClienteHandler implements Runnable {
        private Socket clienteSocket;

        public ClienteHandler(Socket socket) {
            this.clienteSocket = socket;
        }
        private double evaluarExpresion(String expresion) {
            ArbolExpresion arbol = new ArbolExpresion();
            String postfija = convertirApostfija(expresion);
            arbol.construirDesdePostfija(postfija);
            return arbol.evaluar();
        }

       @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                 PrintWriter salida = new PrintWriter(clienteSocket.getOutputStream(), true)) {

                String expresion = entrada.readLine();

                double resultado = evaluarExpresion(expresion);
                salida.println(resultado);

                // Registrar en el CSV
                registrarEnCSV(expresion, String.valueOf(resultado));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void registrarEnCSV(String expresion, String resultado) {
            String archivo = "operaciones.csv";
            try (FileWriter fw = new FileWriter(archivo, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                String registro = new Date() + "," + expresion + "," + resultado;
                out.println(registro);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private String convertirApostfija(String infija) {
            StringBuilder postfija = new StringBuilder();
            Stack<Character> pila = new Stack<>();

            for (char c : infija.toCharArray()) {
                switch (c) {
                    case '+':
                    case '-':
                        while (!pila.isEmpty() && (pila.peek() == '*' || pila.peek() == '/' || pila.peek() == '+' || pila.peek() == '-')) {
                            postfija.append(pila.pop());
                        }
                        pila.push(c);
                        break;
                    case '*':
                    case '/':
                        while (!pila.isEmpty() && (pila.peek() == '*' || pila.peek() == '/')) {
                            postfija.append(pila.pop());
                        }
                        pila.push(c);
                        break;
                    case '(':
                        pila.push(c);
                        break;
                    case ')':
                        while (!pila.isEmpty() && pila.peek() != '(') {
                            postfija.append(pila.pop());
                        }
                        pila.pop();  // Remover '('
                        break;
                    default:
                        postfija.append(c);
                        break;
                }
            }

            while (!pila.isEmpty()) {
                postfija.append(pila.pop());
            }

            return postfija.toString();
        }
    }
}
