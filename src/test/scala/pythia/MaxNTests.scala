package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class MaxNTests(c: MaxN) extends PeekPokeTester(c) {
   poke(c.io.nums(0), 143)
   poke(c.io.nums(1), 540)
   poke(c.io.nums(2), 376)
   poke(c.io.ids(0), 4)
   poke(c.io.ids(1), 3)
   poke(c.io.ids(2), 9)
   step(1)
   expect(c.io.maxNum, 540)
   expect(c.io.maxId, 3)
}

class MaxNTester extends ChiselFlatSpec {
  behavior of "MaxN"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new MaxN, backend)((c) => new MaxNTests(c)) should be (true)
    }
  }
}
