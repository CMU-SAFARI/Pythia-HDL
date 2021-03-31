/* Author: computes Max of N numbers */
package pythia

import chisel3._

class MaxN extends Module
{
   // private def Max2(x:UInt, y:UInt) = Mux(x>=y, x, y)

   val io = IO(new Bundle{
      val nums = Input(Vec(3, UInt(16.W)))
      val ids = Input(Vec(3, UInt(4.W)))
      val maxNum = Output(UInt(16.W))
      val maxId = Output(UInt(4.W))
   })

   io.maxNum := 0.U
   io.maxId := 0.U

   // TODO: make the ugly definition more generic
   when(io.nums(0) >= io.nums(1)){
      when(io.nums(0) >= io.nums(2)){
         io.maxId := io.ids(0)
         io.maxNum := io.nums(0)
      }.otherwise{
         io.maxId := io.ids(2)
         io.maxNum := io.nums(2)
      }
   }.otherwise{
      when(io.nums(1) >= io.nums(2)){
         io.maxId := io.ids(1)
         io.maxNum := io.nums(1)
      }.otherwise{
         io.maxId := io.ids(2)
         io.maxNum := io.nums(2)
      }
   }
}
