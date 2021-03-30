/**********************************************************
 * Author: Rahul Bera
 * Description: Implements the index generation circuitry
 * from state information
 * ********************************************************/
package pythia

import chisel3._
import chisel3.util._

class IndexGen extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val offset = Input(UInt(6.W))
    val index = Output(UInt(7.W))
  })

  val tmp1 = Wire(UInt(32.W))
  tmp1 := Cat(io.pc, io.offset)
  io.index := tmp1(6,0)
}
