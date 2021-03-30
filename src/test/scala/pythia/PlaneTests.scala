package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class PlaneTests(c: Plane) extends PeekPokeTester(c) {
  def PlaneRead(row0:Int, col0:Int, data0:Int, row1:Int, col1:Int, data1:Int){
    poke(c.io.rdrow0, row0)
    poke(c.io.rdcol0, col0)
    poke(c.io.rdrow1, row1)
    poke(c.io.rdcol1, col1)
    poke(c.io.re, 1)
    step(1)
    expect(c.io.rddata0, data0)
    expect(c.io.rddata1, data1)
  }
  def PlaneWrite(row:Int, col:Int, data:Int){
    poke(c.io.wrrow, row)
    poke(c.io.wrcol, col)
    poke(c.io.wrdata, data)
    poke(c.io.we, 1)
    step(1)
  }
  for(t <- 0 until 9){
    val row0 = rnd.nextInt(128)
    val col0 = rnd.nextInt(16)
    val data0 = rnd.nextInt(65536)
    val row1 = rnd.nextInt(128)
    val col1 = rnd.nextInt(16)
    val data1 = rnd.nextInt(65536)
    PlaneWrite(row0, col0, data0)
    PlaneWrite(row1, col1, data1)
    PlaneRead(row0, col0, data0, row1, col1, data1)
  }
}

class PlaneTester extends ChiselFlatSpec {
  behavior of "Plane"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new Plane, backend)((c) => new PlaneTests(c)) should be (true)
    }
  }
}
