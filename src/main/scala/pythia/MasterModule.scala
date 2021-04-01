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

   // machine states
   val ( s_idle
         :: s_update_planeR :: s_update_planeW
         :: s_query_read2 :: s_query_read4 :: s_query_read6 :: s_query_read8 :: s_query_read10 :: s_query_read12 :: s_query_read14 :: s_query_read16
         :: Nil) = Enum(11)
   val state = RegInit(s_idle)

   val update_reward = RegEnable(io.uReward, io.sigUpdate)

   // Init
   igModule.io.pc := Mux(io.sigUpdate, io.uPC, io.qPC)
   igModule.io.offset := Mux(io.sigUpdate, io.uOffset, io.qOffset)
   // igModule.io.pc := RegEnable(io.uPC, io.sigUpdate)
   // igModule.io.offset := RegEnable(io.uOffset, io.sigUpdate)

   // plane.io.rdrow0 := Mux(io.sigUpdate, igModule.io.index, 0.U)
   when(io.sigUpdate){
      plane.io.rdrow0 := igModule.io.index
   }.elsewhen(io.sigQuery){
      plane.io.rdrow0 := igModule.io.index
   }.otherwise{
      plane.io.rdrow0 := 0.U
   }
   when(io.sigUpdate){
      plane.io.rdcol0 := io.uAction
   }.elsewhen(io.sigQuery){
      when(state === s_query_read2)          { plane.io.rdcol0 := 0.U }
      .elsewhen(state === s_query_read4)     { plane.io.rdcol0 := 2.U }
      .elsewhen(state === s_query_read6)     { plane.io.rdcol0 := 4.U }
      .elsewhen(state === s_query_read8)     { plane.io.rdcol0 := 6.U }
      .elsewhen(state === s_query_read10)    { plane.io.rdcol0 := 8.U }
      .elsewhen(state === s_query_read12)    { plane.io.rdcol0 := 10.U }
      .elsewhen(state === s_query_read14)    { plane.io.rdcol0 := 12.U }
      .elsewhen(state === s_query_read16)    { plane.io.rdcol0 := 14.U }
      .otherwise                             { plane.io.rdcol0 := 0.U }
   }.otherwise{
      plane.io.rdcol0 := 0.U
   }

   when(io.sigUpdate){
      plane.io.rdrow1 := 0.U
   }.elsewhen(io.sigQuery){
      plane.io.rdrow1 := igModule.io.index
   }.otherwise{
      plane.io.rdrow1 := 0.U
   }

   when(io.sigUpdate){
      plane.io.rdcol1 := 0.U
   }.elsewhen(io.sigQuery){
      when(state === s_query_read2)          { plane.io.rdcol1 := 1.U }
      .elsewhen(state === s_query_read4)     { plane.io.rdcol1 := 3.U }
      .elsewhen(state === s_query_read6)     { plane.io.rdcol1 := 5.U }
      .elsewhen(state === s_query_read8)     { plane.io.rdcol1 := 7.U }
      .elsewhen(state === s_query_read10)    { plane.io.rdcol1 := 9.U }
      .elsewhen(state === s_query_read12)    { plane.io.rdcol1 := 11.U }
      .elsewhen(state === s_query_read14)    { plane.io.rdcol1 := 13.U }
      .elsewhen(state === s_query_read16)    { plane.io.rdcol1 := 15.U }
      .otherwise                             { plane.io.rdcol1 := 0.U }
   }.otherwise{
      plane.io.rdcol1 := 0.U
   }
   // plane.io.rdrow1 := 0.U
   // plane.io.rdcol1 := 0.U
   plane.io.re := false.B

   plane.io.wrrow := RegEnable(igModule.io.index, io.sigUpdate)
   plane.io.wrcol := RegEnable(io.uAction, io.sigUpdate)
   plane.io.wrdata := RegEnable((plane.io.rddata0 >> 1) + update_reward, state === s_update_planeR)
   plane.io.we := false.B

   when(io.sigQuery){
      when(state === s_idle) {max3.io.nums(0) := 0.U}
      .otherwise {max3.io.nums(0) := RegNext(max3.io.maxNum)}
   }.otherwise{
      max3.io.nums(0) := 0.U
   }
   when(io.sigQuery){
      max3.io.nums(1) := plane.io.rddata0
   }.otherwise{
      max3.io.nums(1) := 0.U
   }
   when(io.sigQuery){
      max3.io.nums(2) := plane.io.rddata1
   }.otherwise{
      max3.io.nums(2) := 0.U
   }

   when(io.sigQuery){
      when(state === s_idle) {max3.io.ids(0) := 0.U}
      .otherwise {max3.io.ids(0) := RegNext(max3.io.maxId)}
   }.otherwise{
      max3.io.ids(0) := 0.U
   }
   when(io.sigQuery){
      when(state === s_query_read2)            {max3.io.ids(1) := 0.U}
      .elsewhen(state === s_query_read4)       {max3.io.ids(1) := 2.U}
      .elsewhen(state === s_query_read6)       {max3.io.ids(1) := 4.U}
      .elsewhen(state === s_query_read8)       {max3.io.ids(1) := 6.U}
      .elsewhen(state === s_query_read10)      {max3.io.ids(1) := 8.U}
      .elsewhen(state === s_query_read12)      {max3.io.ids(1) := 10.U}
      .elsewhen(state === s_query_read14)      {max3.io.ids(1) := 12.U}
      .elsewhen(state === s_query_read16)      {max3.io.ids(1) := 14.U}
      .otherwise                               {max3.io.ids(1) := 0.U}
   }.otherwise{
      max3.io.ids(1) := 0.U
   }
   when(io.sigQuery){
      when(state === s_query_read2)            {max3.io.ids(2) := 1.U}
      .elsewhen(state === s_query_read4)       {max3.io.ids(2) := 3.U}
      .elsewhen(state === s_query_read6)       {max3.io.ids(2) := 5.U}
      .elsewhen(state === s_query_read8)       {max3.io.ids(2) := 7.U}
      .elsewhen(state === s_query_read10)      {max3.io.ids(2) := 9.U}
      .elsewhen(state === s_query_read12)      {max3.io.ids(2) := 11.U}
      .elsewhen(state === s_query_read14)      {max3.io.ids(2) := 13.U}
      .elsewhen(state === s_query_read16)      {max3.io.ids(2) := 15.U}
      .otherwise                               {max3.io.ids(2) := 0.U}
   }.otherwise{
      max3.io.ids(2) := 0.U
   }
   // max3.io.ids(1) := 0.U
   // max3.io.ids(2) := 0.U

   // query phase
   io.qAction := 0.U
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

   // Query state transitions


   // update phase
   // val (s_idle :: s_indexgen :: s_planeread :: s_compute :: s_planewrite :: Nil) = Enum(5)

   // when(io.sigUpdate)
   // {
   //    printf("[UPDATE] pc %x offset %d action %d reward %d\n", io.uPC, io.uOffset, io.uAction, io.uReward)
   //
   //    igModule.io.pc := io.uPC
   //    igModule.io.offset := io.uOffset
   //    // ===================== read stage ==================== //
   //    plane.io.rdrow0 := igModule.io.index
   //    plane.io.rdcol0 := io.uAction
   //    plane.io.rdrow1 := 0.U // dummy
   //    plane.io.rdcol1 := 0.U // dummy
   //    plane.io.re := true.B
   //    val state_index = RegNext(igModule.io.index)
   //    val action_index = RegNext(io.uAction)
   //    val updated_qval = RegNext((plane.io.rddata0 >> 1) + io.uReward)
   //    printf("[UPDATE READ] row_index %d col_index %d value-read %d\n", igModule.io.index, io.uAction, plane.io.rddata0)
   //    printf("[UPDATE] registered qval %d\n", updated_qval)
   //
   //    // ===================== write stage ==================== //
   //    plane.io.wrrow := state_index
   //    plane.io.wrcol := action_index
   //    plane.io.wrdata := updated_qval
   //    plane.io.we := true.B
   //    printf("[UPDATE WRITE] row_index %d col_index %d value-written %d\n", plane.io.wrrow, plane.io.wrcol, plane.io.wrdata)
   //    printf("===================================================\n\n\n")
   // }

   // update state transition
   when(state === s_idle){
      printf("[IDLE] Idle state\n")
      plane.io.re := false.B
      plane.io.we := false.B
      when (io.sigUpdate){
         state := s_update_planeR
      }.elsewhen(io.sigQuery){
         state := s_query_read2
      }
   }
   .elsewhen(state === s_update_planeR){
      plane.io.re := true.B // read the old value from plane
      plane.io.we := false.B
      state := s_update_planeW
      printf("[UPDATE-PLANE-R] row %d col %d value-read %d\n", plane.io.rdrow0, plane.io.rdcol0, plane.io.rddata0)
      // printf("[UPDATE-PLANE-R] io.uReward %d\n", io.uReward)
   }
   .elsewhen(state === s_update_planeW){
      plane.io.re := false.B
      plane.io.we := true.B // write the new value
      state := s_idle
      printf("[UPDATE-PLANE-W] row %d col %d value-written %d\n", plane.io.wrrow, plane.io.wrcol, plane.io.wrdata)
   }
   // query state transitions
   .elsewhen(state === s_query_read2 || state === s_query_read4 || state === s_query_read6 || state === s_query_read8 || state === s_query_read10 || state === s_query_read12 || state === s_query_read14 || state === s_query_read16){
      plane.io.re := true.B
      printf("[QUERY-PLANE] row0 %d col0 %d val0 %d\n", plane.io.rdrow0, plane.io.rdcol0, plane.io.rddata0)
      printf("[QUERY-PLANE] row1 %d col1 %d val1 %d\n", plane.io.rdrow1, plane.io.rdcol1, plane.io.rddata1)
      printf("[QUERY-PLANE] max3 %d maxId %d\n", max3.io.maxNum, max3.io.maxId)

      when(state === s_query_read2)          { state := s_query_read4 }
      .elsewhen(state === s_query_read4)     { state := s_query_read6 }
      .elsewhen(state === s_query_read6)     { state := s_query_read8 }
      .elsewhen(state === s_query_read8)     { state := s_query_read10 }
      .elsewhen(state === s_query_read10)    { state := s_query_read12 }
      .elsewhen(state === s_query_read12)    { state := s_query_read14 }
      .elsewhen(state === s_query_read14)    { state := s_query_read16 }
      .elsewhen(state === s_query_read16)    { io.qAction:= max3.io.maxId; state := s_idle }
      .otherwise                             { state := s_idle }
   }
   // .elsewhen(state === s_query_read4){
   //    plane.io.re := true.B
   //    printf("[QUERY-PLANE-2] row0 %d col0 %d val0 %d\n", plane.io.rdrow0, plane.io.rdcol0, plane.io.rddata0)
   //    printf("[QUERY-PLANE-2] row1 %d col1 %d val1 %d\n", plane.io.rdrow1, plane.io.rdcol1, plane.io.rddata1)
   //    state := s_idle
   // }
}
