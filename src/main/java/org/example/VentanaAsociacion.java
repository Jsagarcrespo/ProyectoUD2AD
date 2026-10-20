
package org.example;

import org.example.dao.GestionAsociacion;
import org.example.models.Asociacion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class VentanaAsociacion extends JFrame {

    private JPanel VentanaAsociacion;
    private JTextField RazonSocial;
    private JTextField CIF;
    private JTextField Direccion;
    private JTextField Telefono;
    private JTextField Email;
    private JTextField Apertura;

    private JButton guardarCambiosButton;
    private JButton recargarButton;
    private JButton cerrarButton;


    public VentanaAsociacion() {

        setTitle("Gestion de Asociacion");
        setContentPane(VentanaAsociacion);
        setSize(650, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Carga los datos al abrir la ventana
        cargarAsociacion();


        // GUARDAR CAMBIOS
        guardarCambiosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                guardarCambios();
            }
        });


        // RECARGAR DATOS
        recargarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                cargarAsociacion();
            }
        });


        // CERRAR VENTANA
        cerrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                dispose();
            }
        });

    }


    // CARGAR DATOS DE LA ASOCIACION
    public void cargarAsociacion() {

        try {

            Asociacion asociacion = GestionAsociacion.obtener();

            if (asociacion != null) {

                RazonSocial.setText(asociacion.getRazonSocial());
                CIF.setText(asociacion.getCif());
                Direccion.setText(asociacion.getDireccion());
                Telefono.setText(asociacion.getTelefono());
                Email.setText(asociacion.getEmail());

                if (asociacion.getFechaApertura() != null) {

                    Apertura.setText(
                            asociacion.getFechaApertura().toString());

                } else {

                    Apertura.setText("");
                }

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se han encontrado datos de la asociacion");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al cargar la asociacion: " + e.getMessage());
        }
    }


    // GUARDAR CAMBIOS
    public void guardarCambios() {

        try {

            String razonSocial = RazonSocial.getText().trim();
            String cif = CIF.getText().trim();
            String direccion = Direccion.getText().trim();
            String telefono = Telefono.getText().trim();
            String email = Email.getText().trim();
            String fechaTexto = Apertura.getText().trim();


            // Comprobamos los campos obligatorios
            if (razonSocial.isEmpty() || cif.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "La razon social y el CIF son obligatorios");

                return;
            }


            // La fecha puede estar vacia
            LocalDate fecha = null;

            if (!fechaTexto.isEmpty()) {

                fecha = LocalDate.parse(fechaTexto);
            }


            // Creamos el objeto Asociacion
            // El ID siempre es 1
            Asociacion asociacion = new Asociacion(
                    1,
                    razonSocial,
                    cif,
                    direccion,
                    telefono,
                    email,
                    fecha
            );


            // Validamos los datos
            asociacion.validar();


            // Modificamos los datos en MariaDB
            if (GestionAsociacion.modificar(asociacion)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Asociacion modificada correctamente");

                cargarAsociacion();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se han realizado cambios o "
                                + "la asociacion no existe");
            }

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Fecha incorrecta. Utiliza AAAA-MM-DD");

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage());

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar los cambios: " + e.getMessage());
        }
    }
}
