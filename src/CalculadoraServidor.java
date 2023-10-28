import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;
public class CalculadoraServidor {
    private static final int PUERTO = 8080;
    private static JTextArea textAreaLog;  // Área de registro para mostrar el análisis

    public static void main(String[] args) {
        // Inicializando la GUI
        initGUI();

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

    private static void initGUI() {
        JFrame frame = new JFrame("Servidor - Análisis de Árbol de Expresión");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textAreaLog = new JTextArea();
        textAreaLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaLog);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

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

        private boolean esOperador(String token) {
            return "+-*/%&|^~()**".contains(token) || "**".equals(token);
        }


        // Función para construir el árbol a partir de una expresión en notación postfija
        public void construirDesdePostfija(String postfija) {
            Stack<NodoExpresion> pila = new Stack<>();
            String[] tokens = postfija.split(" ");

            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
            if (esOperador(token)) {
                NodoExpresion nodo = new NodoExpresion(token);
                nodo.derecho = (pila.isEmpty()) ? null : pila.pop();
                nodo.izquierdo = (pila.isEmpty()) ? null : pila.pop();
                pila.push(nodo);
                System.out.println("Empujando nodo operador: " + nodo.dato + ", Izquierdo: " + (nodo.izquierdo != null ? nodo.izquierdo.dato : "null") + ", Derecho: " + (nodo.derecho != null ? nodo.derecho.dato : "null"));  // <-- AQUÍ
            } else {
                pila.push(new NodoExpresion(token));
                System.out.println("Empujando nodo valor: " + token);  // <-- AQUÍ
            }

            }
            this.raiz = pila.pop();
        }



        // Función para evaluar el árbol
        public double evaluar() {
            return evaluarRecursivo(raiz);
        }

