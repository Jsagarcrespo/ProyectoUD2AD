package org.example;

import org.example.dao.GestionSocios;
import org.example.models.Socio;

import javax.swing.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class VentanaSocios extends JFrame {

    private JTextField Documento;
    private JTextField Nombre;
    private JTextField Apellidos;
    private JTextField Telefono;
    private JTextField Edad;
    private JTextField Email;

    private JButton añadirButton;
    private JButton modificarButton;
    private JButton eliminarButton;

    private JTextField Buscar;
    private JButton buscarButton;
    private JTextArea textArea1;

    private JButton mostrarTodosButton;
    private JButton verHistorialButton;
    private JButton limpiarButton;
    private JButton cerrarButton;

    private JPanel VentanaSocios;

    // Nuevo JComboBox
    private JComboBox<String> TipoBusquedaSocios;


    public VentanaSocios() {

        setTitle("Gestion de Socios");
        setContentPane(VentanaSocios);
        setSize(750, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        textArea1.setEditable(false);

        // Configurar las opciones del nuevo ComboBox
        TipoBusquedaSocios.setModel(new DefaultComboBoxModel<>(new String[]{"DNI", "Nombre", "Apellidos", "Email", "Edad"}));

        mostrarSocios();


        // AÑADIR
        añadirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anadirSocio();
            }
        });


        // MODIFICAR
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modificarSocio();
            }
        });


        // ELIMINAR
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarSocio();
            }
        });


        // BUSCAR
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarSocio();
            }
        });


        // MOSTRAR TODOS
        mostrarTodosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarSocios();
            }
        });


        // VER HISTORIAL
        verHistorialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verHistorial();
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


        // Selecciona un socio pulsando sobre el listado
        textArea1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    int posicion = textArea1.viewToModel2D(e.getPoint());
                    int linea = textArea1.getLineOfOffset(posicion);

                    int inicio = textArea1.getLineStartOffset(linea);
                    int fin = textArea1.getLineEndOffset(linea);

                    String texto = textArea1.getText(inicio, fin - inicio);

                    if (!texto.startsWith("Documento: ")) {
                        return;
                    }

                    String documento = texto.substring(11, texto.indexOf(" |")).trim();

                    Socio socio = GestionSocios.buscar(documento);

                    if (socio != null) {
                        rellenarCampos(socio);
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(VentanaSocios.this, "Error al seleccionar socio: " + ex.getMessage());
                }
            }
        });
    }


    // RELLENAR CAMPOS
    public void rellenarCampos(Socio socio) {

        Documento.setText(socio.getDocumento());
        Nombre.setText(socio.getNombre());
        Apellidos.setText(socio.getApellidos());

        if (socio.getTelefono() != null) {
            Telefono.setText(socio.getTelefono());
        } else {
            Telefono.setText("");
        }

        Edad.setText(String.valueOf(socio.getEdad()));
        Email.setText(socio.getEmail());

        Documento.setEditable(false);
    }


    // LEER DATOS DEL SOCIO
    public Socio leerSocio() {

        String documento = Documento.getText().trim();
        String nombre = Nombre.getText().trim();
        String apellidos = Apellidos.getText().trim();
        String telefono = Telefono.getText().trim();
        int edad = Integer.parseInt(Edad.getText().trim());
        String email = Email.getText().trim();

        Socio socio = new Socio(documento, nombre, apellidos, telefono, edad, email);

        socio.validar();

        return socio;
    }


    // AÑADIR SOCIO
    public void anadirSocio() {

        try {

            Socio socio = leerSocio();

            GestionSocios.anadir(socio);

            JOptionPane.showMessageDialog(this, "Socio añadido correctamente");

            limpiarCampos();
            mostrarSocios();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "La edad debe ser un numero entero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                JOptionPane.showMessageDialog(this, "Ya existe ese documento o email");

            } else {

                JOptionPane.showMessageDialog(this, "Error al añadir socio: " + e.getMessage());
            }
        }
    }


    // MODIFICAR SOCIO
    public void modificarSocio() {

        try {

            if (Documento.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Selecciona un socio primero");
                return;
            }

            Socio socio = leerSocio();

            if (GestionSocios.modificar(socio)) {

                JOptionPane.showMessageDialog(this, "Socio modificado correctamente");

                limpiarCampos();
                mostrarSocios();

            } else {

                JOptionPane.showMessageDialog(this, "Socio no encontrado");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "La edad debe ser un numero entero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al modificar socio: " + e.getMessage());
        }
    }


    // ELIMINAR SOCIO
    public void eliminarSocio() {

        String documento = Documento.getText().trim();

        if (documento.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona un socio primero");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres eliminar este socio?", "Eliminar socio", JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {

            try {

                if (GestionSocios.eliminar(documento)) {

                    JOptionPane.showMessageDialog(this, "Socio eliminado correctamente");

                    limpiarCampos();
                    mostrarSocios();

                } else {

                    JOptionPane.showMessageDialog(this, "Socio no encontrado");
                }

            } catch (SQLException e) {

                if (e.getErrorCode() == 1451) {

                    JOptionPane.showMessageDialog(this, "No se puede eliminar este socio porque " + "tiene juegos o inscripciones asociados");

                } else {

                    JOptionPane.showMessageDialog(this, "Error al eliminar socio: " + e.getMessage());
                }
            }
        }
    }


    // NUEVO: BUSQUEDA AVANZADA DE SOCIOS
    public void buscarSocio() {

        String busqueda = Buscar.getText().trim();

        if (busqueda.isEmpty()) {
            mostrarSocios();
            return;
        }

        try {

            // Obtener la opcion seleccionada
            int opcion = TipoBusquedaSocios.getSelectedIndex();

            // Buscar en MariaDB utilizando nuestro DAO
            List<Socio> socios = GestionSocios.buscarAvanzado(opcion, busqueda);

            textArea1.setText("");

            for (Socio socio : socios) {

                textArea1.append("Documento: " + socio.getDocumento() + " | Nombre: " + socio.getNombre() + " " + socio.getApellidos() + " | Edad: " + socio.getEdad() + " | Email: " + socio.getEmail() + "\n");
            }

            if (socios.isEmpty()) {

                textArea1.setText("No se han encontrado socios");
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(this, "Para buscar por edad introduce un numero entero");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al buscar socios: " + e.getMessage());
        }
    }


    // MOSTRAR TODOS LOS SOCIOS
    public void mostrarSocios() {

        try {

            List<Socio> socios = GestionSocios.listar();

            textArea1.setText("");

            if (socios.isEmpty()) {

                textArea1.setText("No hay socios guardados");
                return;
            }

            for (Socio socio : socios) {

                textArea1.append("Documento: " + socio.getDocumento() + " | Nombre: " + socio.getNombre() + " " + socio.getApellidos() + " | Edad: " + socio.getEdad() + " | Email: " + socio.getEmail() + "\n");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al mostrar socios: " + e.getMessage());
        }
    }


    // VER HISTORIAL DEL SOCIO
    public void verHistorial() {

        String documento = Documento.getText().trim();

        if (documento.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Selecciona un socio primero");
            return;
        }

        try {

            Socio socio = GestionSocios.buscar(documento);

            if (socio == null) {

                JOptionPane.showMessageDialog(this, "Socio no encontrado");
                return;
            }

            String historial = GestionSocios.historial(documento);

            JTextArea texto = new JTextArea(historial, 15, 50);

            texto.setEditable(false);

            JScrollPane scroll = new JScrollPane(texto);

            JOptionPane.showMessageDialog(this, scroll, "Historial de " + socio.getNombre() + " " + socio.getApellidos(), JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(this, "Error al consultar historial: " + e.getMessage());
        }
    }


    // LIMPIAR CAMPOS
    public void limpiarCampos() {

        Documento.setText("");
        Nombre.setText("");
        Apellidos.setText("");
        Telefono.setText("");
        Edad.setText("");
        Email.setText("");
        Buscar.setText("");

        Documento.setEditable(true);

        // Reiniciar el nuevo ComboBox
        TipoBusquedaSocios.setSelectedIndex(0);
    }
}
