// See LICENSE.txt for license details.
package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}


class PlaneTests(c: Plane) extends PeekPokeTester(c) {
  def PlaneRead(row:Int, col:Int, data:Int){
    poke(c.io.rdrow, row)
    poke(c.io.rdcol, col)
    poke(c.io.re, 1)
    step(1)
    expect(c.io.rddata, data)
  }
  def PlaneWrite(row:Int, col:Int, data:Int){
    poke(c.io.wrrow, row)
    poke(c.io.wrcol, col)
    poke(c.io.wrdata, data)
    poke(c.io.we, 1)
    step(1)
  }
  /*
  def PlaneRead(addr:Int, data:Int) = {
    poke(c.io.re, 1)
    poke(c.io.rdaddr, addr)
    step(1)
    expect(c.io.rddata, data)
  }
  def PlaneWrite(addr:Int, data:Int) = {
    poke(c.io.we, 1)
    poke(c.io.wraddr, addr)
    poke(c.io.wrdata, data)
    step(1)
  }
  */
  for(t <- 0 until 9){
    val row = rnd.nextInt(128)
    val col = rnd.nextInt(16)
    val data = rnd.nextInt(65536)
    PlaneWrite(row, col, data)
    PlaneRead(row, col, data)
    //val addr = rnd.nextInt(2048)
    //val data = rnd.nextInt(65536)
    //PlaneWrite(addr, data)
    //PlaneRead(addr, data)
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
