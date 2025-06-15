package faulttolerance2.exception

import java.io.File

@SerialVersionUID(1L)
class ParseException(msg: String, val file: File)
    extends Exception(msg)
    with Serializable
