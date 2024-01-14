import java.io.File

val RUTA: String = File("contactos.csv").absoluteFile.parentFile.absolutePath

const val NOMBRE_FICHERO = "contactos.csv"

val RUTA_FICHERO = File(RUTA, NOMBRE_FICHERO).toString()

val OPCIONES_MENU = setOf(1, 2, 3, 4, 5, 6, 7, 8)

fun borrarConsola() {
    /**
     * Función que limpia la consola cuando es invocada
     */
    val os = System.getProperty("os.name").lowercase()

    when {
        os.contains("nix") || os.contains("nux") || os.contains("mac") -> ProcessBuilder("clear").inheritIO().start().waitFor()
        os.contains("win") -> ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor()
    }
}

fun cargarContactos(contactos: MutableList<HashMap<String, Any>>) {
    /**
     * Función que carga los contactos iniciales de la agenda desde un fichero, en primer lugar a cada línea del fichero
     * se le aplica el método lines.forEach para separar el texto en en cuatro campos (nombre, apellido, email y telefonos)
     * , eliminandpse sus espacios vacíos con el método trim() y siendo delimitandos teniendo en cuenta el carácter ';'
     * @return the new size of the group.
     */
    val archivoContactos = File(RUTA_FICHERO)

    if (archivoContactos.exists()) {
        try {
            archivoContactos.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val campos = line.trim().split(';')
                    val nombre = campos[0].trim().capitalize()
                    val apellido = campos[1].trim().capitalize()
                    val email = campos[2].trim()
                    val telefonos = campos.drop(3).map { it.trim() }.filter { it.isNotEmpty() }

                    val nuevoContacto = hashMapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "email" to email,
                        "telefonos" to telefonos
                    )
                    contactos.add(nuevoContacto)
                }
            }
            println("Contactos cargados correctamente.")
        } catch (e: Exception) {
            println("Error al cargar contactos: $e")
        }
    } else {
        println("El archivo de contactos no existe.")
    }
}


fun buscarContacto(contactos: List<HashMap<String, Any>>, email: String): Int? {
    /**
     * Busca la posición de un contacto con un email determinado
     * @param email parámetro email referente a un contacto de la lista.
     * @type String
     * @return retorna el iterable del contacto cuyo email es el solicitado, o valor nulo en caso de no ser encontrado
     */
    for ((i, contacto) in contactos.withIndex()) {
        if (contacto["email"] == email) {
            return i
        }
    }
    return null
}

fun modificarContacto(contactos: MutableList<HashMap<String, Any>>, email: String) {
    /**
     * Modifica un contacto de la agenda utilizando el iterable retornado de buscarContacto() como
     * posición para poder actualizar los datos del contacto que queramos modificar
     * @param email parámetro email referente a un contacto de la lista.
     * @type String
     * @return el nuevo contacto o una excepción
     */
    try {
        val pos = buscarContacto(contactos, email)
        if (pos != null) {
            println("Contacto encontrado. Proporcione los nuevos datos:")
            val nuevoContacto = pedirDatosContacto()
            contactos[pos] = nuevoContacto
            println("Contacto modificado correctamente.")
        } else {
            println("No se encontró el contacto para modificar")
        }
    } catch (e: Exception) {
        println("**Error** $e")
        println("No se modificó ningún contacto")
    }
}

fun eliminarContacto(contactos: MutableList<HashMap<String, Any>>, email: String) {
    /**
     * Elimina un contacto de la agenda utilizando el iterable retornado de buscar_contacto() como posición para poder encontrar
     * los datos del contacto que queramos eliminar, sabiendo la posición se utiliza 'del' para poder eliminar el contacto asociado
     * @param email parámetro email referente a un contacto de la lista.
     * @type String
     * @return El contacto encontrado, un mensaje si no se encuentra un contacto y una excepción de error si no se elimina ningún contacto
     */
    try {
        val pos = buscarContacto(contactos, email)
        if (pos != null) {
            contactos.removeAt(pos)
            println("Se eliminó 1 contacto")
        } else {
            println("No se encontró el contacto para eliminar")
        }
    } catch (e: Exception) {
        println("**Error** $e")
        println("No se eliminó ningún contacto")
    }
}

fun mostrarMenu() {
    println("""
    AGENDA
    ------
    1. Nuevo contacto
    2. Modificar contacto
    3. Eliminar contacto
    4. Vaciar agenda
    5. Cargar agenda inicial
    6. Mostrar contactos por criterio
    7. Mostrar la agenda completa
    8. Salir
    """.trimIndent())
}

