package org.example;

import org.example.dao.GestionPartidas;
import org.example.models.Partida;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VentanaPartidas extends JFrame {

    private JTextField IDPartidaTextField;
    private JTextField IDJuegoTextField;
    private JTextField Fecha;
    private JTextField Hora;
    private JTextField JugadoresDeseados;
    private JTextField Estado;

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


    public VentanaPartidas() {

        setTitle("Gestion de Partidas");
        setContentPane(VentanaPartidas);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // El ID lo genera MariaDB
        IDPartidaTextField.setEditable(false);

        Lista.setEditable(false);

        Fecha.setText(LocalDate.now().toString());
        Estado.setText("programada");

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


        // Al pulsar una partida del listado
        // carga sus datos, como en UD1
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


    // Rellena los campos con una partida
    public void rellenarCampos(Partida partida) {

        IDPartidaTextField.setText(String.valueOf(partida.getId()));

        IDJuegoTextField.setText(String.valueOf(partida.getIdJuego()));

        Fecha.setText(partida.getFecha().toString());

        Hora.setText(partida.getHora().toString());

        JugadoresDeseados.setText(String.valueOf(partida.getJugadoresDeseados()));

        Estado.setText(partida.getEstado());
    }


    // Recoge los datos y crea un objeto Partida
    public Partida leerPartida(int id) {

        int idJuego = Integer.parseInt(IDJuegoTextField.getText().trim());

        LocalDate fecha = LocalDate.parse(Fecha.getText().trim());

        LocalTime hora = LocalTime.parse(Hora.getText().trim());

        int jugadores = Integer.parseInt(JugadoresDeseados.getText().trim());

        String estado = Estado.getText().trim().toLowerCase();

        return new Partida(id, idJuego, fecha, hora, jugadores, estado);
    }


    // AÑADIR PARTIDA
    public void anadirPartida() {

        try {

            // El ID es 0 porque MariaDB lo genera
            Partida partida = leerPartida(0);

            GestionPartidas.anadir(partida);

            JOptionPane.showMessageDialog(this, "Partida añadida correctamente");

            limpiarCampos();
            mostrarPartidas();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID Juego y jugadores deseados deben ser numeros");

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


    // BUSCAR PARTIDAS
    public void buscarPartida() {

        String busqueda = Buscar.getText().trim().toLowerCase();

        if (busqueda.isEmpty()) {
            mostrarPartidas();
            return;
        }

        try {

            List<Partida> partidas = GestionPartidas.listar();

            Lista.setText("");

            for (Partida partida : partidas) {

                // Busca por ID, juego, fecha o estado
                if (String.valueOf(partida.getId()).equals(busqueda) || String.valueOf(partida.getIdJuego()).equals(busqueda) || partida.getFecha().toString().equals(busqueda) || partida.getEstado().contains(busqueda)) {

                    Lista.append("ID: " + partida.getId() + " | Juego: " + partida.getIdJuego() + " | Fecha: " + partida.getFecha() + " | Hora: " + partida.getHora() + " | Libres: " + GestionPartidas.plazasLibres(partida.getId()) + " | Estado: " + partida.getEstado() + "\n");
                }
            }

            if (Lista.getText().isEmpty()) {
                Lista.setText("No se han encontrado partidas");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al buscar: " + e.getMessage());
        }
    }


    // MOSTRAR TODAS LAS PARTIDAS
    public void mostrarPartidas() {

        try {

            List<Partida> partidas = GestionPartidas.listar();

            Lista.setText("");

            if (partidas.isEmpty()) {

                Lista.setText("No hay partidas guardadas");
                return;
            }

            for (Partida partida : partidas) {

                Lista.append("ID: " + partida.getId() + " | Juego: " + partida.getIdJuego() + " | Fecha: " + partida.getFecha() + " | Hora: " + partida.getHora() + " | Libres: " + GestionPartidas.plazasLibres(partida.getId()) + " | Estado: " + partida.getEstado() + "\n");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al mostrar partidas: " + e.getMessage());
        }
    }


    // LIMPIAR CAMPOS
    public void limpiarCampos() {

        IDPartidaTextField.setText("");
        IDJuegoTextField.setText("");
        Fecha.setText(LocalDate.now().toString());
        Hora.setText("");
        JugadoresDeseados.setText("");
        Estado.setText("programada");
        Buscar.setText("");
    }
}
