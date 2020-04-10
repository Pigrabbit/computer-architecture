package dinocpu
import chisel3._
import chisel3.util._

class Mux2 extends Module {
  val io = IO (new Bundle {
    val sel = Input(UInt(1.W))
    val in0 = Input(UInt(1.W))
    val in1 = Input(UInt(1.W))
    val out = Output(UInt(1.W))
  })
  io.out := (io.sel & io.in1) | (~io.sel & io.in0)
}


class SimpleAdder extends Module {
  val io = IO (new Bundle {
    val inputX = Input(UInt(32.W))
    val inputY = Input(UInt(32.W))
    val result = Output(UInt(32.W))
  })
  io.result := io.inputX + io.inputY 
}

class SimpleSystem extends Module {
  val io = IO (new Bundle {
    val success = Output(Bool())
  })

  val adder1 = Module(new SimpleAdder)
  val adder2 = Module(new SimpleAdder)
//  val reg1 = Reg(UInt(32.W))
//  val reg2 = Reg(UInt(32.W))
  val reg1 = RegInit(1.U(32.W))
  val reg2 = RegInit(0.U(32.W))
  val m0 = Module(new Mux2)

  reg1 := adder1.io.result
  adder1.io.inputX := reg1

  reg2 := adder2.io.result
  adder1.io.inputY := reg2

  adder2.io.inputX := adder1.io.result
  adder2.io.inputY := 3.U(32.W)

  m0.io.in0 := 0.U
  m0.io.in1 := 1.U
  m0.io.sel := (adder2.io.result === 128.U(32.W)) 

  io.success := m0.io.sel

  printf(p"reg1: $reg1, reg2: $reg2, success: ${io.success}\n")
}

