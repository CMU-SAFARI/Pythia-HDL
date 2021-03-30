/**********************************************************
 * Author: Rahul Bera
 * Description: Implements a 128x16 plane of QVStore
 * Read ports: 2
 * Write ports: 1
 * ********************************************************/

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
    val rdrow0 = Input(UInt(7.W))
    val rdcol0 = Input(UInt(4.W))
    val rddata0 = Output(UInt(16.W))
    val rdrow1 = Input(UInt(7.W))
    val rdcol1 = Input(UInt(4.W))
    val rddata1 = Output(UInt(16.W))
  })

  val mem = Mem(2048, UInt(16.W))

  val rdindex0 = Wire(UInt(11.W))
  val rdindex1 = Wire(UInt(11.W))
  val wrindex = Wire(UInt(11.W))
  rdindex0 := (io.rdrow0 << 4) + io.rdcol0
  rdindex1 := (io.rdrow1 << 4) + io.rdcol1
  wrindex := (io.wrrow << 4) + io.wrcol

  when(io.we) {
    mem(wrindex) := io.wrdata
  }

  io.rddata0 := 0.U
  io.rddata1 := 0.U
  when(io.re) {
    io.rddata0 := mem(rdindex0)
    io.rddata1 := mem(rdindex1)
  }
}
