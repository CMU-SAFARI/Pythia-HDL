/**********************************************************
 * Author: Rahul Bera
 * Description: The top-level Pythia module which orchestrates everything
 * ********************************************************/
package pythia

import chisel3._
import chisel3.util._

class MasterModule extends Module
{
   val io = IO(new Bundle {
      val sigUpdate = Input(Bool())
      val sigQuery = Input(Bool())
      // query inputs/outputs
      val qPC = Input(UInt(32.W))
      val qOffset = Input(UInt(6.W))
      val qAction = Output(UInt(4.W))
      // update input/outputs
      val uPC = Input(UInt(32.W))
      val uOffset = Input(UInt(6.W))
      val uAction = Input(UInt(4.W))
      val uReward = Input(UInt(8.W))
   })

   val igModule = Module(new IndexGen()) // index generator
   val plane = Module(new Plane()) // plane
   val max3 = Module(new MaxN()) // max reducer

   // query phase
   io.qAction := 0.U
   when(io.sigQuery)
   {
      var index = Wire(UInt(7.W))
      igModule.io.pc := io.qPC
      igModule.io.offset := io.qOffset
      index = igModule.io.index

      var qvalMax = RegInit(0.U(16.W)) // FIXME: it has to be initialized with very small -ve number
      val actMax = RegInit(0.U(4.W))
      var qval0 = RegInit(0.U(16.W))
      var qval1 = RegInit(0.U(16.W))

      for(i <- 0 until 7)
      {
         // dual read ports
         plane.io.rdrow0 := index
         plane.io.rdcol0 := (2*i).asUInt(4.W)
         plane.io.rdrow1 := index
         plane.io.rdcol1 := ((2*i)+1).asUInt(4.W)
         plane.io.re := true.B
         qval0 := plane.io.rddata0
         qval1 := plane.io.rddata1

         // max reduction
         max3.io.nums(0) := qvalMax
         max3.io.nums(1) := qval0
         max3.io.nums(2) := qval1
         max3.io.ids(0) := actMax
         max3.io.ids(1) := (2*i).asUInt(4.W)
         max3.io.ids(2) := ((2*i)+1).asUInt(4.W)
         qvalMax := max3.io.maxNum
         actMax := max3.io.maxId
      }

      io.qAction := actMax
   }

   // update phase
   when(io.sigUpdate)
   {
      var index = Wire(UInt(7.W))
      var qval = Wire(UInt(16.W))
      var newVal = Wire(UInt(16.W))

      igModule.io.pc := io.qPC
      igModule.io.offset := io.qOffset
      index = igModule.io.index

      plane.io.rdrow0 := index
      plane.io.rdcol0 := io.uAction
      plane.io.rdrow1 := 0.U // dummy
      plane.io.rdcol1 := 0.U // dummy
      plane.io.re := true.B
      qval := plane.io.rddata0

      newVal := (qval << 2) + io.uReward

      plane.io.wrrow := index
      plane.io.wrcol := io.uAction
      plane.io.wrdata := newVal
      plane.io.we := true.B
   }
}
