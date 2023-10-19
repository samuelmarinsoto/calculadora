import java.util.Stack;

/**
 * Arbol de expresion
 * TODO: funcion que convierta arreglo de infijo a posfijo, se podria extender es String de java y crear un metodo ahi, o ponerlo aqui directo
 */
public class Expr {
    private char raiz;
    private Expr derecho;
    private Expr izquierdo;

    public Expr(char dato, Expr d, Expr i){
        this.raiz = dato;
        this.derecho = d;
        this.izquierdo = i;
    }

    /**
     * Funcion que revisa si un caracter es un operando o no
     * @param i caracter que se va a determinar si es numero o no
     * @return booleano
     */
    public boolean esOperando(char i){
        if (i == (int)i || i == (float)i){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Funcion que resuelve el arbol de expresion
     * @return int, float representante del valor numerico de la expresion
     */
    public int resolver(){
        switch(this.raiz){
            case '+':
                return this.izquierdo.resolver() + this.derecho.resolver();
            case '-':
                return this.izquierdo.resolver() - this.derecho.resolver();
            case '*':
                return this.izquierdo.resolver() * this.derecho.resolver();
            case '/':
                return this.izquierdo.resolver() / this.derecho.resolver();
            case '\u0000':
                System.out.println("Operando sobre un valor nulo");
                return -1;
            default:
                if (this.esOperando(this.raiz))
                    return this.raiz;
                else
                    System.out.println("Operacion no implementada");
        }
        return -1;
    }

    /**
     * Funcion que toma una expresion en formato posfijo y la convierte en un arbol de expresion
     * @param posfijo un arreglo
     */
    public void aArbol(String posfijo){
        Stack nodos = new Stack(); // TODO: crear un Stack casero
        for(int i = 0; i < posfijo.length(); i++) {
            if (esOperando(posfijo.charAt(i))) {
                Expr nodo = new Expr(posfijo.charAt(i), null, null);
                nodos.push(nodo);
            } else {
                Expr der = (Expr) nodos.pop();
                Expr izq = (Expr) nodos.pop();
                Expr nuevo_nodo = new Expr(posfijo.charAt(i), der, izq);
                nodos.push(nuevo_nodo);
            }
        }
        
        Expr laraiz = (Expr) nodos.pop();
        this.raiz = laraiz.raiz;
        this.derecho = laraiz.derecho;
        this.izquierdo = laraiz.izquierdo;
    }
    public static void main(String[] args){

    }
}
