public class Nodo {
    private char dato;
    private Nodo derecho;
    private Nodo izquierdo;

    public Nodo(){
        this.dato = '\u0000';
        this.derecho = null;
        this.izquierdo = null;
    }
    public boolean esOperando(Nodo nodo){
        if (nodo.dato == (int)nodo.dato || nodo.dato == (float)nodo.dato){
            return true;
        } else {
            return false;
        }
    }
    private void agregarDerecha(Nodo nodo){
        this.derecho = nodo;
    }
    private void agregarIzquierda(Nodo nodo){
        this.izquierdo = nodo;
    }

    public int resolver(Nodo nodo){
        switch(nodo.dato){
            case '+':
                return resolver(nodo.izquierdo) + resolver(nodo.derecho);
            case '-':
                return resolver(nodo.izquierdo) - resolver(nodo.derecho);
            case '*':
                return resolver(nodo.izquierdo) * resolver(nodo.derecho);
            case '/':
                return resolver(nodo.izquierdo) / resolver(nodo.derecho);
            case '\u0000':
                System.out.println("Operando sobre un valor nulo");
                return -1;
            default:
                if (this.esOperando(nodo))
                    return nodo.dato;
                else
                    System.out.println("Operacion no implementada");
        }
        return -1;
    }
    public void agregar(Nodo nodo){

    }
    public static void main(String[] args){
        Nodo nodo = new Nodo();


    }
}
