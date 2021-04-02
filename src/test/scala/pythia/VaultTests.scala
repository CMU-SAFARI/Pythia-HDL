package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class ValutTests(c: Valut) extends PeekPokeTester(c) {
   def ValutRead(row0_p0:Int, row0_p1:Int, row0_p2:Int, col0:Int, data0:Int,
                  row1_p0:Int, row1_p1:Int, row1_p2:Int, col1:Int, data1:Int) = {
     poke(c.io.rdrow0(0), row0_p0)
     poke(c.io.rdrow0(1), row0_p1)
     poke(c.io.rdrow0(2), row0_p2)
     poke(c.io.rdcol0, col0)
     poke(c.io.rdrow1(0), row1_p0)
     poke(c.io.rdrow1(1), row1_p1)
     poke(c.io.rdrow1(2), row1_p2)
     poke(c.io.rdcol1, col1)
     poke(c.io.re, 1)
     step(1)
     expect(c.io.rddata0(0), data0)
     expect(c.io.rddata0(1), data0)
     expect(c.io.rddata0(2), data0)
     expect(c.io.rddata1(0), data1)
     expect(c.io.rddata1(1), data1)
     expect(c.io.rddata1(2), data1)
   }
   def ValutWrite(row_p0:Int, row_p1:Int, row_p2:Int, col:Int, data:Int) = {
     poke(c.io.wrrow(0), row_p0)
     poke(c.io.wrrow(1), row_p1)
     poke(c.io.wrrow(2), row_p2)
     poke(c.io.wrcol, col)
     poke(c.io.wrdata(0), data)
     poke(c.io.wrdata(1), data)
     poke(c.io.wrdata(2), data)
     poke(c.io.we, 1)
     step(1)
   }
   def test(row0_p0:Int, row0_p1:Int, row0_p2:Int, col0:Int, data0:Int,
                  row1_p0:Int, row1_p1:Int, row1_p2:Int, col1:Int, data1:Int) = {
      ValutWrite(row0_p0, row0_p1, row0_p2, col0, data0)
      ValutWrite(row1_p0, row1_p1, row1_p2, col1, data1)
      ValutRead(row0_p0, row0_p1, row0_p2, col0, data0, row1_p0, row1_p1, row1_p2, col1, data1)
   }

   test(2,3,9,5,10, 4,6,7,1,18)
   test(3,4,12,5,7891, 4,9,78,32,2390)
   test(96,48,67,42,9056, 28,90,69,25,4863)
   test(40,83,23,12,7813, 104,39,85,7,12986)
   test(1,100,69,27,785, 5,111,51,19,2308)
}

class ValutTester extends ChiselFlatSpec {
  behavior of "Valut"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new Valut, backend)((c) => new ValutTests(c)) should be (true)
    }
  }
}
