/**********************************************************
 * Author: Rahul Bera
 * Description: Implements a valut of a QVStore.
 * Num of planes in a valut: 3
 * Each plane supports two read ports, and one write port
 * ********************************************************/
package pythia

import chisel3._
import chisel3.util._

class Valut extends Module {
   val io = IO(new Bundle{
      val re = Input(Bool())
      val rdrow0 = Input(Vec(3, UInt(7.W)))
      val rdcol0 = Input(UInt(4.W))
      val rdrow1 = Input(Vec(3, UInt(7.W)))
      val rdcol1 = Input(UInt(4.W))
      val rddata0 = Output(UInt(16.W))
      val rddata1 = Output(UInt(16.W))
      val we = Input(Bool())
      val wrrow = Input(Vec(3, UInt(7.W)))
      val wrcol = Input(UInt(4.W))
      val wrdata = Input(UInt(16.W))
   })

   // constituent planes
   val plane0 = Module(new Plane())
   val plane1 = Module(new Plane())
   val plane2 = Module(new Plane())

   plane0.io.rdrow0 := io.rdrow0(0)
   plane0.io.rdcol0 := io.rdcol0
   plane0.io.rdrow1 := io.rdrow1(0)
   plane0.io.rdcol1 := io.rdcol1
   plane0.io.re     := io.re

   plane1.io.rdrow0 := io.rdrow0(1)
   plane1.io.rdcol0 := io.rdcol0
   plane1.io.rdrow1 := io.rdrow1(1)
   plane1.io.rdcol1 := io.rdcol1
   plane1.io.re     := io.re

   plane2.io.rdrow0 := io.rdrow0(2)
   plane2.io.rdcol0 := io.rdcol0
   plane2.io.rdrow1 := io.rdrow1(2)
   plane2.io.rdcol1 := io.rdcol1
   plane2.io.re     := io.re

   // For each port, the final read value returned by the vault
   // will be the sum of value returned by each constituent port
   io.rddata0 := plane0.io.rddata0 + plane1.io.rddata0 + plane2.io.rddata0
   io.rddata1 := plane0.io.rddata1 + plane1.io.rddata1 + plane2.io.rddata1

   plane0.io.wrrow  := io.wrrow(0)
   plane0.io.wrcol  := io.wrcol
   plane0.io.wrdata := io.wrdata
   plane0.io.we     := io.we

   plane1.io.wrrow  := io.wrrow(1)
   plane1.io.wrcol  := io.wrcol
   plane1.io.wrdata := io.wrdata
   plane1.io.we     := io.we

   plane2.io.wrrow  := io.wrrow(2)
   plane2.io.wrcol  := io.wrcol
   plane2.io.wrdata := io.wrdata
   plane2.io.we     := io.we
}
