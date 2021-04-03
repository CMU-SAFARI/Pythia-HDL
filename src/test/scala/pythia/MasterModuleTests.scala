package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class MasterModuleTests(c: MasterModule) extends PeekPokeTester(c) {

   def updateQVStore(pc:Int, offset:Int, action:Int, reward:Int) = {
      poke(c.io.uPC, pc)
      poke(c.io.uOffset, offset)
      poke(c.io.uAction, action)
      poke(c.io.uReward, reward)
      poke(c.io.sigUpdate, 1)
      step(3)
      poke(c.io.sigUpdate, 0)
      step(1)
   }

   def queryQVStore(pc:Int, offset:Int, action:Int) = {
      poke(c.io.qPC, pc)
      poke(c.io.qOffset, offset)
      poke(c.io.sigQuery, 1)
      step(8)
      expect(c.io.qAction, action)
      poke(c.io.sigQuery, 0)
      step(1)
   }

   val pc = 0x7fff3028
   updateQVStore(pc, 5, 2, 6)
   queryQVStore(pc, 5, 2)
   // updateQVStore(pc, 5, 2, 10)
   // queryQVStore(pc, 5, 2)
}

class MasterModuleTester extends ChiselFlatSpec {
  behavior of "MasterModule"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new MasterModule, backend)((c) => new MasterModuleTests(c)) should be (true)
    }
  }
}
