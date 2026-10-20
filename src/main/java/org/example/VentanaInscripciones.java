package org.example;

import org.example.dao.GestionInscripciones;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionJuegos;
import org.example.dao.GestionSocios;
import org.example.models.Inscripcion;
import org.example.models.Partida;
import org.example.models.Juego;
import org.example.models.Socio;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class VentanaInscripciones extends JFrame {

    private JTextField IdInscripcion;
    private JTextField IDPartida;
    private JTextField Socio;
    private JTextField Invitado;

    private JButton inscribirSocioButton;
    private JButton inscribirInvitadoButton1;
    private JButton quitarInscripcionButton;
    private JButton marcarGanadroButton;

    private JTextField Buscador;
    private JButton mostrarParticipantesButton;
    private JTextArea Lista;

    private JButton limpiarButton;
    private JButton cerrarButton;
    private JButton QuitarGanador;

    private JPanel VentanaInscripciones;

    // NUEVO: ComboBox de busquedas
    private JComboBox TipoBusquedaInscripciones;

    private JComboBox<String> ComboPartidas;
    private JComboBox<String> ComboSocios;
    private JLabel PlazasLibresLabel;

    // Las listas guardan los objetos que se muestran en los ComboBox
    private List<Partida> partidasCombo = new ArrayList<>();
    private List<Socio> sociosCombo = new ArrayList<>();


    public VentanaInscripciones() {

        setTitle("Gestion de Inscripciones");
        setContentPane(VentanaInscripciones);
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        IdInscripcion.setEditable(false);
        Lista.setEditable(false);

        // Opciones del buscador
        TipoBusquedaInscripciones.setModel(new DefaultComboBoxModel<>(new String[]{"ID de partida", "Documento del socio", "Nombre del socio", "Nombre del invitado", "Ganadores", "ID de inscripcion"}));

        Lista.setText("Introduce un ID de partida para ver sus participantes");

        // Cargar las opciones desde MariaDB
        cargarPartidas();
        cargarSocios();

        // Al elegir una partida, se rellena el ID automaticamente
        ComboPartidas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int posicion = ComboPartidas.getSelectedIndex() - 1;

                IdInscripcion.setText("");

                if (posicion >= 0 && posicion < partidasCombo.size()) {

                    Partida partida = partidasCombo.get(posicion);
                    IDPartida.setText(String.valueOf(partida.getId()));
                    TipoBusquedaInscripciones.setSelectedIndex(0);
                    Buscador.setText(String.valueOf(partida.getId()));
                    mostrarParticipantes();

                } else {

                    IDPartida.setText("");
                    PlazasLibresLabel.setText("Plazas libres: -");
                }
            }
        });

        // Al elegir un socio, se rellena su documento automaticamente
        ComboSocios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int posicion = ComboSocios.getSelectedIndex() - 1;

                if (posicion >= 0 && posicion < sociosCombo.size()) {

                    Socio.setText(sociosCombo.get(posicion).getDocumento());
                    Invitado.setText("");

                } else {

                    Socio.setText("");
                }
            }
        });

        // Si vamos a escribir un invitado, quitamos el socio seleccionado
        Invitado.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ComboSocios.setSelectedIndex(0);
            }
        });


        // INSCRIBIR SOCIO
        inscribirSocioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inscribirSocio();
            }
        });


        // INSCRIBIR INVITADO
        inscribirInvitadoButton1.addActionListener(new ActionListener() {
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


        // BUSCAR / MOSTRAR PARTICIPANTES
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


        // Seleccionar una inscripcion pulsando en el listado
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

                    int separador = texto.indexOf(" |");

                    if (separador == -1) {
                        return;
                    }

                    String numero = texto.substring(4, separador);

                    int id = Integer.parseInt(numero.trim());

                    Inscripcion inscripcion = GestionInscripciones.buscar(id);

                    if (inscripcion != null) {
                        rellenarCampos(inscripcion);
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(VentanaInscripciones.this, "Error al seleccionar: " + ex.getMessage());
                }
            }
        });
    }


    // RELLENAR CAMPOS
    public void rellenarCampos(Inscripcion inscripcion) {

        // Seleccionar la partida en el desplegable
        for (int i = 0; i < partidasCombo.size(); i++) {
            if (partidasCombo.get(i).getId() == inscripcion.getIdPartida()) {
                ComboPartidas.setSelectedIndex(i + 1);
                break;
            }
        }

        IDPartida.setText(String.valueOf(inscripcion.getIdPartida()));

        if (inscripcion.getDocumentoSocio() != null) {

            Socio.setText(inscripcion.getDocumentoSocio());
            Invitado.setText("");

            // Seleccionar el socio en el desplegable
            for (int i = 0; i < sociosCombo.size(); i++) {
                if (sociosCombo.get(i).getDocumento().equals(inscripcion.getDocumentoSocio())) {
                    ComboSocios.setSelectedIndex(i + 1);
                    break;
                }
            }

        } else {

            ComboSocios.setSelectedIndex(0);
            Socio.setText("");
            Invitado.setText(inscripcion.getNombreInvitado());
        }

        // Al final, para que no se borre al cambiar de partida
        IdInscripcion.setText(String.valueOf(inscripcion.getId()));
    }


    // INSCRIBIR SOCIO
    public void inscribirSocio() {

        try {

            int partida = Integer.parseInt(IDPartida.getText().trim());

            String documento = Socio.getText().trim();

            if (documento.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Introduce el documento del socio");
                return;
            }

            if (!Invitado.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Deja vacio el campo Invitado");
                return;
            }

            GestionInscripciones.inscribirSocio(partida, documento);

            JOptionPane.showMessageDialog(this, "Socio inscrito correctamente");

            mostrarPartidaDespuesCambio(partida);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "El ID de la partida debe ser un numero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al inscribir socio: " + e.getMessage());
        }
    }


    // INSCRIBIR INVITADO
    public void inscribirInvitado() {

        try {

            int partida = Integer.parseInt(IDPartida.getText().trim());

            String nombre = Invitado.getText().trim();

            if (nombre.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Introduce el nombre del invitado");
                return;
            }

            if (!Socio.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Deja vacio el campo Socio");
                return;
            }

            GestionInscripciones.inscribirInvitado(partida, nombre);

            JOptionPane.showMessageDialog(this, "Invitado inscrito correctamente");

            mostrarPartidaDespuesCambio(partida);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "El ID de la partida debe ser un numero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al inscribir invitado: " + e.getMessage());
        }
    }


    // QUITAR INSCRIPCION
    public void quitarInscripcion() {

        if (IdInscripcion.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona una inscripcion primero");
            return;
        }

        try {

            int id = Integer.parseInt(IdInscripcion.getText());

            int partida = Integer.parseInt(IDPartida.getText());

            int respuesta = JOptionPane.showConfirmDialog(this, "¿Quieres eliminar esta inscripcion?", "Eliminar inscripcion", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {

                if (GestionInscripciones.quitar(id)) {

                    JOptionPane.showMessageDialog(this, "Inscripcion eliminada correctamente");

                    mostrarPartidaDespuesCambio(partida);

                } else {

                    JOptionPane.showMessageDialog(this, "Inscripcion no encontrada");
                }
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID de inscripcion incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }


    // MARCAR O QUITAR GANADOR
    public void cambiarGanador(boolean ganador) {

        if (IdInscripcion.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona una inscripcion primero");
            return;
        }

        try {

            int id = Integer.parseInt(IdInscripcion.getText());

            int partida = Integer.parseInt(IDPartida.getText());

            if (GestionInscripciones.marcarGanador(id, ganador)) {

                if (ganador) {

                    JOptionPane.showMessageDialog(this, "Ganador marcado correctamente");

                } else {

                    JOptionPane.showMessageDialog(this, "Ganador desmarcado correctamente");
                }

                mostrarPartidaDespuesCambio(partida);

            } else {

                JOptionPane.showMessageDialog(this, "Inscripcion no encontrada");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID de inscripcion incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al modificar ganador: " + e.getMessage());
        }
    }


    // NUEVO: BUSQUEDA AVANZADA DE INSCRIPCIONES
    public void mostrarParticipantes() {

        String busqueda = Buscador.getText().trim();

        int opcion = TipoBusquedaInscripciones.getSelectedIndex();

        // Mantener el funcionamiento anterior:
        // poder consultar una partida desde IDPartida
        if (opcion == 0 && busqueda.isEmpty()) {

            busqueda = IDPartida.getText().trim();
        }

        // Ganadores no necesita texto de busqueda
        if (opcion != 4 && busqueda.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Introduce un dato para buscar");
            return;
        }

        try {

            if (opcion == 0) {

                int partida = Integer.parseInt(busqueda);

                if (GestionPartidas.buscar(partida) == null) {

                    JOptionPane.showMessageDialog(this, "La partida no existe");
                    return;
                }
            }

            List<Inscripcion> inscripciones = GestionInscripciones.buscarAvanzado(opcion, busqueda);

            mostrarListado(inscripciones);

            // Para una partida concreta mostrar las plazas libres
            if (opcion == 0) {

                int partida = Integer.parseInt(busqueda);

                int libres = GestionPartidas.plazasLibres(partida);

                Lista.append("\nPlazas libres: " + libres);

                Partida datosPartida = GestionPartidas.buscar(partida);
                PlazasLibresLabel.setText("Plazas libres: " + libres + " de " + datosPartida.getJugadoresDeseados() + " (" + datosPartida.getEstado() + ")");

                // Facilitar nuevas inscripciones
                IDPartida.setText(String.valueOf(partida));
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Introduce un ID numerico valido");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al buscar inscripciones: " + e.getMessage());
        }
    }


    // NUEVO: MOSTRAR LOS RESULTADOS DE LA BUSQUEDA
    private void mostrarListado(List<Inscripcion> inscripciones) {

        Lista.setText("");

        if (inscripciones.isEmpty()) {

            Lista.setText("No se han encontrado inscripciones\n");
            return;
        }

        for (Inscripcion inscripcion : inscripciones) {

            String nombre;

            if (inscripcion.getDocumentoSocio() != null) {

                nombre = "Socio: " + inscripcion.getDocumentoSocio();

            } else {

                nombre = "Invitado: " + inscripcion.getNombreInvitado();
            }

            String ganador = "";

            if (inscripcion.isGanador()) {
                ganador = " | GANADOR";
            }

            // Mantenemos "ID: " para seleccionar con el raton
            Lista.append("ID: " + inscripcion.getId() + " | Partida: " + inscripcion.getIdPartida() + " | " + nombre + ganador + "\n");
        }
    }


    // CARGAR PARTIDAS EN EL COMBOBOX
    private void cargarPartidas() {

        try {

            partidasCombo = GestionPartidas.listar();

            ComboPartidas.removeAllItems();
            ComboPartidas.addItem("Selecciona una partida");

            for (Partida partida : partidasCombo) {

                Juego juego = GestionJuegos.buscar(partida.getIdJuego());
                String nombre = juego == null ? "Juego sin nombre" : juego.getNombre();

                ComboPartidas.addItem(partida.getId() + " - " + nombre + " - " + partida.getFecha() + " " + partida.getHora());
            }

            ComboPartidas.setSelectedIndex(0);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al cargar partidas: " + e.getMessage());
        }
    }


    // CARGAR SOCIOS EN EL COMBOBOX
    private void cargarSocios() {

        try {

            sociosCombo = GestionSocios.listar();

            ComboSocios.removeAllItems();
            ComboSocios.addItem("Selecciona un socio");

            for (Socio socio : sociosCombo) {

                ComboSocios.addItem(socio.getNombre() + " " + socio.getApellidos() + " - " + socio.getDocumento());
            }

            ComboSocios.setSelectedIndex(0);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al cargar socios: " + e.getMessage());
        }
    }


    // VOLVER A MOSTRAR LA PARTIDA TRAS AÑADIR O CAMBIAR DATOS
    private void mostrarPartidaDespuesCambio(int partida) {

        limpiarCampos();

        for (int i = 0; i < partidasCombo.size(); i++) {
            if (partidasCombo.get(i).getId() == partida) {
                ComboPartidas.setSelectedIndex(i + 1);
                return;
            }
        }

        // Si la partida no aparece en el ComboBox, usamos el ID
        IDPartida.setText(String.valueOf(partida));
        Buscador.setText(String.valueOf(partida));
        mostrarParticipantes();
    }


    // LIMPIAR CAMPOS
    public void limpiarCampos() {

        IdInscripcion.setText("");
        IDPartida.setText("");
        Socio.setText("");
        Invitado.setText("");
        Buscador.setText("");

        TipoBusquedaInscripciones.setSelectedIndex(0);
        ComboPartidas.setSelectedIndex(0);
        ComboSocios.setSelectedIndex(0);
        PlazasLibresLabel.setText("Plazas libres: -");
    }
}
