/*******************************************************************
 * Author: Rahul Bera
 * Description: This is purely combinatorial circuit
 * Given two sets of partial Q-values read from each vault,
 * this circuit sums up all partial Q-values for a given feature
 * and compares a max across Q-values of all features
 * *****************************************************************/
package pythia

import chisel3._
import chisel3.util._

class QVCompare extends Module{
   val io = IO(new Bundle{
      val qv0_p0 = Input(UInt(16.W))
      val qv0_p1 = Input(UInt(16.W))
      val qv0_p2 = Input(UInt(16.W))
      val qv1_p0 = Input(UInt(16.W))
      val qv1_p1 = Input(UInt(16.W))
      val qv1_p2 = Input(UInt(16.W))
      val qv_out = Output(UInt(16.W))
   })

   val qv0 = io.qv0_p0 + io.qv0_p1 + io.qv0_p2
   val qv1 = io.qv1_p0 + io.qv1_p1 + io.qv1_p2

   io.qv_out := Mux(qv0 > qv1, qv0, qv1)
}
