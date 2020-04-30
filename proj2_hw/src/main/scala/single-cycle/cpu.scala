// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class SingleCycleCPU(implicit val conf: CPUConfig) extends Module {
  val io = IO(new CoreIO())
  io := DontCare

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val branchCtrl = Module(new BranchControl())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  // To make the FIRRTL compiler happy. Remove this as you connect up the I/O's
  // control.io    := DontCare
  // registers.io  := DontCare
  // aluControl.io := DontCare
  // alu.io        := DontCare
  immGen.io     := DontCare
  branchCtrl.io := DontCare
  // pcPlusFour.io := DontCare
  branchAdd.io  := DontCare

  io.imem.address := pc
  val instruction = io.imem.instruction

  
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  pc := pcPlusFour.io.result

  // immGen.io.instruction := instruction

  control.io.opcode := instruction(6, 0)
  
  aluControl.io.funct7 := instruction(31, 25)
  aluControl.io.funct3 := instruction(14, 12)
  aluControl.io.add := DontCare
  aluControl.io.immediate := DontCare
  
  registers.io.readreg1 := instruction(19, 15)
  registers.io.readreg2 := instruction(24, 20)
  registers.io.writereg := instruction(11, 7)
  registers.io.writedata := alu.io.result
  // add 0 이면 wen을 false로
  // 나머지 경우는 wen을 true로 넣어준다
  when(instruction(11, 7) === "b00000".U) {
    registers.io.wen := false.B
  } 
  .otherwise {
    registers.io.wen := true.B
  }
  
  alu.io.operation := aluControl.io.operation
  alu.io.inputx := registers.io.readdata1
  alu.io.inputy := registers.io.readdata2

  // Do not modify
  // Debug / pipeline viewer
  val structures = List(
    (control, "control"),
    (registers, "registers"),
    (aluControl, "aluControl"),
    (alu, "alu"),
    (immGen, "immGen"),
    (branchCtrl, "branchCtrl"),
    (pcPlusFour, "pcPlusFour"),
    (branchAdd, "branchAdd")
  )

  printf("DASM(%x)\n", instruction)
  printf(p"CYCLE=$cycleCount\n")
  printf(p"pc: $pc\n")
  for (structure <- structures) {
    printf(p"${structure._2}: ${structure._1.io}\n")
  }
  printf("\n")

}
