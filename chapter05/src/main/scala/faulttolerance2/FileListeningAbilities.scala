package faulttolerance2

trait FileListeningAbilities {
  def register(uri: String): Unit =
    println(s"Registering directory: $uri")
}
