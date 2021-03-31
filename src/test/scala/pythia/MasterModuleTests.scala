package pythia

import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class MasterModuleTests(c: MasterModule) extends PeekPokeTester(c) {

   def updateQVStore(pc:Int, offset:Int, action:Int, reward:Int) = {
      poke(c.io.uPC, pc)
      poke(c.io.uOffset, offset)
      poke(c.io.uAction, action)
      poke(c.io.uReward, reward)
      poke(c.io.sigUpdate, 1)
      step(1)
   }

   // def queryQVStore(pc:Int, offset:Int, action:Int) = {
   //    poke(c.io.qPC, pc)
   //    poke(c.io.qOffset, offset)
   //    poke(c.io.sigQuery, 1)
   //    step(1)
   //    // val act = peek(c.io.qAction).toInt
   //    // printf("recommended action %d, expected %d", act, action)
   //    expect(c.io.qAction, action)
   // }

   val pc = 0x7fff3028
   val offset = 5
   val action = 2
   val reward = 12

   // update QVStore first
   updateQVStore(pc, offset, action, 12)
   updateQVStore(pc, offset, action, 10)

   // get action recommedation from QVStore
   // queryQVStore(pc, offset, action)
}

class MasterModuleTester extends ChiselFlatSpec {
  behavior of "MasterModule"
  backends foreach {backend =>
    it should s"correctly add randomly generated numbers and show carry in $backend" in {
      Driver(() => new MasterModule, backend)((c) => new MasterModuleTests(c)) should be (true)
    }
  }
}
