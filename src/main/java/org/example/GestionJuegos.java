package org.example;

import org.example.models.Juego;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class GestionJuegos extends JFrame {

    private JTextField NombreJuego;
    private JTextField Autores;
    private JTextField Descripcion;
    private JTextField MaxJugadores;
    private JTextField MinJugadors;
    private JTextField Duracion;
    private JTextField Tematica;
    private JTextField Tipo;
    private JTextField Ubicacion;
    private JTextField Estado;
    private JTextField Propietario;
    private JTextField FechaAlta;

    private JButton añadirButton;
    private JButton eliminarButton;
    private JButton modificarButton;
    private JTextField BuscarTextField;
    private JButton BUSCARButton;
    private JTextArea Juegos;
    private JButton mostrarTodoButton;
    private JButton limpiarButton;
    private JButton cerrarButton;

    private JTextField IdJuego;
    private JPanel GestionJuegos;


    public GestionJuegos() {

        setTitle("Gestion de juegos");
        setContentPane(GestionJuegos);
        setSize(850, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // El ID lo crea MariaDB
        IdJuego.setEditable(false);

        // El listado no se puede modificar
        Juegos.setEditable(false);

        FechaAlta.setText(LocalDate.now().toString());
        Estado.setText("disponible");

        mostrarJuegos();


        // Listener para añadir
        añadirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anadirJuego();
            }
        });


        // Listener para modificar
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarJuego();
            }
        });


        // Listener para eliminar
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarJuego();
            }
        });


        // Listener para buscar
        BUSCARButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarJuego();
            }
        });


        // Listener para mostrar todos
        mostrarTodoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarJuegos();
            }
        });


        // Listener para limpiar
        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });


        // Listener para cerrar
        cerrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        // Selecciona un juego pulsando sobre el listado
        // Igual que en VentanaLibros de UD1
        Juegos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    int posicion = Juegos.viewToModel2D(e.getPoint());

                    int linea = Juegos.getLineOfOffset(posicion);

                    int inicio = Juegos.getLineStartOffset(linea);
                    int fin = Juegos.getLineEndOffset(linea);

                    String texto = Juegos.getText(inicio, fin - inicio);

                    if (!texto.startsWith("ID: ")) {
                        return;
                    }

                    String numero = texto.substring(4, texto.indexOf(" |"));

                    int id = Integer.parseInt(numero.trim());

                    Juego juego = org.example.dao.GestionJuegos.buscar(id);

                    if (juego != null) {
                        rellenarCampos(juego);
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(GestionJuegos.this, "Error al seleccionar juego: " + ex.getMessage());
                }
            }
        });
    }


    // Rellena los campos con los datos del juego seleccionado
    public void rellenarCampos(Juego juego) {

        IdJuego.setText(String.valueOf(juego.getId()));

        NombreJuego.setText(juego.getNombre());

        Autores.setText(juego.getAutores());

        Descripcion.setText(juego.getDescripcion() == null ? "" : juego.getDescripcion());

        MinJugadors.setText(String.valueOf(juego.getMinJugadores()));

        MaxJugadores.setText(String.valueOf(juego.getMaxJugadores()));

        Duracion.setText(String.valueOf(juego.getDuracionMin()));

        Tematica.setText(juego.getTematica() == null ? "" : juego.getTematica());

        Tipo.setText(juego.getTipo());

        Ubicacion.setText(juego.getUbicacion() == null ? "" : juego.getUbicacion());

        Estado.setText(juego.getEstado());

        Propietario.setText(juego.getDocumentoPropietario() == null ? "" : juego.getDocumentoPropietario());

        FechaAlta.setText(juego.getFechaAlta().toString());
    }


    // Recoge los datos y crea un objeto Juego
    // Igual que haciamos con el constructor de Libro
    private Juego leerJuego(int id) {

        String nombre = NombreJuego.getText().trim();
        String autores = Autores.getText().trim();
        String descripcion = Descripcion.getText().trim();

        int minimo = Integer.parseInt(MinJugadors.getText().trim());

        int maximo = Integer.parseInt(MaxJugadores.getText().trim());

        int duracion = Integer.parseInt(Duracion.getText().trim());

        String tematica = Tematica.getText().trim();
        String tipo = Tipo.getText().trim().toLowerCase();
        String ubicacion = Ubicacion.getText().trim();
        String estado = Estado.getText().trim().toLowerCase();
        String propietario = Propietario.getText().trim();

        LocalDate fecha = LocalDate.parse(FechaAlta.getText().trim());

        // Si no hay propietario, pertenece a la asociacion
        if (propietario.isEmpty()) {
            propietario = null;
        }

        Juego juego = new Juego(id, nombre, autores, descripcion, minimo, maximo, duracion, tematica, tipo, ubicacion, estado, propietario, fecha);

        juego.validar();

        return juego;
    }


    // Añade un juego a MariaDB
    public void anadirJuego() {

        try {

            // El ID es 0 porque lo genera MariaDB
            Juego juego = leerJuego(0);

            org.example.dao.GestionJuegos.anadir(juego);

            JOptionPane.showMessageDialog(this, "Juego añadido correctamente");

            limpiarCampos();
            mostrarJuegos();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Minimo, maximo y duracion deben ser numeros");

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(this, "Fecha incorrecta. Utiliza AAAA-MM-DD");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al añadir juego: " + e.getMessage());
        }
    }


    // Modifica un juego existente
    public void modificarJuego() {

        try {

            if (IdJuego.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona un juego primero");
                return;
            }

            int id = Integer.parseInt(IdJuego.getText());

            Juego juego = leerJuego(id);

            if (org.example.dao.GestionJuegos.modificar(juego)) {

                JOptionPane.showMessageDialog(this, "Juego modificado correctamente");

                limpiarCampos();
                mostrarJuegos();

            } else {

                JOptionPane.showMessageDialog(this, "Juego no encontrado");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Minimo, maximo y duracion deben ser numeros");

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(this, "Fecha incorrecta. Utiliza AAAA-MM-DD");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al modificar juego: " + e.getMessage());
        }
    }


    // Elimina un juego despues de pedir confirmacion
    public void eliminarJuego() {

        try {

            if (IdJuego.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona un juego primero");
                return;
            }

            int id = Integer.parseInt(IdJuego.getText());

            int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar este juego?", "Eliminar juego", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {

                if (org.example.dao.GestionJuegos.eliminar(id)) {

                    JOptionPane.showMessageDialog(this, "Juego eliminado correctamente");

                    limpiarCampos();
                    mostrarJuegos();

                } else {

                    JOptionPane.showMessageDialog(this, "Juego no encontrado");
                }
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "ID del juego incorrecto");

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "No se puede eliminar el juego. " + "Puede tener partidas asociadas.\n" + e.getMessage());
        }
    }


    // Busca juegos por ID, nombre o autores
    public void buscarJuego() {

        String busqueda = BuscarTextField.getText().trim().toLowerCase();

        if (busqueda.isEmpty()) {
            mostrarJuegos();
            return;
        }

        try {

            List<Juego> juegos = org.example.dao.GestionJuegos.listar();

            Juegos.setText("");

            for (Juego juego : juegos) {

                if (String.valueOf(juego.getId()).equals(busqueda) || juego.getNombre().toLowerCase().contains(busqueda) || juego.getAutores().toLowerCase().contains(busqueda)) {

                    Juegos.append("ID: " + juego.getId() + " | Nombre: " + juego.getNombre() + " | Autores: " + juego.getAutores() + " | Estado: " + juego.getEstado() + "\n");
                }
            }

            if (Juegos.getText().isEmpty()) {
                Juegos.setText("No se han encontrado juegos");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al buscar: " + e.getMessage());
        }
    }


    // Muestra todos los juegos guardados en MariaDB
    public void mostrarJuegos() {

        try {

            List<Juego> juegos = org.example.dao.GestionJuegos.listar();

            Juegos.setText("");

            for (Juego juego : juegos) {

                Juegos.append("ID: " + juego.getId() + " | Nombre: " + juego.getNombre() + " | Autores: " + juego.getAutores() + " | Estado: " + juego.getEstado() + "\n");
            }

            if (juegos.isEmpty()) {
                Juegos.setText("No hay juegos guardados");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al mostrar juegos: " + e.getMessage());
        }
    }


    // Limpia todos los campos
    public void limpiarCampos() {

        IdJuego.setText("");
        NombreJuego.setText("");
        Autores.setText("");
        Descripcion.setText("");
        MinJugadors.setText("");
        MaxJugadores.setText("");
        Duracion.setText("");
        Tematica.setText("");
        Tipo.setText("");
        Ubicacion.setText("");
        Estado.setText("disponible");
        Propietario.setText("");
        FechaAlta.setText(LocalDate.now().toString());
        BuscarTextField.setText("");
    }
}
