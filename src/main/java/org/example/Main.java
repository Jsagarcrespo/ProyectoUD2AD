package org.example;

import org.example.dao.GestionAsociacion;
import org.example.dao.GestionInscripciones;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocias;
import org.example.models.Asociacion;
import org.example.models.Inscripcion;
import org.example.models.Juego;
import org.example.models.Partida;
import org.example.models.Socia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Main {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


    public static void main(String[] args) {

        try {
            menuPrincipal();

        } catch (IOException e) {
            System.out.println("Error al leer el teclado: " + e.getMessage());
        }
    }


    // MENU PRINCIPAL
    public static void menuPrincipal() throws IOException {

        int opcion;

        do {

            System.out.println("\n===== ASOCIACION DE JUEGOS DE MESA =====");

            try {

                Asociacion asociacion = GestionAsociacion.obtener();

                if (asociacion != null) {

                    System.out.println(asociacion.getRazonSocial());
                    System.out.println("CIF: " + asociacion.getCif());
                    System.out.println("Direccion: " + asociacion.getDireccion());
                    System.out.println("Fecha de apertura: " + asociacion.getFechaApertura());
                }

            } catch (SQLException e) {
                mostrarErrorSQL(e);
            }

            System.out.println("1. Gestion de juegos");
            System.out.println("2. Gestion de socias");
            System.out.println("3. Gestion de partidas");
            System.out.println("4. Inscripciones");
            System.out.println("5. Datos de la asociacion");
            System.out.println("6. Exportar a XML");
            System.out.println("7. Exportar a JSON");
            System.out.println("8. Metadatos de la base de datos");
            System.out.println("0. Salir");

            opcion = leerEntero("Elige una opcion: ");

            switch (opcion) {

                case 1:
                    menuJuegos();
                    break;

                case 2:
                    menuSocias();
                    break;

                case 3:
                    menuPartidas();
                    break;

                case 4:
                    menuInscripciones();
                    break;

                case 5:
                    menuAsociacion();
                    break;

                case 6:

                    try {

                        GestionXML.exportarTodo();

                        System.out.println("XML exportados en la carpeta ficheros");

                    } catch (SQLException e) {
                        mostrarErrorSQL(e);

                    } catch (IOException e) {
                        System.out.println("Error al guardar XML: " + e.getMessage());
                    }

                    break;

                case 7:

                    try {

                        GestionJSON.exportarTodo();

                        System.out.println("JSON exportados en la carpeta ficheros");

                    } catch (SQLException e) {
                        mostrarErrorSQL(e);

                    } catch (IOException e) {
                        System.out.println("Error al guardar JSON: " + e.getMessage());
                    }

                    break;

                case 8:

                    try {

                        GestionMetadatos.mostrar();
                        GestionMetadatos.exportar();

                        System.out.println("Metadatos guardados en ficheros/Metadatos.txt");

                    } catch (SQLException e) {
                        mostrarErrorSQL(e);

                    } catch (IOException e) {
                        System.out.println("Error al guardar metadatos: " + e.getMessage());
                    }

                    break;

                case 0:
                    System.out.println("Hasta pronto");
                    break;

                default:
                    System.out.println("Opcion incorrecta");
            }

        } while (opcion != 0);
    }


    // MENU DE JUEGOS
    public static void menuJuegos() throws IOException {

        int opcion;

        do {

            System.out.println("\n===== GESTION DE JUEGOS =====");
            System.out.println("1. Listar juegos");
            System.out.println("2. Buscar juego por ID");
            System.out.println("3. Añadir juego");
            System.out.println("4. Modificar juego");
            System.out.println("5. Eliminar juego");
            System.out.println("6. Buscar juegos por nombre");
            System.out.println("7. Buscar juegos por estado");
            System.out.println("8. Juegos de una socia");
            System.out.println("9. Juegos de la asociacion");
            System.out.println("0. Volver");

            opcion = leerEntero("Opcion: ");

            try {

                switch (opcion) {

                    case 1:

                        mostrarJuegos(org.example.dao.GestionJuegos.listar());
                        break;

                    case 2: {

                        int id = leerEntero("ID del juego: ");

                        Juego juego = org.example.dao.GestionJuegos.buscar(id);

                        if (juego == null) {
                            System.out.println("Juego no encontrado");
                        } else {
                            mostrarJuego(juego);
                        }

                        break;
                    }

                    case 3: {

                        Juego juego = leerDatosJuego(0, LocalDate.now());

                        org.example.dao.GestionJuegos.anadir(juego);

                        System.out.println("Juego añadido correctamente");
                        break;
                    }

                    case 4: {

                        int id = leerEntero("ID del juego a modificar: ");

                        Juego anterior = org.example.dao.GestionJuegos.buscar(id);

                        if (anterior == null) {
                            System.out.println("Juego no encontrado");
                            break;
                        }

                        mostrarJuego(anterior);

                        Juego nuevo = leerDatosJuego(id, anterior.getFechaAlta());

                        if (org.example.dao.GestionJuegos.modificar(nuevo)) {
                            System.out.println("Juego modificado");
                        } else {
                            System.out.println("No se ha modificado");
                        }

                        break;
                    }

                    case 5: {

                        int id = leerEntero("ID del juego a eliminar: ");

                        if (confirmar("¿Eliminar el juego? (s/n): ")) {

                            if (org.example.dao.GestionJuegos.eliminar(id)) {
                                System.out.println("Juego eliminado");
                            } else {
                                System.out.println("Juego no encontrado");
                            }
                        }

                        break;
                    }

                    case 6: {

                        String nombre = leerTexto("Texto a buscar: ");

                        mostrarJuegos(org.example.dao.GestionJuegos.buscarPorNombre(nombre));
                        break;
                    }

                    case 7: {

                        String estado = leerTexto("Estado (disponible/prestado/reparacion): ");

                        mostrarJuegos(org.example.dao.GestionJuegos.listarPorEstado(estado));
                        break;
                    }

                    case 8: {

                        String documento = leerTexto("Documento de la socia: ");

                        mostrarJuegos(org.example.dao.GestionJuegos.listarPorPropietaria(documento));
                        break;
                    }

                    case 9:

                        mostrarJuegos(org.example.dao.GestionJuegos.listarDeLaAsociacion());
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println("Opcion incorrecta");
                }

            } catch (SQLException e) {
                mostrarErrorSQL(e);

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (opcion != 0);
    }


    // LEE LOS DATOS DE UN JUEGO
    private static Juego leerDatosJuego(int id, LocalDate fechaAlta) throws IOException {

        String nombre = leerTexto("Nombre: ");
        String autores = leerTexto("Autor o autores: ");
        String descripcion = leerOpcional("Descripcion: ");

        int minimo = leerEntero("Minimo de jugadores: ");
        int maximo = leerEntero("Maximo de jugadores: ");
        int duracion = leerEntero("Duracion en minutos: ");

        String tematica = leerOpcional("Tematica: ");
        String tipo = leerTexto("Tipo (competitivo/cooperativo/mixto): ");
        String ubicacion = leerOpcional("Ubicacion: ");
        String estado = leerTexto("Estado (disponible/prestado/reparacion): ");

        String propietaria = leerOpcional("Documento propietaria (vacio = asociacion): ");

        if (propietaria.isEmpty()) {
            propietaria = null;
        }

        return new Juego(id, nombre, autores, descripcion, minimo, maximo, duracion, tematica, tipo, ubicacion, estado, propietaria, fechaAlta);
    }


    // MUESTRA UNA LISTA DE JUEGOS
    private static void mostrarJuegos(List<Juego> juegos) {

        if (juegos.isEmpty()) {
            System.out.println("No hay juegos para mostrar");
            return;
        }

        for (Juego juego : juegos) {
            mostrarJuego(juego);
        }
    }


    // MUESTRA LOS DATOS DE UN JUEGO
    private static void mostrarJuego(Juego juego) {

        System.out.println("\nID: " + juego.getId() + " | " + juego.getNombre());

        System.out.println("Autores: " + juego.getAutores());
        System.out.println("Descripcion: " + juego.getDescripcion());

        System.out.println("Jugadores: " + juego.getMinJugadores() + " - " + juego.getMaxJugadores());

        System.out.println("Duracion: " + juego.getDuracionMin() + " minutos");

        System.out.println("Tematica: " + juego.getTematica() + " | Tipo: " + juego.getTipo());

        System.out.println("Ubicacion: " + juego.getUbicacion() + " | Estado: " + juego.getEstado());

        System.out.println("Propietaria: " + (juego.getDocumentoPropietaria() == null ? "Asociacion" : juego.getDocumentoPropietaria()));

        System.out.println("Fecha de alta: " + juego.getFechaAlta());
    }


    // MENU DE SOCIAS
    public static void menuSocias() throws IOException {

        int opcion;

        do {

            System.out.println("\n===== GESTION DE SOCIAS =====");
            System.out.println("1. Listar socias");
            System.out.println("2. Buscar socia por documento");
            System.out.println("3. Añadir socia");
            System.out.println("4. Modificar socia");
            System.out.println("5. Eliminar socia");
            System.out.println("6. Consultar juegos y partidas");
            System.out.println("7. Buscar por nombre o apellidos");
            System.out.println("0. Volver");

            opcion = leerEntero("Opcion: ");

            try {

                switch (opcion) {

                    case 1:

                        mostrarSocias(GestionSocias.listar());
                        break;

                    case 2: {

                        Socia socia = GestionSocias.buscar(leerTexto("Documento: "));

                        if (socia == null) {
                            System.out.println("Socia no encontrada");
                        } else {
                            mostrarSocia(socia);
                        }

                        break;
                    }

                    case 3: {

                        String documento = leerTexto("Documento: ");

                        Socia socia = leerDatosSocia(documento);

                        GestionSocias.anadir(socia);

                        System.out.println("Socia añadida correctamente");
                        break;
                    }

                    case 4: {

                        String documento = leerTexto("Documento a modificar: ");

                        Socia socia = GestionSocias.buscar(documento);

                        if (socia == null) {
                            System.out.println("Socia no encontrada");
                            break;
                        }

                        mostrarSocia(socia);

                        if (GestionSocias.modificar(leerDatosSocia(documento))) {

                            System.out.println("Socia modificada correctamente");
                        } else {
                            System.out.println("No se ha modificado la socia");
                        }

                        break;
                    }

                    case 5: {

                        String documento = leerTexto("Documento a eliminar: ");

                        if (confirmar("¿Eliminar la socia? (s/n): ")) {

                            if (GestionSocias.eliminar(documento)) {
                                System.out.println("Socia eliminada");
                            } else {
                                System.out.println("Socia no encontrada");
                            }
                        }

                        break;
                    }

                    case 6: {

                        String documento = leerTexto("Documento: ");

                        if (GestionSocias.buscar(documento) == null) {
                            System.out.println("Socia no encontrada");
                        } else {
                            System.out.println(GestionSocias.historial(documento));
                        }

                        break;
                    }

                    case 7:

                        mostrarSocias(GestionSocias.buscarPorNombre(leerTexto("Nombre o apellidos: ")));
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println("Opcion incorrecta");
                }

            } catch (SQLException e) {
                mostrarErrorSQL(e);

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (opcion != 0);
    }


    // LEE LOS DATOS DE UNA SOCIA
    private static Socia leerDatosSocia(String documento) throws IOException {

        String nombre = leerTexto("Nombre: ");
        String apellidos = leerTexto("Apellidos: ");
        String telefono = leerOpcional("Telefono: ");
        int edad = leerEntero("Edad: ");
        String email = leerTexto("Email: ");

        return new Socia(documento, nombre, apellidos, telefono, edad, email);
    }


    // MUESTRA UNA LISTA DE SOCIAS
    private static void mostrarSocias(List<Socia> socias) {

        if (socias.isEmpty()) {
            System.out.println("No hay socias para mostrar");
            return;
        }

        for (Socia socia : socias) {
            mostrarSocia(socia);
        }
    }


    // MUESTRA UNA SOCIA
    private static void mostrarSocia(Socia socia) {

        System.out.println("\nDocumento: " + socia.getDocumento());

        System.out.println("Nombre: " + socia.getNombre() + " " + socia.getApellidos());

        System.out.println("Telefono: " + socia.getTelefono());

        System.out.println("Edad: " + socia.getEdad() + " | Email: " + socia.getEmail());
    }


    // MENU DE PARTIDAS
    public static void menuPartidas() throws IOException {

        int opcion;

        do {

            System.out.println("\n===== GESTION DE PARTIDAS =====");
            System.out.println("1. Listar partidas con plazas libres");
            System.out.println("2. Buscar partida por ID");
            System.out.println("3. Añadir partida");
            System.out.println("4. Modificar partida");
            System.out.println("5. Cancelar partida");
            System.out.println("6. Eliminar partida");
            System.out.println("7. Finalizar partida");
            System.out.println("8. Buscar partidas por juego");
            System.out.println("9. Buscar partidas por fecha");
            System.out.println("10. Buscar partidas por estado");
            System.out.println("0. Volver");

            opcion = leerEntero("Opcion: ");

            try {

                switch (opcion) {

                    case 1: {

                        String listado = GestionPartidas.listadoConPlazas();

                        if (listado.isEmpty()) {
                            System.out.println("No hay partidas");
                        } else {
                            System.out.println(listado);
                        }

                        break;
                    }

                    case 2: {

                        Partida partida = GestionPartidas.buscar(leerEntero("ID de partida: "));

                        if (partida == null) {
                            System.out.println("Partida no encontrada");
                        } else {
                            mostrarPartida(partida);
                        }

                        break;
                    }

                    case 3:

                        GestionPartidas.anadir(leerDatosPartida(0, "programada"));

                        System.out.println("Partida añadida correctamente");
                        break;

                    case 4: {

                        int id = leerEntero("ID de partida a modificar: ");

                        Partida anterior = GestionPartidas.buscar(id);

                        if (anterior == null) {
                            System.out.println("Partida no encontrada");
                            break;
                        }

                        mostrarPartida(anterior);

                        Partida nueva = leerDatosPartida(id, anterior.getEstado());

                        if (GestionPartidas.modificar(nueva)) {
                            System.out.println("Partida modificada");
                        } else {
                            System.out.println("No se ha modificado la partida");
                        }

                        break;
                    }

                    case 5: {

                        int id = leerEntero("ID de partida a cancelar: ");

                        if (confirmar("¿Cancelar la partida? (s/n): ")) {

                            if (GestionPartidas.cancelar(id)) {
                                System.out.println("Partida cancelada");
                            } else {
                                System.out.println("Partida no encontrada");
                            }
                        }

                        break;
                    }

                    case 6: {

                        int id = leerEntero("ID de partida a eliminar: ");

                        if (confirmar("¿Eliminar la partida? (s/n): ")) {

                            if (GestionPartidas.eliminar(id)) {
                                System.out.println("Partida eliminada");
                            } else {
                                System.out.println("Partida no encontrada");
                            }
                        }

                        break;
                    }

                    case 7: {

                        int id = leerEntero("ID de partida a finalizar: ");

                        if (GestionPartidas.finalizar(id)) {
                            System.out.println("Partida finalizada");
                        } else {
                            System.out.println("La partida no existe o no esta programada");
                        }

                        break;
                    }

                    case 8:

                        mostrarPartidas(GestionPartidas.listarPorJuego(leerEntero("ID del juego: ")));
                        break;

                    case 9:

                        mostrarPartidas(GestionPartidas.listarPorFecha(leerFecha("Fecha (AAAA-MM-DD): ")));
                        break;

                    case 10:

                        mostrarPartidas(GestionPartidas.listarPorEstado(leerTexto("Estado (programada/finalizada/cancelada): ")));
                        break;

                    case 0:
                        break;

                    default:
                        System.out.println("Opcion incorrecta");
                }

            } catch (SQLException e) {
                mostrarErrorSQL(e);

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (opcion != 0);
    }


    // LEE LOS DATOS DE UNA PARTIDA
    private static Partida leerDatosPartida(int id, String estado) throws IOException {

        int idJuego = leerEntero("ID del juego: ");
        LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
        LocalTime hora = leerHora("Hora (HH:mm): ");
        int deseados = leerEntero("Numero de jugadores deseados: ");

        return new Partida(id, idJuego, fecha, hora, deseados, estado);
    }


    // MUESTRA UNA LISTA DE PARTIDAS
    private static void mostrarPartidas(List<Partida> partidas) throws SQLException {

        if (partidas.isEmpty()) {
            System.out.println("No hay partidas para mostrar");
            return;
        }

        for (Partida partida : partidas) {
            mostrarPartida(partida);
        }
    }


    // MUESTRA UNA PARTIDA
    private static void mostrarPartida(Partida partida) throws SQLException {

        Juego juego = org.example.dao.GestionJuegos.buscar(partida.getIdJuego());

        System.out.println("\nPartida ID: " + partida.getId());

        System.out.println("Juego: " + (juego == null ? partida.getIdJuego() : juego.getNombre()));

        System.out.println("Fecha: " + partida.getFecha() + " | Hora: " + partida.getHora());

        System.out.println("Jugadores deseados: " + partida.getJugadoresDeseados());

        System.out.println("Plazas libres: " + GestionPartidas.plazasLibres(partida.getId()));

        System.out.println("Estado: " + partida.getEstado());
    }


    // MENU DE INSCRIPCIONES
    public static void menuInscripciones() throws IOException {

        int opcion;

        do {

            System.out.println("\n===== INSCRIPCIONES =====");
            System.out.println("1. Ver participantes de una partida");
            System.out.println("2. Inscribir socia");
            System.out.println("3. Inscribir invitada");
            System.out.println("4. Quitar inscripcion");
            System.out.println("5. Marcar o desmarcar ganadora");
            System.out.println("6. Ver inscripciones de una socia");
            System.out.println("0. Volver");

            opcion = leerEntero("Opcion: ");

            try {

                switch (opcion) {

                    case 1: {

                        int id = leerEntero("ID de la partida: ");

                        if (GestionPartidas.buscar(id) == null) {
                            System.out.println("Partida no encontrada");
                            break;
                        }

                        String participantes = GestionInscripciones.participantes(id);

                        if (participantes.isEmpty()) {
                            System.out.println("No hay participantes");
                        } else {
                            System.out.println(participantes);
                        }

                        break;
                    }

                    case 2: {

                        int partida = leerEntero("ID de la partida: ");

                        String documento = leerTexto("Documento de la socia: ");

                        GestionInscripciones.inscribirSocia(partida, documento);

                        System.out.println("Socia inscrita correctamente");
                        break;
                    }

                    case 3: {

                        int partida = leerEntero("ID de la partida: ");

                        String nombre = leerTexto("Nombre de la invitada: ");

                        GestionInscripciones.inscribirInvitada(partida, nombre);

                        System.out.println("Invitada inscrita correctamente");
                        break;
                    }

                    case 4: {

                        int id = leerEntero("ID de la inscripcion: ");

                        if (confirmar("¿Quitar inscripcion? (s/n): ")) {

                            if (GestionInscripciones.quitar(id)) {
                                System.out.println("Inscripcion eliminada");
                            } else {
                                System.out.println("Inscripcion no encontrada");
                            }
                        }

                        break;
                    }

                    case 5: {

                        int id = leerEntero("ID de la inscripcion: ");

                        boolean ganadora = confirmar("¿Marcar como ganadora? (s/n): ");

                        if (GestionInscripciones.marcarGanadora(id, ganadora)) {

                            System.out.println("Resultado actualizado");
                        } else {
                            System.out.println("Inscripcion no encontrada");
                        }

                        break;
                    }

                    case 6: {

                        String documento = leerTexto("Documento de la socia: ");

                        List<Inscripcion> lista = GestionInscripciones.listarPorSocia(documento);

                        if (lista.isEmpty()) {
                            System.out.println("No hay inscripciones para esa socia");
                        }

                        for (Inscripcion inscripcion : lista) {

                            System.out.println("Inscripcion " + inscripcion.getId() + " | Partida " + inscripcion.getIdPartida() + (inscripcion.isGanadora() ? " | Ganadora" : ""));
                        }

                        break;
                    }

                    case 0:
                        break;

                    default:
                        System.out.println("Opcion incorrecta");
                }

            } catch (SQLException e) {
                mostrarErrorSQL(e);

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (opcion != 0);
    }


    // MENU DE ASOCIACION
    public static void menuAsociacion() throws IOException {

        int opcion;

        do {

            System.out.println("\n===== DATOS DE LA ASOCIACION =====");

            System.out.println("1. Mostrar datos");
            System.out.println("2. Modificar datos");
            System.out.println("0. Volver");

            opcion = leerEntero("Opcion: ");

            try {

                switch (opcion) {

                    case 1: {

                        Asociacion asociacion = GestionAsociacion.obtener();

                        if (asociacion == null) {
                            System.out.println("No existe la asociacion");
                        } else {
                            mostrarAsociacion(asociacion);
                        }

                        break;
                    }

                    case 2: {

                        Asociacion anterior = GestionAsociacion.obtener();

                        if (anterior == null) {
                            System.out.println("No existe la asociacion");
                            break;
                        }

                        mostrarAsociacion(anterior);

                        String nombre = leerTexto("Razon social: ");
                        String cif = leerTexto("CIF: ");
                        String direccion = leerOpcional("Direccion: ");
                        String telefono = leerOpcional("Telefono: ");
                        String email = leerOpcional("Email: ");

                        Asociacion nueva = new Asociacion(anterior.getId(), nombre, cif, direccion, telefono, email, anterior.getFechaApertura());

                        if (GestionAsociacion.modificar(nueva)) {
                            System.out.println("Asociacion modificada correctamente");
                        } else {
                            System.out.println("No se ha modificado la asociacion");
                        }

                        break;
                    }

                    case 0:
                        break;

                    default:
                        System.out.println("Opcion incorrecta");
                }

            } catch (SQLException e) {
                mostrarErrorSQL(e);

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (opcion != 0);
    }


    // MUESTRA LOS DATOS DE LA ASOCIACION
    private static void mostrarAsociacion(Asociacion asociacion) {

        System.out.println("\nRazon social: " + asociacion.getRazonSocial());

        System.out.println("CIF: " + asociacion.getCif());

        System.out.println("Direccion: " + asociacion.getDireccion());

        System.out.println("Telefono: " + asociacion.getTelefono());

        System.out.println("Email: " + asociacion.getEmail());

        System.out.println("Fecha de apertura: " + asociacion.getFechaApertura());
    }


    // LEE UN TEXTO OPCIONAL
    private static String leerOpcional(String mensaje) throws IOException {

        System.out.print(mensaje);

        String texto = br.readLine();

        if (texto == null) {
            throw new IOException("Entrada de teclado cerrada");
        }

        return texto.trim();
    }


    // LEE UN TEXTO OBLIGATORIO
    private static String leerTexto(String mensaje) throws IOException {

        while (true) {

            String texto = leerOpcional(mensaje);

            if (!texto.isEmpty()) {
                return texto;
            }

            System.out.println("Este dato es obligatorio");
        }
    }


    // LEE UN NUMERO ENTERO
    private static int leerEntero(String mensaje) throws IOException {

        while (true) {

            String texto = leerTexto(mensaje);

            try {
                return Integer.parseInt(texto);

            } catch (NumberFormatException e) {
                System.out.println("Introduce un numero entero correcto");
            }
        }
    }


    // LEE UNA FECHA
    private static LocalDate leerFecha(String mensaje) throws IOException {

        while (true) {

            String texto = leerTexto(mensaje);

            try {
                return LocalDate.parse(texto);

            } catch (DateTimeParseException e) {
                System.out.println("Fecha incorrecta. Ejemplo: 2026-10-20");
            }
        }
    }


    // LEE UNA HORA
    private static LocalTime leerHora(String mensaje) throws IOException {

        while (true) {

            String texto = leerTexto(mensaje);

            try {
                return LocalTime.parse(texto);

            } catch (DateTimeParseException e) {
                System.out.println("Hora incorrecta. Ejemplo: 17:00");
            }
        }
    }


    // PIDE UNA CONFIRMACION
    private static boolean confirmar(String mensaje) throws IOException {

        while (true) {

            String respuesta = leerTexto(mensaje);

            if (respuesta.equalsIgnoreCase("s")) {
                return true;
            }

            if (respuesta.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("Escribe s o n");
        }
    }


    // MUESTRA LOS ERRORES DE MARIA DB
    private static void mostrarErrorSQL(SQLException e) {

        if (e.getErrorCode() == 1062) {

            System.out.println("Ya existe un registro con ese documento, " + "email u otro dato unico");

        } else if (e.getErrorCode() == 1451) {

            System.out.println("No se puede eliminar: tiene datos relacionados");

        } else if (e.getErrorCode() == 1452) {

            System.out.println("El juego, la socia o la partida indicada no existe");

        } else {

            System.out.println("Error de base de datos: " + e.getMessage());
        }
    }
}
