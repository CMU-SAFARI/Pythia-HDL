package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class QVCompareTests(c: QVCompare) extends PeekPokeTester(c) {
   def test(qv0_p0:Int, qv0_p1:Int, qv0_p2:Int, qv1_p0:Int, qv1_p1:Int, qv1_p2:Int) = {
      poke(c.io.qv0_p0, qv0_p0)
      poke(c.io.qv0_p1, qv0_p1)
      poke(c.io.qv0_p2, qv0_p2)
      poke(c.io.qv1_p0, qv1_p0)
      poke(c.io.qv1_p1, qv1_p1)
      poke(c.io.qv1_p2, qv1_p2)
      step(1)
      val qv0 = qv0_p0 + qv0_p1 + qv0_p2
      val qv1 = qv1_p0 + qv1_p1 + qv1_p2
      val max = if (qv0 >= qv1) {qv0} else {qv1}
      expect(c.io.qv_out, max)
   }

   test(1,2,3, 1,2,9)
   test(1,2,5, 1,2,9)
   test(1,2,10, 1,2,9)
}

class QVCompareTester extends ChiselFlatSpec {
  behavior of "QVCompare"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new QVCompare, backend)((c) => new QVCompareTests(c)) should be (true)
    }
  }
}
