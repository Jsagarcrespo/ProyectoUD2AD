
package org.example;

import org.example.dao.GestionInscripciones;
import org.example.dao.GestionPartidas;
import org.example.models.Inscripcion;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class VentanaInscripciones extends JFrame {

    private JTextField IdInscripcion;
    private JTextField IDPartida;
    private JTextField Socio;
    private JTextField Invitado;

    private JButton inscribirSocioButton;
    private JButton inscribirSocioButton1;
    private JButton quitarInscripcionButton;
    private JButton marcarGanadroButton;

    private JTextField Buscador;
    private JButton mostrarParticipantesButton;
    private JTextArea Lista;

    private JButton limpiarButton;
    private JButton cerrarButton;
    private JButton QuitarGanador;

    private JPanel VentanaInscripciones;


    public VentanaInscripciones() {

        setTitle("Gestion de Inscripciones");
        setContentPane(VentanaInscripciones);
        setSize(800, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        IdInscripcion.setEditable(false);
        Lista.setEditable(false);

        Lista.setText("Introduce el ID de una partida para ver sus participantes");


        // INSCRIBIR SOCIO
        inscribirSocioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inscribirSocio();
            }
        });


        // INSCRIBIR INVITADO
        inscribirSocioButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inscribirInvitado();
            }
        });


        // QUITAR INSCRIPCION
        quitarInscripcionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitarInscripcion();
            }
        });


        // MARCAR GANADOR
        marcarGanadroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarGanador(true);
            }
        });


        // QUITAR GANADOR
        QuitarGanador.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarGanador(false);
            }
        });


        // MOSTRAR PARTICIPANTES
        mostrarParticipantesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarParticipantes();
            }
        });


        // LIMPIAR
        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });


        // CERRAR
        cerrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        // Seleccionar una inscripcion del listado
        // Igual que haciamos en UD1
        Lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    int posicion = Lista.viewToModel2D(e.getPoint());

                    int linea = Lista.getLineOfOffset(posicion);

                    int inicio = Lista.getLineStartOffset(linea);
                    int fin = Lista.getLineEndOffset(linea);

                    String texto = Lista.getText(inicio, fin - inicio);

                    if (!texto.startsWith("ID: ")) {
                        return;
                    }

                    String numero = texto.substring(
                            4, texto.indexOf(" |"));

                    int id = Integer.parseInt(numero.trim());

                    Inscripcion inscripcion =
                            GestionInscripciones.buscar(id);

                    if (inscripcion != null) {
                        rellenarCampos(inscripcion);
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            VentanaInscripciones.this,
                            "Error al seleccionar: " + ex.getMessage());
                }
            }
        });
    }


    // Rellena los campos con la inscripcion seleccionada
    public void rellenarCampos(Inscripcion inscripcion) {

        IdInscripcion.setText(
                String.valueOf(inscripcion.getId()));

        IDPartida.setText(
                String.valueOf(inscripcion.getIdPartida()));

        if (inscripcion.getDocumentoSocio() != null) {

            Socio.setText(inscripcion.getDocumentoSocio());
            Invitado.setText("");

        } else {

            Socio.setText("");
            Invitado.setText(inscripcion.getNombreInvitado());
        }
    }


    // INSCRIBIR SOCIO
    public void inscribirSocio() {

        try {

            int partida = Integer.parseInt(
                    IDPartida.getText().trim());

            String documento = Socio.getText().trim();

            if (documento.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this, "Introduce el documento del socio");
                return;
            }

            if (!Invitado.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this, "Deja vacio el campo Invitado");
                return;
            }

            GestionInscripciones.inscribirSocio(partida, documento);

            JOptionPane.showMessageDialog(
                    this, "Socio inscrito correctamente");

            limpiarCampos();
            Buscador.setText(String.valueOf(partida));
            mostrarParticipantes();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "El ID de la partida debe ser un numero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this, "Error al inscribir socio: " + e.getMessage());
        }
    }


    // INSCRIBIR INVITADO
    public void inscribirInvitado() {

        try {

            int partida = Integer.parseInt(
                    IDPartida.getText().trim());

            String nombre = Invitado.getText().trim();

            if (nombre.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this, "Introduce el nombre del invitado");
                return;
            }

            if (!Socio.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this, "Deja vacio el campo Socio");
                return;
            }

            GestionInscripciones.inscribirInvitado(partida, nombre);

            JOptionPane.showMessageDialog(
                    this, "Invitado inscrito correctamente");

            limpiarCampos();
            Buscador.setText(String.valueOf(partida));
            mostrarParticipantes();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "El ID de la partida debe ser un numero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this, "Error al inscribir invitado: " + e.getMessage());
        }
    }


    // QUITAR INSCRIPCION
    public void quitarInscripcion() {

        if (IdInscripcion.getText().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Selecciona una inscripcion primero");
            return;
        }

        try {

            int id = Integer.parseInt(
                    IdInscripcion.getText());

            int partida = Integer.parseInt(
                    IDPartida.getText());

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Quieres eliminar esta inscripcion?",
                    "Eliminar inscripcion",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {

                if (GestionInscripciones.quitar(id)) {

                    JOptionPane.showMessageDialog(
                            this, "Inscripcion eliminada correctamente");

                    limpiarCampos();
                    Buscador.setText(String.valueOf(partida));
                    mostrarParticipantes();

                } else {

                    JOptionPane.showMessageDialog(
                            this, "Inscripcion no encontrada");
                }
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "ID de inscripcion incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this, "Error al eliminar: " + e.getMessage());
        }
    }


    // MARCAR O QUITAR GANADOR
    public void cambiarGanador(boolean ganador) {

        if (IdInscripcion.getText().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Selecciona una inscripcion primero");
            return;
        }

        try {

            int id = Integer.parseInt(
                    IdInscripcion.getText());

            int partida = Integer.parseInt(
                    IDPartida.getText());

            if (GestionInscripciones.marcarGanador(id, ganador)) {

                if (ganador) {

                    JOptionPane.showMessageDialog(
                            this, "Ganador marcado correctamente");

                } else {

                    JOptionPane.showMessageDialog(
                            this, "Ganador desmarcado correctamente");
                }

                limpiarCampos();
                Buscador.setText(String.valueOf(partida));
                mostrarParticipantes();

            } else {

                JOptionPane.showMessageDialog(
                        this, "Inscripcion no encontrada");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "ID de inscripcion incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this, "Error al modificar ganador: " + e.getMessage());
        }
    }


    // MOSTRAR PARTICIPANTES DE UNA PARTIDA
    public void mostrarParticipantes() {

        try {

            String textoPartida = Buscador.getText().trim();

            // Tambien se puede utilizar el campo IDPartida
            if (textoPartida.isEmpty()) {
                textoPartida = IDPartida.getText().trim();
            }

            int partida = Integer.parseInt(textoPartida);

            if (GestionPartidas.buscar(partida) == null) {

                JOptionPane.showMessageDialog(
                        this, "La partida no existe");
                return;
            }

            List<Inscripcion> inscripciones =
                    GestionInscripciones.listar(partida);

            Lista.setText("");

            if (inscripciones.isEmpty()) {

                Lista.setText("No hay participantes inscritos");

            } else {

                for (Inscripcion inscripcion : inscripciones) {

                    String nombre;

                    if (inscripcion.getDocumentoSocio() != null) {

                        nombre = "Socio: "
                                + inscripcion.getDocumentoSocio();

                    } else {

                        nombre = "Invitado: "
                                + inscripcion.getNombreInvitado();
                    }

                    String ganador = "";

                    if (inscripcion.isGanador()) {
                        ganador = " | GANADOR";
                    }

                    Lista.append(
                            "ID: " + inscripcion.getId()
                                    + " | Partida: " + inscripcion.getIdPartida()
                                    + " | " + nombre
                                    + ganador
                                    + "\n");
                }
            }

            int libres = GestionPartidas.plazasLibres(partida);

            Lista.append("\nPlazas libres: " + libres);

            // Deja puesto el ID de partida para nuevas inscripciones
            IDPartida.setText(String.valueOf(partida));

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this, "Introduce un ID de partida valido");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this, "Error al mostrar participantes: "
                            + e.getMessage());
        }
    }


    // LIMPIAR CAMPOS
    public void limpiarCampos() {

        IdInscripcion.setText("");
        IDPartida.setText("");
        Socio.setText("");
        Invitado.setText("");
        Buscador.setText("");
    }
}
