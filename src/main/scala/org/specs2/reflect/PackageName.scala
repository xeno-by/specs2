package org.specs2
package reflect


/**
 * Utility function for packages
 */
private[specs2]
trait PackageName {
  implicit class toPackageName(name: String) {
    /** return a directory path from a package name (dot-separated string with no final dot) */
    def toPath = name.replace(".", "/")+"/"
  }
}

private[specs2]
object PackageName extends PackageName