fun pedirOpcion(): Int {
    /**
     * Pide al usuario que seleccione una opción del menú, mediante un bucle pide al usuario que introduzca
     * una opción que sea un número, además de que se encuentre dentro del conjunto 'OPCIONES_MENU'
     * @return Valor de opción
     */
    print("Seleccione una opción: ")
    var opcion = readLine()?.toIntOrNull() ?: 0
    while (opcion !in OPCIONES_MENU) {
        println("Opción no válida. Intente nuevamente.")
        print("Seleccione una opción: ")
        opcion = readLine()?.toIntOrNull() ?: 0
    }
    return opcion
}

fun pedirEmail(contactos: List<HashMap<String, Any>>): String {
    /**
     * Función en la que se introduce el parámetro email, y se devuelve la cadena.
     */
    while (true) {
        print("Email: ")
        val email = readLine()?.trim() ?: ""

        try {
            validarEmail(email, contactos)
            return email
        } catch (e: IllegalArgumentException) {
            println("Error: $e")
        }
    }
}

fun validarEmail(email: String, contactos: List<HashMap<String, Any>>) {
    /**
     * Función que valida el parámetro email introducido y se asegura que cumpla las condiciones de este parámetro.
     * @param email parámetro email referente a un contacto de la lista.
     * @type String
     * @return IllegalArgumentException en caso de que se de alguna de las tres condiciones
     */
    if (email.isBlank()) {
        throw IllegalArgumentException("El email no puede ser una cadena vacía")
    }

    if ('@' !in email || '.' !in email) {
        throw IllegalArgumentException("El email no es un correo válido")
    }

    if (contactos.any { it["email"] == email }) {
        throw IllegalArgumentException("El email ya está en uso")
    }
}

fun validarTelefono(telefono: String): Boolean {
    /**
     * Función que comprueba telefono, si este no tiene un prefijo o tiene un prefijo '+34' o '+34-' y si es una
     * cadena númerica de nueve digitos
     * @param telefono parámetro telefono referente a un contacto de la lista
     * @type String
     * @return Boolean, True si se cumple alguna de las tres condiciones, False en cualquier otro caso
     */
    if (telefono.all { it.isDigit() } && telefono.length == 9) {
        return true
    }

    if (telefono.startsWith("+34") && telefono.substring(3).all { it.isDigit() } && telefono.substring(3).length == 9) {
        return true
    }

    if (telefono.startsWith("+34-") && telefono.substring(4).all { it.isDigit() } && telefono.substring(4).length == 9) {
        return true
    }

    return false
}

fun agregarContacto(contactos: MutableList<HashMap<String, Any>>) {
    /**
     * Función que agrega un nuevo contacto a la agenda
     */
    val nuevoContacto = pedirDatosContacto()
    contactos.add(nuevoContacto)
}

fun pedirDatosContacto(): HashMap<String, Any> {
    /**
     * Pide al usuario los datos para un nuevo contacto
     */
    print("Nombre: ")
    val nombre = readLine()?.trim()?.capitalize() ?: ""

    print("Apellido: ")
    val apellido = readLine()?.trim()?.capitalize() ?: ""

    val email = pedirEmail(listOf())

    val telefonos = mutableListOf<String>()
    var telefono: String

    do {
        print("Teléfono (deje en blanco para terminar): ")
        telefono = readLine()?.trim()?.replace(" ", "") ?: ""
        if (telefono.isNotBlank() && !validarTelefono(telefono)) {
            println("Teléfono no válido. Intente nuevamente.")
        } else if (telefono.isNotBlank()) {
            telefonos.add(telefono)
        }
    } while (telefono.isNotBlank())

    return hashMapOf(
        "nombre" to nombre,
        "apellido" to apellido,
        "email" to email,
        "telefonos" to telefonos
    )
}

fun mostrarContactos(contactos: List<HashMap<String, Any>>) {
    /**
     * Muestra todos los contactos de la agenda, primero se ordena la lista de contactos por nombre antes de mostrarlos,
     * luego se muestra la cantidad de contactos ordenados, finalmente puede mostrar cada contacto con el formato requerido
     * o mostrar un mensaje indicando que la agenda está vacía.
     */
    val contactosOrdenados = contactos.sortedBy { it["nombre"] as String }

    println("AGENDA (${contactosOrdenados.size})")
    println("------")

    for (contacto in contactosOrdenados) {
        println("Nombre: ${contacto["nombre"]} ${contacto["apellido"]} (${contacto["email"]})")
        val telefonos = (contacto["telefonos"] as List<*>).joinToString(" / ") { it as String }
        println("Teléfonos: $telefonos")
        println("......")
    }

    if (contactosOrdenados.isEmpty()) {
        println("La agenda está vacía.")
    }
}

