// Author: Rahul Bera
// Description: Implements a 128x16 plane of QVStore
package pythia

import chisel3._
import chisel3.util._

class Plane extends Module {
  val io = IO(new Bundle {
    val we = Input(Bool())
    val wrrow = Input(UInt(7.W))
    val wrcol = Input(UInt(4.W))
    val wrdata = Input(UInt(16.W))
    val re = Input(Bool())
    val rdrow = Input(UInt(7.W))
    val rdcol = Input(UInt(4.W))
    val rddata = Output(UInt(16.W))
  })

  val mem = Mem(2048, UInt(16.W))

  val rdindex = Wire(UInt(11.W))
  val wrindex = Wire(UInt(11.W))
  rdindex := (io.rdrow << 4) + io.rdcol
  wrindex := (io.wrrow << 4) + io.wrcol

  when(io.we) {
    mem(wrindex) := io.wrdata
  }

  io.rddata := 0.U
  when(io.re) {
    io.rddata := mem(rdindex)
  }
}
