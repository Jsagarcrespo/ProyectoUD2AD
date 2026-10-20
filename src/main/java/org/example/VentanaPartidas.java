package org.example;

import org.example.dao.GestionPartidas;
import org.example.dao.GestionJuegos;
import org.example.models.Partida;
import org.example.models.Juego;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

public class VentanaPartidas extends JFrame {

    private JTextField IDPartidaTextField;
    private JTextField Fecha;
    private JTextField Hora;
    private JTextField JugadoresDeseados;

    private JComboBox<String> ComboJuegos;
    private JComboBox<String> ComboEstado;

    // Lista para saber el ID del juego seleccionado
    private List<Juego> juegosDelCombo = new ArrayList<>();

    private JButton añadirButton;
    private JButton modificarButton;
    private JButton cancelarButton;
    private JButton finalizarButton;

    private JTextField Buscar;
    private JButton buscarButton;
    private JTextArea Lista;

    private JButton mostrarTodosButton;
    private JButton eliminarButton;
    private JButton limpiarButton;
    private JButton cerrarButton;

    private JPanel VentanaPartidas;
    private JComboBox TipoBusquedaPartidas;


    public VentanaPartidas() {

        setTitle("Gestion de Partidas");
        setContentPane(VentanaPartidas);
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        IDPartidaTextField.setEditable(false);
        Lista.setEditable(false);

        Fecha.setText(LocalDate.now().toString());

        // Estados disponibles
        ComboEstado.setModel(new DefaultComboBoxModel<>(new String[]{"programada", "finalizada", "cancelada"}));

        // Cargar juegos desde la base de datos
        cargarJuegos();

        // Opciones para buscar
        TipoBusquedaPartidas.setModel(new DefaultComboBoxModel<>(new String[]{"ID de partida", "Nombre del juego", "ID del juego", "Fecha", "Estado"}));

        mostrarPartidas();


        // AÑADIR
        añadirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anadirPartida();
            }
        });


        // MODIFICAR
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarPartida();
            }
        });


        // CANCELAR
        cancelarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelarPartida();
            }
        });


        // FINALIZAR
        finalizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalizarPartida();
            }
        });


        // BUSCAR
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarPartida();
            }
        });


        // MOSTRAR TODOS
        mostrarTodosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarPartidas();
            }
        });


        // ELIMINAR
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarPartida();
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


        // Seleccionar una partida pulsando en el listado
        Lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    int posicion = Lista.viewToModel2D(e.getPoint());
                    int linea = Lista.getLineOfOffset(posicion);

                    int inicio = Lista.getLineStartOffset(linea);
                    int fin = Lista.getLineEndOffset(linea);
                    Lista.select(inicio, fin);
                    String texto = Lista.getText(inicio, fin - inicio);

                    if (!texto.startsWith("ID: ")) {
                        return;
                    }

                    String numero = texto.substring(4, texto.indexOf(" |"));

                    int id = Integer.parseInt(numero.trim());

                    Partida partida = GestionPartidas.buscar(id);

                    if (partida != null) {
                        rellenarCampos(partida);
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(VentanaPartidas.this, "Error al seleccionar partida: " + ex.getMessage());
                }
            }
        });
    }


    // RELLENAR CAMPOS
    public void rellenarCampos(Partida partida) {

        IDPartidaTextField.setText(String.valueOf(partida.getId()));

        // Seleccionar el juego correspondiente
        for (int i = 0; i < juegosDelCombo.size(); i++) {

            if (juegosDelCombo.get(i).getId() == partida.getIdJuego()) {

                ComboJuegos.setSelectedIndex(i + 1);
                break;
            }
        }

        Fecha.setText(partida.getFecha().toString());
        Hora.setText(partida.getHora().toString());

        JugadoresDeseados.setText(String.valueOf(partida.getJugadoresDeseados()));

        ComboEstado.setSelectedItem(partida.getEstado());
    }


    // LEER DATOS DEL FORMULARIO
    public Partida leerPartida(int id) {

        int posicion = ComboJuegos.getSelectedIndex();

        if (posicion <= 0 || posicion > juegosDelCombo.size()) {

            throw new IllegalArgumentException("Selecciona un juego de la lista");
        }

        // Obtenemos el ID sin necesitar un JTextField
        Juego juego = juegosDelCombo.get(posicion - 1);

        int idJuego = juego.getId();

        LocalDate fecha = LocalDate.parse(Fecha.getText().trim());

        LocalTime hora = LocalTime.parse(Hora.getText().trim());

        int jugadores = Integer.parseInt(JugadoresDeseados.getText().trim());

        String estado = ComboEstado.getSelectedItem().toString();

        return new Partida(id, idJuego, fecha, hora, jugadores, estado);
    }


    // AÑADIR PARTIDA
    public void anadirPartida() {

        try {

            Partida partida = leerPartida(0);

            GestionPartidas.anadir(partida);

            JOptionPane.showMessageDialog(this, "Partida añadida correctamente");

            limpiarCampos();
            mostrarPartidas();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Los jugadores deseados deben ser un numero");

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(this, "Fecha u hora incorrecta.\n" + "Fecha: AAAA-MM-DD\n" + "Hora: HH:MM");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al añadir partida: " + e.getMessage());
        }
    }


    // MODIFICAR PARTIDA
    public void modificarPartida() {

        try {

            if (IDPartidaTextField.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona una partida primero");
                return;
            }

            int id = Integer.parseInt(IDPartidaTextField.getText());

            Partida partida = leerPartida(id);

            if (GestionPartidas.modificar(partida)) {

                JOptionPane.showMessageDialog(this, "Partida modificada correctamente");

                limpiarCampos();
                mostrarPartidas();

            } else {

                JOptionPane.showMessageDialog(this, "Partida no encontrada");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Introduce numeros correctos");

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(this, "Fecha u hora incorrecta");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al modificar partida: " + e.getMessage());
        }
    }


    // CANCELAR PARTIDA
    public void cancelarPartida() {

        if (IDPartidaTextField.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona una partida primero");
            return;
        }

        try {

            int id = Integer.parseInt(IDPartidaTextField.getText());

            Partida partida = GestionPartidas.buscar(id);

            if (partida == null) {

                JOptionPane.showMessageDialog(this, "Partida no encontrada");
                return;
            }

            if (!partida.getEstado().equals("programada")) {

                JOptionPane.showMessageDialog(this, "Solo se pueden cancelar partidas programadas");
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(this, "¿Quieres cancelar esta partida?", "Cancelar partida", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {

                if (GestionPartidas.cancelar(id)) {

                    JOptionPane.showMessageDialog(this, "Partida cancelada correctamente");

                    limpiarCampos();
                    mostrarPartidas();

                } else {

                    JOptionPane.showMessageDialog(this, "No se ha podido cancelar la partida");
                }
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID de partida incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al cancelar: " + e.getMessage());
        }
    }


    // FINALIZAR PARTIDA
    public void finalizarPartida() {

        if (IDPartidaTextField.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona una partida primero");
            return;
        }

        try {

            int id = Integer.parseInt(IDPartidaTextField.getText());

            if (GestionPartidas.finalizar(id)) {

                JOptionPane.showMessageDialog(this, "Partida finalizada correctamente");

                limpiarCampos();
                mostrarPartidas();

            } else {

                JOptionPane.showMessageDialog(this, "No se puede finalizar esta partida.\n" + "Debe estar programada.");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID de partida incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al finalizar: " + e.getMessage());
        }
    }


    // ELIMINAR PARTIDA
    public void eliminarPartida() {

        if (IDPartidaTextField.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona una partida primero");
            return;
        }

        try {

            int id = Integer.parseInt(IDPartidaTextField.getText());

            int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar esta partida?", "Eliminar partida", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {

                if (GestionPartidas.eliminar(id)) {

                    JOptionPane.showMessageDialog(this, "Partida eliminada correctamente");

                    limpiarCampos();
                    mostrarPartidas();

                } else {

                    JOptionPane.showMessageDialog(this, "Partida no encontrada");
                }
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID de partida incorrecto");

        } catch (SQLException e) {

            if (e.getErrorCode() == 1451) {

                JOptionPane.showMessageDialog(this, "No puedes eliminar esta partida porque " + "tiene participantes inscritos.\n" + "Puedes cancelarla.");

            } else {

                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }


    // BUSQUEDA AVANZADA DE PARTIDAS
    public void buscarPartida() {

        String busqueda = Buscar.getText().trim();

        if (busqueda.isEmpty()) {
            mostrarPartidas();
            return;
        }

        try {

            int opcion = TipoBusquedaPartidas.getSelectedIndex();

            List<Partida> partidas = GestionPartidas.buscarAvanzado(opcion, busqueda);

            mostrarListado(partidas, "No se han encontrado partidas");

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Introduce un numero entero para buscar por ID");

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(this, "Fecha incorrecta. Utiliza AAAA-MM-DD");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al buscar partidas: " + e.getMessage());
        }
    }


    // MOSTRAR TODAS LAS PARTIDAS
    public void mostrarPartidas() {

        try {

            List<Partida> partidas = GestionPartidas.listar();

            mostrarListado(partidas, "No hay partidas guardadas");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al mostrar partidas: " + e.getMessage());
        }
    }


    // MOSTRAR LISTADO
    private void mostrarListado(List<Partida> partidas, String mensajeVacio) throws SQLException {

        Lista.setText("");

        if (partidas.isEmpty()) {

            Lista.setText(mensajeVacio);
            return;
        }

        for (Partida partida : partidas) {

            Juego juego = GestionJuegos.buscar(partida.getIdJuego());

            String nombreJuego = "Desconocido";

            if (juego != null) {
                nombreJuego = juego.getNombre();
            }

            int libres = GestionPartidas.plazasLibres(partida.getId());

            Lista.append("ID: " + partida.getId() + " | Juego: " + nombreJuego + " (ID " + partida.getIdJuego() + ")" + " | Fecha: " + partida.getFecha() + " | Hora: " + partida.getHora() + " | Libres: " + libres + " | Estado: " + partida.getEstado() + "\n");
        }
    }


    // LIMPIAR CAMPOS
    public void limpiarCampos() {

        IDPartidaTextField.setText("");
        Fecha.setText(LocalDate.now().toString());
        Hora.setText("");
        JugadoresDeseados.setText("");
        Buscar.setText("");

        ComboEstado.setSelectedIndex(0);
        ComboJuegos.setSelectedIndex(0);
        TipoBusquedaPartidas.setSelectedIndex(0);
    }


    // CARGAR JUEGOS EN EL COMBOBOX
    public void cargarJuegos() {

        try {

            juegosDelCombo = GestionJuegos.listar();

            ComboJuegos.removeAllItems();
            ComboJuegos.addItem("Selecciona un juego");

            for (Juego juego : juegosDelCombo) {

                ComboJuegos.addItem(juego.getNombre() + " (ID " + juego.getId() + ")");
            }

            ComboJuegos.setSelectedIndex(0);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al cargar juegos: " + e.getMessage());
        }
    }
}