fun mostrarContactosPorCriterio(contactos: List<HashMap<String, Any>>, criterio: String, valor: String) {
    /**
     * Muestra los contactos de la agenda que coinciden con un criterio específico
     */
    val contactosCoincidentes = mutableListOf<HashMap<String, Any>>()

    for (contacto in contactos) {
        val valorContacto = when (criterio.lowercase()) {
            "nombre" -> contacto["nombre"] as String
            "apellido" -> contacto["apellido"] as String
            "email" -> contacto["email"] as String
            "telefono" -> (contacto["telefonos"] as List<*>).joinToString(" / ")
            else -> ""
        }

        if (valorContacto.lowercase().contains(valor.lowercase())) {
            contactosCoincidentes.add(contacto)
        }
    }

    if (contactosCoincidentes.isNotEmpty()) {
        println("Contactos que coinciden con el criterio '$criterio':")
        mostrarContactos(contactosCoincidentes)
    } else {
        println("No se encontraron contactos que coincidan con el criterio '$criterio $valor'.")
    }
}

fun vaciarAgenda(contactos: MutableList<HashMap<String, Any>>) {
    /**
     * Vacia la lista de contactos de la agenda usando el método .clear(), y siendo confirmado posteriormente
     */
    print("¿Está seguro de que desea vaciar la agenda? (S/N): ")
    val confirmacion = readLine()?.trim()?.lowercase() ?: ""
    if (confirmacion == "s") {
        contactos.clear()
        println("Agenda vaciada correctamente.")
    } else {
        println("Operación cancelada.")
    }
}

fun cargarAgendaInicial(contactos: MutableList<HashMap<String, Any>>) {
    /**
     * Carga la agenda con contactos iniciales desde el archivo contactos.csv, borrando la agenda actual primero
     */
    print("¿Está seguro de que desea cargar la agenda inicial? (S/N): ")
    val confirmacion = readLine()?.trim()?.lowercase() ?: ""
    if (confirmacion == "s") {
        val archivoContactos = File(RUTA_FICHERO)

        if (archivoContactos.exists()) {
            try {
                archivoContactos.bufferedReader().useLines { lines ->
                    contactos.clear()

                    lines.forEach { line ->
                        val datosContacto = line.trim().split(';')
                        if (datosContacto.size >= 3) {
                            val nuevoContacto = hashMapOf(
                                "nombre" to datosContacto[0],
                                "apellido" to datosContacto[1],
                                "email" to datosContacto[2],
                                "telefonos" to datosContacto.drop(3)
                            )
                            contactos.add(nuevoContacto)
                        } else {
                            println("Advertencia: La línea '$line' no tiene suficientes campos y será ignorada.")
                        }
                    }
                }
                println("Agenda inicial cargada correctamente.")
            } catch (e: Exception) {
                println("Error al cargar la agenda inicial: $e")
            }
        } else {
            println("El archivo de contactos no existe.")
        }
    } else {
        println("Operación cancelada.")
    }
}

fun agenda(contactos: MutableList<HashMap<String, Any>>) {
    /**
     * Carga la agenda con contactos iniciales desde un archivo, primero se limpia la lista actual de contactos
     * con el método .clear() antes de cargar los nuevos, después de realiza un bucle for para cargar poder cargar los contactos
     *  originales del archivo
     */
    var salirDeAgenda = false
    while (!salirDeAgenda) {
        mostrarMenu()
        val opcion = pedirOpcion()

        when (opcion) {
            1 -> agregarContacto(contactos)
            2 -> {
                print("Ingrese el email del contacto a modificar: ")
                val emailAModificar = readLine()?.trim() ?: ""
                modificarContacto(contactos, emailAModificar)
            }
            3 -> {
                print("Ingrese el email del contacto a eliminar: ")
                val emailAEliminar = readLine()?.trim() ?: ""
                eliminarContacto(contactos, emailAEliminar)
            }
            4 -> vaciarAgenda(contactos)
            5 -> cargarAgendaInicial(contactos)
            6 -> {
                print("Ingrese el criterio de búsqueda (nombre, apellido, email o telefono): ")
                val criterio = readLine()?.trim()?.lowercase() ?: ""
                print("Ingrese el valor a buscar: ")
                val valor = readLine()?.trim() ?: ""
                mostrarContactosPorCriterio(contactos, criterio, valor)
            }
            7 -> mostrarContactos(contactos)
            8 -> {
                println("\nSaliendo de la agenda . . . ")
                salirDeAgenda = true
            }
            else -> println("Opción no válida. Intente nuevamente.")
        }

        pulseTeclaParaContinuar()
        borrarConsola()
    }
}

fun pulseTeclaParaContinuar() {
    /**
     * Muestra un mensaje y realiza una pausa hasta que se pulse una tecla
     */
    println("\n")
    print("Presione Enter para continuar...")
    readLine()
}

fun main() {
    /**
     * Función principal del programa donde se utiliza borrarConsola() para una mayor legibilidad, se cargan los
     * contactos, y se ejecuta la función agenda
     */
    borrarConsola()

    val contactos = mutableListOf<HashMap<String, Any>>()

    cargarContactos(contactos)

    agenda(contactos)
}
