import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Interfaz de Configuraci\u00f3n de p\u00e1gina (Java + JOGL)
 * - Ajusta m\u00e1rgenes (superior, inferior, izquierdo, derecho) en cm
 * - Orientaci\u00f3n Vertical / Horizontal
 * - Bot\u00f3n Reset: m\u00e1rgenes en 0 y p\u00e1gina vertical
 * La vista previa de la hoja se dibuja con OpenGL (JOGL).
 */
public class ConfiguracionPagina extends JFrame implements GLEventListener {

    // Hoja tama\u00f1o A4 en mm
    private static final double ANCHO_MM = 210.0;
    private static final double ALTO_MM = 297.0;

    private final JSpinner spSuperior = crearSpinner();
    private final JSpinner spInferior = crearSpinner();
    private final JSpinner spIzquierdo = crearSpinner();
    private final JSpinner spDerecho = crearSpinner();
    private final JComboBox<String> cbOrientacion =
            new JComboBox<>(new String[]{"Vertical", "Horizontal"});
    private final JLabel lblInfo = new JLabel(" ");
    private final GLJPanel panelGL;

    public ConfiguracionPagina() {
        super("Configuraci\u00f3n de p\u00e1gina");

        GLProfile perfil = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(perfil);
        panelGL = new GLJPanel(caps);
        panelGL.addGLEventListener(this);
        panelGL.setPreferredSize(new Dimension(420, 480));

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> reset());

        javax.swing.event.ChangeListener cl = e -> actualizar();
        spSuperior.addChangeListener(cl);
        spInferior.addChangeListener(cl);
        spIzquierdo.addChangeListener(cl);
        spDerecho.addChangeListener(cl);
        cbOrientacion.addActionListener(e -> actualizar());

        JPanel controles = new JPanel(new GridBagLayout());
        controles.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);
        c.weightx = 1;

        agregar(controles, c, new JLabel("Margen superior (cm)"));
        agregar(controles, c, spSuperior);
        agregar(controles, c, new JLabel("Margen inferior (cm)"));
        agregar(controles, c, spInferior);
        agregar(controles, c, new JLabel("Margen izquierdo (cm)"));
        agregar(controles, c, spIzquierdo);
        agregar(controles, c, new JLabel("Margen derecho (cm)"));
        agregar(controles, c, spDerecho);
        agregar(controles, c, new JLabel("Orientaci\u00f3n de p\u00e1gina"));
        agregar(controles, c, cbOrientacion);
        c.insets = new Insets(18, 4, 4, 4);
        agregar(controles, c, btnReset);
        c.insets = new Insets(10, 4, 4, 4);
        agregar(controles, c, lblInfo);

        // empuja todo hacia arriba
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        agregar(controles, c, new JPanel());

        setLayout(new BorderLayout());
        add(controles, BorderLayout.WEST);
        add(panelGL, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(560, 520));
        setLocationRelativeTo(null);
        actualizar();
    }

    private static void agregar(JPanel p, GridBagConstraints c, Component comp) {
        p.add(comp, c);
        c.gridy++;
    }

    private static JSpinner crearSpinner() {
        // valor, m\u00ednimo, m\u00e1ximo, paso (cm)
        JSpinner s = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.5));
        s.setEditor(new JSpinner.NumberEditor(s, "0.0"));
        return s;
    }

    private void reset() {
        spSuperior.setValue(0.0);
        spInferior.setValue(0.0);
        spIzquierdo.setValue(0.0);
        spDerecho.setValue(0.0);
        cbOrientacion.setSelectedItem("Vertical");
        actualizar();
    }

    private boolean esHorizontal() {
        return "Horizontal".equals(cbOrientacion.getSelectedItem());
    }

    private double val(JSpinner s) {
        return ((Number) s.getValue()).doubleValue();
    }

    private void actualizar() {
        boolean h = esHorizontal();
        double w = h ? ALTO_MM : ANCHO_MM;
        double hh = h ? ANCHO_MM : ALTO_MM;
        lblInfo.setText((h ? "Horizontal" : "Vertical") + " - " + (int) w + " x " + (int) hh + " mm");
        panelGL.repaint();
    }

    // ---------------- JOGL ----------------

    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        gl.glClearColor(0.93f, 0.93f, 0.93f, 1f);
        gl.glDisable(GL2.GL_DEPTH_TEST);
    }

    @Override
    public void dispose(GLAutoDrawable d) {
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
    }

    @Override
    public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        int w = d.getSurfaceWidth();
        int h = d.getSurfaceHeight();

        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, w, 0, h, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        boolean horiz = esHorizontal();
        double pw = horiz ? ALTO_MM : ANCHO_MM;
        double ph = horiz ? ANCHO_MM : ALTO_MM;

        // escala para que la hoja quepa con un poco de aire
        double pad = 30;
        double s = Math.min((w - 2 * pad) / pw, (h - 2 * pad) / ph);
        if (s <= 0) return;

        double pxW = pw * s;
        double pxH = ph * s;
        double x0 = (w - pxW) / 2.0;
        double y0 = (h - pxH) / 2.0;
        double x1 = x0 + pxW;
        double y1 = y0 + pxH;

        // sombra
        rellenar(gl, x0 + 5, y0 - 5, x1 + 5, y1 - 5, 0.75f, 0.75f, 0.75f);
        // hoja blanca
        rellenar(gl, x0, y0, x1, y1, 1f, 1f, 1f);

        // m\u00e1rgenes (cm -> mm -> px)
        double mt = val(spSuperior) * 10 * s;
        double mb = val(spInferior) * 10 * s;
        double ml = val(spIzquierdo) * 10 * s;
        double mr = val(spDerecho) * 10 * s;

        double ix0 = x0 + ml;
        double ix1 = x1 - mr;
        double iy0 = y0 + mb;
        double iy1 = y1 - mt;

        if (ix1 > ix0 && iy1 > iy0) {
            // \u00e1rea imprimible
            rellenar(gl, ix0, iy0, ix1, iy1, 0.90f, 0.95f, 1f);
            contorno(gl, ix0, iy0, ix1, iy1, 1f, 0f, 0f, 1.5f);
        }

        // borde de la hoja
        contorno(gl, x0, y0, x1, y1, 0f, 0f, 0.8f, 2f);
    }

    private void rellenar(GL2 gl, double x0, double y0, double x1, double y1,
                          float r, float g, float b) {
        gl.glColor3f(r, g, b);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2d(x0, y0);
        gl.glVertex2d(x1, y0);
        gl.glVertex2d(x1, y1);
        gl.glVertex2d(x0, y1);
        gl.glEnd();
    }

    private void contorno(GL2 gl, double x0, double y0, double x1, double y1,
                          float r, float g, float b, float grosor) {
        gl.glColor3f(r, g, b);
        gl.glLineWidth(grosor);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(x0, y0);
        gl.glVertex2d(x1, y0);
        gl.glVertex2d(x1, y1);
        gl.glVertex2d(x0, y1);
        gl.glEnd();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConfiguracionPagina().setVisible(true));
    }
}
