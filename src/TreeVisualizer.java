import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

import javax.swing.*;

public class TreeVisualizer {

    public static void main(String[] args) {

        // Crear un grafo
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Añadir nodos y bordes al grafo (árbol de expresión)
        g.addVertex("5");
        g.addVertex("+");
        g.addVertex("3");
        g.addEdge("5", "+");
        g.addEdge("+", "3");

        // Crear una visualización del grafo usando JGraphX
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(g);
        mxGraphComponent component = new mxGraphComponent(graphAdapter);
        component.zoomAndCenter();
        new mxCircleLayout(graphAdapter).execute(graphAdapter.getDefaultParent());

        // Crear y mostrar el frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(component);
        frame.pack();
        frame.setVisible(true);
    }
}
