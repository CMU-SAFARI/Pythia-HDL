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
      // val qPC = Input(UInt(32.W))
      // val qOffset = Input(UInt(6.W))
      // val qAction = Output(UInt(4.W))
      // update input/outputs
      val uPC = Input(UInt(32.W))
      val uOffset = Input(UInt(6.W))
      val uAction = Input(UInt(4.W))
      val uReward = Input(UInt(8.W))
   })

   val igModule = Module(new IndexGen()) // index generator
   val plane = Module(new Plane()) // plane
   // val max3 = Module(new MaxN()) // max reducer

   // Init
   igModule.io.pc := 0.U
   igModule.io.offset := 0.U
   plane.io.rdrow0 := 0.U
   plane.io.rdcol0 := 0.U
   plane.io.rdrow1 := 0.U
   plane.io.rdcol1 := 0.U
   plane.io.re := 0.U
   plane.io.wrrow := 0.U
   plane.io.wrcol := 0.U
   plane.io.wrdata := 0.U
   plane.io.we := 0.U
   // max3.io.nums(0) := 0.U
   // max3.io.nums(1) := 0.U
   // max3.io.nums(2) := 0.U
   // max3.io.ids(0) := 0.U
   // max3.io.ids(1) := 0.U
   // max3.io.ids(2) := 0.U

   // query phase
   // io.qAction := 0.U
   // when(io.sigQuery)
   // {
   //    printf("[QUERY] pc %x offset %d action %d\n", io.qPC, io.qOffset, io.qAction)
   //
   //    var index = RegInit(0.U(7.W))
   //    igModule.io.pc := io.qPC
   //    igModule.io.offset := io.qOffset
   //    index = igModule.io.index
   //    printf("[QUERY] index %d\n", index)
   //
   //    var qvalMax = RegInit(0.U(16.W)) // FIXME: it has to be initialized with very small -ve number
   //    val actMax = RegInit(0.U(4.W))
   //    var qval0 = RegInit(0.U(16.W))
   //    var qval1 = RegInit(0.U(16.W))
   //
   //    for(i <- 0 until 7)
   //    {
   //       // dual read ports
   //       plane.io.rdrow0 := index
   //       plane.io.rdcol0 := (2*i).asUInt(4.W)
   //       plane.io.rdrow1 := index
   //       plane.io.rdcol1 := ((2*i)+1).asUInt(4.W)
   //       plane.io.re := true.B
   //       qval0 := plane.io.rddata0
   //       qval1 := plane.io.rddata1
   //
   //       // max reduction
   //       printf("[QUERY] iter %d qvalMax %d, actMax %d\n", i.asUInt, qvalMax, actMax)
   //       printf("[QUERY] iter %d qval0 %d, qval1 %d\n", i.asUInt, qval0, qval1)
   //       max3.io.nums(0) := qvalMax
   //       max3.io.nums(1) := qval0
   //       max3.io.nums(2) := qval1
   //       max3.io.ids(0) := actMax
   //       max3.io.ids(1) := (2*i).asUInt(4.W)
   //       max3.io.ids(2) := ((2*i)+1).asUInt(4.W)
   //       qvalMax := max3.io.maxNum
   //       actMax := max3.io.maxId
   //       printf("[QUERY] qvalMax %d, actMax %d\n", qvalMax, actMax)
   //    }
   //
   //    io.qAction := actMax
   //    printf("[QUERY] qAction %d\n", io.qAction)
   // }

   // update phase
   // val (s_idle :: s_indexgen :: s_planeread :: s_compute :: s_planewrite :: Nil) = Enum(5)

   when(io.sigUpdate)
   {
      printf("[UPDATE] pc %x offset %d action %d reward %d\n", io.uPC, io.uOffset, io.uAction, io.uReward)

      igModule.io.pc := io.uPC
      igModule.io.offset := io.uOffset
      // ===================== read stage ==================== //
      plane.io.rdrow0 := igModule.io.index
      plane.io.rdcol0 := io.uAction
      plane.io.rdrow1 := 0.U // dummy
      plane.io.rdcol1 := 0.U // dummy
      plane.io.re := true.B
      val state_index = RegNext(igModule.io.index)
      val action_index = RegNext(io.uAction)
      val updated_qval = RegNext((plane.io.rddata0 >> 1) + io.uReward)
      printf("[UPDATE READ] row_index %d col_index %d value-read %d\n", igModule.io.index, io.uAction, plane.io.rddata0)
      printf("[UPDATE] registered qval %d\n", updated_qval)

      // ===================== write stage ==================== //
      plane.io.wrrow := state_index
      plane.io.wrcol := action_index
      plane.io.wrdata := 42.U
      plane.io.we := true.B
      printf("[UPDATE WRITE] row_index %d col_index %d value-written %d\n", plane.io.wrrow, plane.io.wrcol, plane.io.wrdata)
      printf("===================================================\n\n\n")
   }
}
