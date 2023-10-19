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
            String[] tokens = postfija.split(" "); // Separamos por espacio

            for (String token : tokens) {
                if (esOperador(token)) {
                    NodoExpresion nodo = new NodoExpresion(token);
                    if (token.equals("~")) { // Si es el operador NOT
                        nodo.derecho = (pila.isEmpty()) ? null : pila.pop();
                        nodo.izquierdo = null;
                    } else {
                        nodo.derecho = (pila.isEmpty()) ? null : pila.pop();
                        nodo.izquierdo = (pila.isEmpty()) ? null : pila.pop();
                    }
                    pila.push(nodo);
                } else {
                    pila.push(new NodoExpresion(token));
                }
            }
            this.raiz = pila.pop();
        }

        private boolean esOperador(String token) {
            return "+-*/%&|^~".contains(token);
        }


        // Función para evaluar el árbol
        public double evaluar() {
            return evaluarRecursivo(raiz);
        }
        protected double evaluarRecursivo(NodoExpresion nodo) {
            // Este método lo redefiniremos en las clases hijas para tener comportamientos específicos
            return 0.0;
        }
//        private double evaluarRecursivo(NodoExpresion nodo) {
//            if (nodo == null) return 0;
//
//            if (nodo.izquierdo == null && nodo.derecho == null) {
//                return Double.parseDouble(nodo.dato);
//            }
//
//            double izquierdo = evaluarRecursivo(nodo.izquierdo);
//            double derecho = evaluarRecursivo(nodo.derecho);
//
//            switch (nodo.dato.charAt(0)) {
//                case '+':
//                    return izquierdo + derecho;
//                case '-':
//                    return izquierdo - derecho;
//                case '*':
//                    if (nodo.dato.length() > 1 && nodo.dato.equals("**")) {
//                        return Math.pow(izquierdo, derecho);
//                    }
//                    return izquierdo * derecho;
//                case '/':
//                    return izquierdo / derecho;
//                case '%':
//                    return izquierdo % derecho;
//                case '&':
//                    return (int) izquierdo & (int) derecho;
//                case '|':
//                    return (int) izquierdo | (int) derecho;
//                case '^':
//                    return (int) izquierdo ^ (int) derecho;
//                case '~':
//                    return ~(int) izquierdo; // Considerando que es un operador unario
//            }
//            return 0;  // Esto debería manejarse mejor, tal vez lanzando una excepción
//        }
    }
    static class ArbolAlgebraico extends ArbolExpresion {
        @Override
        protected double evaluarRecursivo(NodoExpresion nodo) {
            if (nodo == null) return 0;

            if (nodo.izquierdo == null && nodo.derecho == null) {
                return Double.parseDouble(nodo.dato);
            }

            double izquierdo = evaluarRecursivo(nodo.izquierdo);
            double derecho = evaluarRecursivo(nodo.derecho);

            switch (nodo.dato.charAt(0)) {
                case '+':
                    return izquierdo + derecho;
                case '-':
                    return izquierdo - derecho;
                case '*':
                    if (nodo.dato.length() > 1 && nodo.dato.equals("**")) {
                        return Math.pow(izquierdo, derecho);
                    }
                    return izquierdo * derecho;
                case '/':
                    return izquierdo / derecho;
                case '%':
                    return izquierdo % derecho;
            }
            throw new IllegalArgumentException("Operador no soportado: " + nodo.dato);
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
            System.out.println("Nodo actual: " + nodo.dato);
            System.out.println("Izquierdo: " + izquierdo);
            System.out.println("Derecho: " + derecho);

            switch (nodo.dato.charAt(0)) {
                case '&':
                    return (int) izquierdo & (int) derecho;
                case '|':
                    return (int) izquierdo | (int) derecho;
                case '^':
                    return (int) izquierdo ^ (int) derecho;
                case '~':
                    return (double) ~(int) derecho; // Inversión de bits y luego convertir a double
            }
            return 0;
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
        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                 PrintWriter salida = new PrintWriter(clienteSocket.getOutputStream(), true)) {

                String expresion = entrada.readLine();

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
            System.out.println("Convirtiendo expresión a postfija...");
            String postfija = convertirApostfija(expresion);
            System.out.println("Expresión postfija: " + postfija);

            ArbolExpresion arbol;

            // Decidir qué tipo de árbol usar según la expresión
            if (expresion.contains("&") || expresion.contains("|") || expresion.contains("^") || expresion.contains("~")) {
                arbol = new ArbolLogico();
            } else {
                arbol = new ArbolAlgebraico();
            }

            arbol.construirDesdePostfija(postfija);
            double result = arbol.evaluar();
            System.out.println("Resultado de evaluar el árbol: " + result);
            return result;
        }



        private boolean esEntradaValida(String expresion) {
            boolean contieneOperadoresLogicos = expresion.contains("&") || expresion.contains("|") || expresion.contains("^") || expresion.contains("~");
            boolean contieneOperadoresAlgebraicos = expresion.contains("+") || expresion.contains("-") || expresion.contains("*") || expresion.contains("/") || expresion.contains("%") || expresion.contains("**");

            if (contieneOperadoresLogicos && contieneOperadoresAlgebraicos) {
                return false;  // La expresión contiene ambos tipos de operadores, lo cual es inválido
            }
            return true;  // La expresión es válida
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
            System.out.println("Convirtiendo expresión a notación postfija: " + infija);
            StringBuilder postfija = new StringBuilder();
            Stack<Character> pila = new Stack<>();

            for (int i = 0; i < infija.length(); i++) {
                char c = infija.charAt(i);

                if (Character.isDigit(c)) {
                    // Manejar números de múltiples dígitos
                    while (i < infija.length() && (Character.isDigit(infija.charAt(i)) || infija.charAt(i) == '.')) {
                        postfija.append(infija.charAt(i++));
                    }
                    postfija.append(" "); // Agregamos un espacio como separador
                    i--; // Ajuste para el bucle principal
                } else if (c == '+' || c == '-') {
                    while (!pila.isEmpty() && (pila.peek() == '*' || pila.peek() == '/' || pila.peek() == '+' || pila.peek() == '-' || pila.peek() == '%' || pila.peek() == '&' || pila.peek() == '|' || pila.peek() == '^')) {
                        postfija.append(pila.pop());
                    }
                    pila.push(c);
                } else if (c == '*' || c == '/' || c == '%' || c == '&' || c == '|' || c == '^') {
                    if (c == '*' && i + 1 < infija.length() && infija.charAt(i + 1) == '*') { // Para manejar el operador **
                        postfija.append("** ");
                        i++; // Saltar el siguiente asterisco
                    } else {
                        while (!pila.isEmpty() && (pila.peek() == '*' || pila.peek() == '/' || pila.peek() == '%')) {
                            postfija.append(pila.pop());
                        }
                        pila.push(c);
                    }
                } else if (c == '(') {
                    pila.push(c);
                } else if (c == ')') {
                    while (!pila.isEmpty() && pila.peek() != '(') {
                        postfija.append(pila.pop());
                    }
                    pila.pop();  // Remover '// ('
                } else if (c == '~') {
                    while (i + 1 < infija.length() && (Character.isDigit(infija.charAt(i + 1)) || infija.charAt(i + 1) == '.')) {
                        postfija.append(infija.charAt(++i));
                    }
                    postfija.append(" "); // Agregamos un espacio como separador
                    postfija.append(c);   // Añade el operador ~ después del número
                    postfija.append(" "); // Añade otro espacio
                }
            }


            while (!pila.isEmpty()) {
                postfija.append(pila.pop());
            }

            String resultado = postfija.toString().trim();
            System.out.println("Resultado de conversión a postfija: " + resultado);
            return resultado;
        }
    }
}
