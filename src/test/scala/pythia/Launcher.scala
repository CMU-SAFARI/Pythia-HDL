// See LICENSE.txt for license details.
package pythia

import chisel3._
import chisel3.iotesters.{Driver, TesterOptionsManager}
import utils.PythiaRunner

object Launcher {
  val tests = Map(
    "IndexGen" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new IndexGen(), manager) {
        (c) => new IndexGenTests(c)
      }
    },
    "MaxN" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new MaxN(), manager) {
        (c) => new MaxNTests(c)
      }
    },
    "Plane" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new Plane(), manager) {
        (c) => new PlaneTests(c)
      }
    },
    "Vault" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new Vault(), manager) {
        (c) => new VaultTests(c)
      }
    },
    "MasterModule" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new MasterModule(), manager) {
        (c) => new MasterModuleTests(c)
      }
    },
    "QVCompare" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new QVCompare(), manager) {
        (c) => new QVCompareTests(c)
      }
    }
  )

  def main(args: Array[String]): Unit = {
    PythiaRunner("pythia", tests, args)
  }
}
