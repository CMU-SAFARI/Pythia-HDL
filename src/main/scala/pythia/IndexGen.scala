/**********************************************************
 * Author: Rahul Bera
 * Description: Implements the index generation circuitry
 * from state information
 * ********************************************************/
package pythia

import chisel3._
import chisel3.util._

class IndexGen (val plane_offset: Int) extends Module {
  val io = IO(new Bundle {
    val pc = Input(UInt(32.W))
    val offset = Input(UInt(6.W))
    val index = Output(UInt(7.W))
  })

  val temp = (Cat(io.pc, io.offset) ^ plane_offset.asUInt)

  // Robert Jenkin's 32 bit mix function for hashing
  val t0 = temp + (temp << 12)
  val t1 = t0   ^ (temp >> 22)
  val t2 = t1   + (temp << 4)
  val t3 = t2   ^ (temp >> 9)
  // val t4 = t3   + (temp << 10)
  // val t5 = t4   ^ (temp >> 2)
  // val t6 = t5   + (temp << 7)
  // val t7 = t6   ^ (temp >> 12)

  // Modulo 128
  io.index := t3(6, 0)
  printf("[INDEXGEN] index %d\n", io.index)
}
