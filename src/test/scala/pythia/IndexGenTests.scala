package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class IndexGenTests(c: IndexGen) extends PeekPokeTester(c) {
  for(t <- 0 until 4) {
    val pc = rnd.nextInt(65536)
    val offset = rnd.nextInt(64)
    val result = ((pc << 6) + offset) % 128
    poke(c.io.pc, pc)
    poke(c.io.offset, offset)
    step(1)
    expect(c.io.index, result)
  }
}

class IndexGenTester extends ChiselFlatSpec {
  behavior of "IndexGen"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new IndexGen, backend)((c) => new IndexGenTests(c)) should be (true)
    }
  }
}
