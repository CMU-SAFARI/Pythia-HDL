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

   val pcoIndexGen0 = Module(new IndexGen(iType = 1, plane_offset = 0xdead)) // index generator
   val pcoIndexGen1 = Module(new IndexGen(iType = 1, plane_offset = 0xdada)) // index generator
   val pcoIndexGen2 = Module(new IndexGen(iType = 1, plane_offset = 0xdeaf)) // index generator
   // val plane = Module(new Plane()) // plane
   val vault = Module(new Vault()) // vault
   val qvcompare0 = Module(new QVCompare()) // Q-value comparator
   val qvcompare1 = Module(new QVCompare()) // Q-value comparator
   val max3 = Module(new MaxN()) // max reducer

   // machine states
   val ( s_idle
         :: s_update_planeR :: s_update_planeW
         :: s_query_read2 :: s_query_read4 :: s_query_read6 :: s_query_read8 :: s_query_read10 :: s_query_read12 :: s_query_read14 :: s_query_read16
         :: Nil) = Enum(11)
   val state = RegInit(s_idle)

   val update_reward = RegEnable(io.uReward, io.sigUpdate)

   //=========== Connections for Index Generation Modules ============//
   pcoIndexGen0.io.pc := Mux(io.sigUpdate, io.uPC, io.qPC)
   pcoIndexGen0.io.offset := Mux(io.sigUpdate, io.uOffset, io.qOffset)
   pcoIndexGen0.io.delta_path <> DontCare
   pcoIndexGen1.io.pc := Mux(io.sigUpdate, io.uPC, io.qPC)
   pcoIndexGen1.io.offset := Mux(io.sigUpdate, io.uOffset, io.qOffset)
   pcoIndexGen1.io.delta_path <> DontCare
   pcoIndexGen2.io.pc := Mux(io.sigUpdate, io.uPC, io.qPC)
   pcoIndexGen2.io.offset := Mux(io.sigUpdate, io.uOffset, io.qOffset)
   pcoIndexGen2.io.delta_path <> DontCare
   //=================================================================//

   //=================== Connections for Vault Module ====================//
   when(io.sigUpdate){
      vault.io.rdrow0(0) := pcoIndexGen0.io.index; vault.io.rdrow0(1) := pcoIndexGen1.io.index; vault.io.rdrow0(2) := pcoIndexGen2.io.index;
   }.elsewhen(io.sigQuery){
      vault.io.rdrow0(0) := pcoIndexGen0.io.index; vault.io.rdrow0(1) := pcoIndexGen1.io.index; vault.io.rdrow0(2) := pcoIndexGen2.io.index;
   }.otherwise{
      vault.io.rdrow0(0) := 0.U; vault.io.rdrow0(1) := 0.U; vault.io.rdrow0(2) := 0.U;
   }

   when(io.sigUpdate){
      vault.io.rdcol0 := io.uAction
   }.elsewhen(io.sigQuery){
      when(state === s_query_read2)          { vault.io.rdcol0 := 0.U }
      .elsewhen(state === s_query_read4)     { vault.io.rdcol0 := 2.U }
      .elsewhen(state === s_query_read6)     { vault.io.rdcol0 := 4.U }
      .elsewhen(state === s_query_read8)     { vault.io.rdcol0 := 6.U }
      .elsewhen(state === s_query_read10)    { vault.io.rdcol0 := 8.U }
      .elsewhen(state === s_query_read12)    { vault.io.rdcol0 := 10.U }
      .elsewhen(state === s_query_read14)    { vault.io.rdcol0 := 12.U }
      .elsewhen(state === s_query_read16)    { vault.io.rdcol0 := 14.U }
      .otherwise                             { vault.io.rdcol0 := 0.U }
   }.otherwise{
      vault.io.rdcol0 := 0.U
   }

   when(io.sigUpdate){
      vault.io.rdrow1(0) := 0.U; vault.io.rdrow1(1) := 0.U; vault.io.rdrow1(2) := 0.U;
   }.elsewhen(io.sigQuery){
      vault.io.rdrow1(0) := pcoIndexGen0.io.index; vault.io.rdrow1(1) := pcoIndexGen1.io.index; vault.io.rdrow1(2) := pcoIndexGen2.io.index;
   }.otherwise{
      vault.io.rdrow1(0) := 0.U; vault.io.rdrow1(1) := 0.U; vault.io.rdrow1(2) := 0.U;
   }

   when(io.sigUpdate){
      vault.io.rdcol1 := 0.U
   }.elsewhen(io.sigQuery){
      when(state === s_query_read2)          { vault.io.rdcol1 := 1.U }
      .elsewhen(state === s_query_read4)     { vault.io.rdcol1 := 3.U }
      .elsewhen(state === s_query_read6)     { vault.io.rdcol1 := 5.U }
      .elsewhen(state === s_query_read8)     { vault.io.rdcol1 := 7.U }
      .elsewhen(state === s_query_read10)    { vault.io.rdcol1 := 9.U }
      .elsewhen(state === s_query_read12)    { vault.io.rdcol1 := 11.U }
      .elsewhen(state === s_query_read14)    { vault.io.rdcol1 := 13.U }
      .elsewhen(state === s_query_read16)    { vault.io.rdcol1 := 15.U }
      .otherwise                             { vault.io.rdcol1 := 0.U }
   }.otherwise{
      vault.io.rdcol1 := 0.U
   }

   vault.io.re := false.B

   vault.io.wrrow(0) := RegEnable(pcoIndexGen0.io.index, io.sigUpdate); vault.io.wrrow(1) := RegEnable(pcoIndexGen1.io.index, io.sigUpdate); vault.io.wrrow(2) := RegEnable(pcoIndexGen2.io.index, io.sigUpdate);
   vault.io.wrcol := RegEnable(io.uAction, io.sigUpdate)
   vault.io.wrdata(0) := RegEnable((vault.io.rddata0(0) >> 1) + update_reward, state === s_update_planeR)
   vault.io.wrdata(1) := RegEnable((vault.io.rddata0(1) >> 1) + update_reward, state === s_update_planeR)
   vault.io.wrdata(2) := RegEnable((vault.io.rddata0(2) >> 1) + update_reward, state === s_update_planeR)
   vault.io.we := false.B
   //=====================================================================//

   //===================== Connections for QVCompare Module0 =====================//
   when(io.sigQuery){
      when(state === s_idle)  {qvcompare0.io.qv0_p0 := 0.U}
      .otherwise              {qvcompare0.io.qv0_p0 := vault.io.rddata0(0)}
   }.otherwise{
      qvcompare0.io.qv0_p0 := 0.U
   }
   when(io.sigQuery){
      when(state === s_idle)  {qvcompare0.io.qv0_p1 := 0.U}
      .otherwise              {qvcompare0.io.qv0_p1 := vault.io.rddata0(1)}
   }.otherwise{
      qvcompare0.io.qv0_p1 := 0.U
   }
   when(io.sigQuery){
      when(state === s_idle)  {qvcompare0.io.qv0_p2 := 0.U}
      .otherwise              {qvcompare0.io.qv0_p2 := vault.io.rddata0(2)}
   }.otherwise{
      qvcompare0.io.qv0_p2 := 0.U
   }
   // qvcompare0.io.qv0_p0 := 0.U
   // qvcompare0.io.qv0_p1 := 0.U
   // qvcompare0.io.qv0_p2 := 0.U
   qvcompare0.io.qv1_p0 := 0.U
   qvcompare0.io.qv1_p1 := 0.U
   qvcompare0.io.qv1_p2 := 0.U
   //============================================================================//

   //===================== Connections for QVCompare Module1 =====================//
   when(io.sigQuery){
      when(state === s_idle)  {qvcompare1.io.qv0_p0 := 0.U}
      .otherwise              {qvcompare1.io.qv0_p0 := vault.io.rddata1(0)}
   }.otherwise{
      qvcompare1.io.qv0_p0 := 0.U
   }
   when(io.sigQuery){
      when(state === s_idle)  {qvcompare1.io.qv0_p1 := 0.U}
      .otherwise              {qvcompare1.io.qv0_p1 := vault.io.rddata1(1)}
   }.otherwise{
      qvcompare1.io.qv0_p1 := 0.U
   }
   when(io.sigQuery){
      when(state === s_idle)  {qvcompare1.io.qv0_p2 := 0.U}
      .otherwise              {qvcompare1.io.qv0_p2 := vault.io.rddata1(2)}
   }.otherwise{
      qvcompare1.io.qv0_p2 := 0.U
   }

   // Add delta path signature's QV values here
   qvcompare1.io.qv1_p0 := 0.U
   qvcompare1.io.qv1_p1 := 0.U
   qvcompare1.io.qv1_p2 := 0.U
   //============================================================================//

   //========================= Connections for Max Module =======================//
   when(io.sigQuery){
      when(state === s_idle) {max3.io.nums(0) := 0.U}
      .otherwise {max3.io.nums(0) := RegNext(max3.io.maxNum)}
   }.otherwise{
      max3.io.nums(0) := 0.U
   }
   when(io.sigQuery){
      // max3.io.nums(1) := vault.io.rddata0(0) + vault.io.rddata0(1) + vault.io.rddata0(2) // sum aggregation across Q-values from each plane
      max3.io.nums(1) := qvcompare0.io.qv_out
   }.otherwise{
      max3.io.nums(1) := 0.U
   }
   when(io.sigQuery){
      // max3.io.nums(2) := vault.io.rddata1(0) + vault.io.rddata1(1) + vault.io.rddata1(2) // sum aggregation across Q-values from each plane
      max3.io.nums(2) := qvcompare1.io.qv_out
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
   //=====================================================================//

   // Output Connection
   io.qAction := Mux(io.sigQuery && state === s_query_read16, max3.io.maxId, 0.U)



   // *********************************************************************//
   // *********************** STATE MACHINE DEFINITION ********************//
   // *********************************************************************//
   when(state === s_idle){
      printf("[IDLE] ================ CHILLING HOMES ===============\n")
      vault.io.re := false.B
      vault.io.we := false.B
      when (io.sigUpdate){
         state := s_update_planeR
      }.elsewhen(io.sigQuery){
         state := s_query_read2
      }
   }
   .elsewhen(state === s_update_planeR){
      vault.io.re := true.B // read the old value from plane
      vault.io.we := false.B
      state := s_update_planeW
      printf("[UPDATE-VAULT-R-PLANE-0] row %d col %d value-read %d\n", vault.io.rdrow0(0), vault.io.rdcol0, vault.io.rddata0(0))
      printf("[UPDATE-VAULT-R-PLANE-1] row %d col %d value-read %d\n", vault.io.rdrow0(1), vault.io.rdcol0, vault.io.rddata0(1))
      printf("[UPDATE-VAULT-R-PLANE-2] row %d col %d value-read %d\n", vault.io.rdrow0(2), vault.io.rdcol0, vault.io.rddata0(2))
   }
   .elsewhen(state === s_update_planeW){
      vault.io.re := false.B
      vault.io.we := true.B // write the new value
      state := s_idle
      printf("[UPDATE-VAULT-W-PLANE-0] row %d col %d value-written %d\n", vault.io.wrrow(0), vault.io.wrcol, vault.io.wrdata(0))
      printf("[UPDATE-VAULT-W-PLANE-1] row %d col %d value-written %d\n", vault.io.wrrow(1), vault.io.wrcol, vault.io.wrdata(1))
      printf("[UPDATE-VAULT-W-PLANE-2] row %d col %d value-written %d\n", vault.io.wrrow(2), vault.io.wrcol, vault.io.wrdata(2))
   }
   // query state transitions
   .elsewhen(state === s_query_read2 || state === s_query_read4 || state === s_query_read6 || state === s_query_read8 || state === s_query_read10 || state === s_query_read12 || state === s_query_read14 || state === s_query_read16){
      vault.io.re := true.B
      printf("[QUERY-VALUT-PLANE-0] row0 %d col0 %d val0 %d\n", vault.io.rdrow0(0), vault.io.rdcol0, vault.io.rddata0(0))
      printf("[QUERY-VALUT-PLANE-1] row0 %d col0 %d val0 %d\n", vault.io.rdrow0(1), vault.io.rdcol0, vault.io.rddata0(1))
      printf("[QUERY-VALUT-PLANE-2] row0 %d col0 %d val0 %d\n", vault.io.rdrow0(2), vault.io.rdcol0, vault.io.rddata0(2))
      printf("[QUERY-VALUT-PLANE-0] row1 %d col1 %d val1 %d\n", vault.io.rdrow1(0), vault.io.rdcol1, vault.io.rddata1(0))
      printf("[QUERY-VALUT-PLANE-1] row1 %d col1 %d val1 %d\n", vault.io.rdrow1(1), vault.io.rdcol1, vault.io.rddata1(1))
      printf("[QUERY-VALUT-PLANE-2] row1 %d col1 %d val1 %d\n", vault.io.rdrow1(2), vault.io.rdcol1, vault.io.rddata1(2))
      printf("[QUERY-VAULT] max3 %d maxId %d\n", max3.io.maxNum, max3.io.maxId)

      when(state === s_query_read2)          { state := s_query_read4 }
      .elsewhen(state === s_query_read4)     { state := s_query_read6 }
      .elsewhen(state === s_query_read6)     { state := s_query_read8 }
      .elsewhen(state === s_query_read8)     { state := s_query_read10 }
      .elsewhen(state === s_query_read10)    { state := s_query_read12 }
      .elsewhen(state === s_query_read12)    { state := s_query_read14 }
      .elsewhen(state === s_query_read14)    { state := s_query_read16 }
      .elsewhen(state === s_query_read16)    { state := s_idle }
      .otherwise                             { state := s_idle }
   }
   // *********************************************************************//
}

// object MasterModule extends App {
//    chisel3.Driver.execute(args, () => new MasterModule)
// }
