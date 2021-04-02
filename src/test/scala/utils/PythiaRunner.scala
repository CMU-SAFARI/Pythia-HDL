// See LICENSE.txt for license details.
package utils

import scala.collection.mutable.ArrayBuffer
import chisel3.iotesters._

object OptionsCopy {
  def apply(t: TesterOptionsManager): TesterOptionsManager = {
    new TesterOptionsManager {
      testerOptions = t.testerOptions.copy()
      interpreterOptions = t.interpreterOptions.copy()
      chiselOptions = t.chiselOptions.copy()
      firrtlOptions = t.firrtlOptions.copy()
      treadleOptions = t.treadleOptions.copy()
    }
  }
}

object PythiaRunner {
  def apply(section: String, pythiaMap: Map[String, TesterOptionsManager => Boolean], args: Array[String]): Unit = {
    var successful = 0
    val errors = new ArrayBuffer[String]

    val optionsManager = new TesterOptionsManager()
    optionsManager.doNotExitOnHelp()

    optionsManager.parse(args)

    val programArgs = optionsManager.commonOptions.programArgs

    if(programArgs.isEmpty) {
      println("Available Pythia modules")
      for(x <- pythiaMap.keys) {
        println(x)
      }
      println("all")
      System.exit(0)
    }

    val problemsToRun = if(programArgs.exists(x => x.toLowerCase() == "all")) {
      pythiaMap.keys
    }
    else {
      programArgs
    }

    for(testName <- problemsToRun) {
      pythiaMap.get(testName) match {
        case Some(test) =>
          println(s"Starting pythia module $testName")
          try {
            // Start with a (relatively) clean set of options.
            val testOptionsManager = OptionsCopy(optionsManager)
            testOptionsManager.setTopName(testName)
            testOptionsManager.setTargetDirName(s"test_run_dir/$section/$testName")
            if(test(testOptionsManager)) {
              successful += 1
            }
            else {
              errors += s"Pythia module $testName: test error occurred"
            }
          }
          catch {
            case exception: Exception =>
              exception.printStackTrace()
              errors += s"Pythia module $testName: exception ${exception.getMessage}"
            case t : Throwable =>
              errors += s"Pythia module $testName: throwable ${t.getMessage}"
          }
        case _ =>
          errors += s"Bad pythia module name: $testName"
      }

    }
    if(successful > 0) {
      println(s"Pythia modules passing: $successful")
    }
    if(errors.nonEmpty) {
      println("=" * 80)
      println(s"Errors: ${errors.length}: in the following pythia modules")
      println(errors.mkString("\n"))
      println("=" * 80)
      System.exit(1)
    }
  }
}