        protected double evaluarRecursivo(NodoExpresion nodo) {
            return 0.0;
        }
    }

    static class ArbolAlgebraico extends ArbolExpresion {
        private StringBuilder trazabilidad;

        public ArbolAlgebraico() {
            this.trazabilidad = new StringBuilder();
        }

        @Override
        protected double evaluarRecursivo(NodoExpresion nodo) {
            if (nodo == null) return 0;

            if (nodo.izquierdo == null && nodo.derecho == null) {
                return Double.parseDouble(nodo.dato);
            }

            double izquierdo = evaluarRecursivo(nodo.izquierdo);
            double derecho = evaluarRecursivo(nodo.derecho);
            System.out.println("Nodo actual: " + nodo.dato + ", Izquierdo: " + izquierdo + ", Derecho: " + derecho);  // <-- AQUÍ
            trazabilidad.append("paso ").append(trazabilidad.toString().split("\n").length).append(": ");
            trazabilidad.append("(").append(izquierdo).append(" ").append(nodo.dato).append(" ").append(derecho).append(")").append("\n");

            switch (nodo.dato) {
                case "+":
                    System.out.println("Sumando...");  // <-- AQUÍ
                    return izquierdo + derecho;
                case "-":
                    System.out.println("Restando...");  // <-- AQUÍ
                    return izquierdo - derecho;
                case "*":
                    System.out.println("Multiplicando...");  // <-- AQUÍ
                    return izquierdo * derecho;
                case "^":
                case "**":
                    System.out.println("Elevando al poder...");  // <-- AQUÍ
                    return Math.pow(izquierdo, derecho);
                case "/":
                    return izquierdo / derecho;
                case "%":
                    return izquierdo % derecho;
//                case "^":  // Manejo de XOR
//                    return (int)izquierdo ^ (int)derecho;
                // ... otros casos
            }



            throw new IllegalArgumentException("Operador no soportado: " + nodo.dato);
        }

        public String getTrazabilidad() {
            return trazabilidad.toString();
        }
    }


    static class ArbolLogico extends ArbolExpresion {
        @Override
        protected double evaluarRecursivo(NodoExpresion nodo) {
            if (nodo == null) return 0;

            if (nodo.izquierdo == null && nodo.derecho == null) {
                return Double.parseDouble(nodo.dato);
            }

            double izquierdo = evaluarRecursivo(nodo.izquierdo);
            double derecho = evaluarRecursivo(nodo.derecho);

            if (izquierdo != 0.0 && izquierdo != 1.0) {
                throw new IllegalArgumentException("Operaciones lógicas solo permitidas con 0 y 1.");
            }
            if (derecho != 0.0 && derecho != 1.0) {
                throw new IllegalArgumentException("Operaciones lógicas solo permitidas con 0 y 1.");
            }

            switch (nodo.dato) {
                case "&":
                    return (int) izquierdo & (int) derecho;
                case "|":
                    return (int) izquierdo | (int) derecho;
                case "^":
                    return (int) izquierdo ^ (int) derecho;
                case "~":
                    return (derecho == 1) ? 0 : 1;
                default:
                    throw new IllegalArgumentException("Operador no soportado: " + nodo.dato);
            }
        }
    }

    private static class ClienteHandler implements Runnable {
            private Socket clienteSocket;

            public ClienteHandler(Socket socket) {
                this.clienteSocket = socket;
            }

            private int prioridad(char operador) {
                switch (operador) {
                    case '+':
                    case '-':
                        return 1;
                    case '*':
                    case '/':
                    case '%':
                        return 2;
                    case '^':
                        return 3;
                    case '&':
                    case '|':
                    case '~':
                        return 4;
                }
                return -1;
            }

            @Override
            public void run() {
                try (BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                     PrintWriter salida = new PrintWriter(clienteSocket.getOutputStream(), true)) {

                    String expresion = entrada.readLine();
                    System.out.println("Recibida expresión: " + expresion);


                    if (!esEntradaValida(expresion)) {
                        salida.println("Error: La expresión no puede contener operadores algebraicos y lógicos al mismo tiempo.");
                        return;
                    }

                    System.out.println("Evaluando expresión: " + expresion);
                    double resultado = evaluarExpresion(expresion);
                    System.out.println("Resultado obtenido: " + resultado);

                    salida.println(resultado);

                    // Registrar en el CSV
                    System.out.println("Registrando en el CSV: " + expresion + ", " + resultado);
                    registrarEnCSV(expresion, String.valueOf(resultado));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private double evaluarExpresion(String expresion) {
                textAreaLog.append("Convirtiendo expresión a postfija...\\n");
                String postfija = convertirApostfija(expresion);
                System.out.println("Notación postfija: " + postfija);
                textAreaLog.append("Expresión postfija: " + postfija + "\\n");
                boolean valid = esEntradaValida(expresion);
                System.out.println("Es entrada válida: " + valid);
                ArbolExpresion arbol;

                if (expresion.contains("&") || expresion.contains("|") || expresion.contains("^") || expresion.contains("~")) {
                    arbol = new ArbolLogico();
                    System.out.println("Usando ArbolLogico");  // <-- AQUÍ
                } else {
                    arbol = new ArbolAlgebraico();
                    System.out.println("Usando ArbolAlgebraico");  // <-- AQUÍ
                }


                arbol.construirDesdePostfija(postfija);
                System.out.println("Árbol construido desde postfija");
                try {
                    System.out.println("Evaluando el árbol...");  // <-- AQUÍ
                    double result = arbol.evaluar();
                    if (arbol instanceof ArbolAlgebraico) {
                        System.out.println(((ArbolAlgebraico) arbol).getTrazabilidad());
                    }
                    textAreaLog.append("Resultado de evaluar el árbol: " + result + "\n");
                    return result;
                } catch (IllegalArgumentException e) {
                    System.out.println("Error durante la evaluación: " + e.getMessage());  // <-- AQUÍ
                    textAreaLog.append(e.getMessage() + "\\n");
                    throw e;
                }
            }

            private boolean esEntradaValida(String expresion) {
                boolean contieneOperadoresLogicos = expresion.contains("&") || expresion.contains("|") || (expresion.contains("^") && !expresion.contains("**")) || expresion.contains("~");
                boolean contieneOperadoresAlgebraicos = expresion.contains("+") || expresion.contains("-") || expresion.contains("*") || expresion.contains("/") || expresion.contains("%") || expresion.contains("**");

                if (contieneOperadoresLogicos && contieneOperadoresAlgebraicos) {
                    return false;
                }
                return true;
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
            StringBuilder postfijaBuilder = new StringBuilder();
            Queue<String> postfija = new LinkedList<>();
            Stack<Character> pila = new Stack<>();
            for (int i = 0; i < infija.length(); i++) {
                char c = infija.charAt(i);

                if (Character.isWhitespace(c)) {  // Ignorar espacios
                    continue;
                }

                if (Character.isDigit(c) || c == '.') {
                    StringBuilder numero = new StringBuilder();
                    while (i < infija.length() && (Character.isDigit(infija.charAt(i)) || infija.charAt(i) == '.')) {
                        numero.append(infija.charAt(i++));
                    }
                    postfija.add(numero.toString());
                    i--;
                } else if (c == '(') {
                    pila.push(c);
                } else if (c == ')') {
                    while (!pila.isEmpty() && pila.peek() != '(') {
                        postfija.add(String.valueOf(pila.pop()));
                    }
                    if (!pila.isEmpty()) pila.pop();
                } else {
                    if (c == '*' && i + 1 < infija.length() && infija.charAt(i + 1) == '*') {
                        i++;
                        pila.push('^');
                    } else {
                        while (!pila.isEmpty() && prioridad(pila.peek()) >= prioridad(c)) {
                            postfija.add(String.valueOf(pila.pop()));
                        }
                        pila.push(c);
                    }
                }
            }
            while (!pila.isEmpty()) {
                char operador = pila.pop();
                if (operador == '^') {
                    postfija.add("**");
                } else {
                    postfija.add(String.valueOf(operador));
                }
            }

            while (!postfija.isEmpty()) {
                postfijaBuilder.append(postfija.poll()).append(' ');
            }

            return postfijaBuilder.toString().trim();
        }


    }
}
