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
  // io := DontCare

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
  // immGen.io     := DontCare
  // branchCtrl.io := DontCare
  // branchAdd.io  := DontCare

  io.imem.address := pc

  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  val instruction = io.imem.instruction
  immGen.io.instruction := instruction
  control.io.opcode := instruction(6,0)


  registers.io.readreg1 := instruction(19,15)
  registers.io.readreg2 := instruction(24,20)
  registers.io.writereg := instruction(11,7)
  registers.io.writedata := MuxCase(0.U(32.W),
    Array(
      (control.io.toreg === 0.U) -> alu.io.result,
      (control.io.toreg === 1.U) -> io.dmem.readdata,
      (control.io.toreg === 2.U) -> pcPlusFour.io.result,
    )
  )

  val writereg = instruction(11,7)
  registers.io.writereg := writereg
  registers.io.wen := MuxCase(control.io.regwrite,
    Array(
      (instruction(11,7) === "b00000".U) -> false.B
    )
  )

  aluControl.io.add       := control.io.add
  aluControl.io.immediate := control.io.immediate
  aluControl.io.funct7    := instruction(31,25)
  aluControl.io.funct3    := instruction(14,12)

  alu.io.operation := aluControl.io.operation
  alu.io.inputx := MuxCase(0.U(32.W),
    Array(
      (control.io.alusrc1 === 0.U) -> registers.io.readdata1,
      (control.io.alusrc1 === 1.U) -> 0.U(32.W),
      (control.io.alusrc1 === 2.U) -> pc,
    )
  )
  alu.io.inputy := Mux(control.io.immediate, immGen.io.sextImm, registers.io.readdata2)

  io.dmem.address := alu.io.result
  io.dmem.writedata := registers.io.readdata2
  io.dmem.memread := control.io.memread
  io.dmem.memwrite := control.io.memwrite
  io.dmem.maskmode := instruction(13, 12)
  io.dmem.sext := ~instruction(14)

  branchCtrl.io.branch := control.io.branch
  branchCtrl.io.funct3 := instruction(14,12)
  branchCtrl.io.inputx := registers.io.readdata1
  branchCtrl.io.inputy := registers.io.readdata2

  branchAdd.io.inputx := pc
  branchAdd.io.inputy := immGen.io.sextImm

  when (branchCtrl.io.taken || control.io.jump === 2.U) {
    pc := branchAdd.io.result
  } .elsewhen (control.io.jump === 3.U) {
    pc := alu.io.result & Cat(Fill(32, 1.U), 0.U)
  } .otherwise {
    pc := pcPlusFour.io.result
  }

  // Debug / pipeline viewer
  val structures = List(
    (control, "control"),
    (registers, "registers"),
    (aluControl, "aluControl"),
    (alu, "alu"),
    (immGen, "immGen"),
    (branchCtrl, "branchCtrl"),
    (pcPlusFour, "pcPlusFour"),
    (branchAdd, "branchAdd"),
  )

  printf("DASM(%x)\n", instruction)
  printf(p"CYCLE=$cycleCount\n")
  printf(p"pc: $pc\n")
  for (structure <- structures) {
    printf(p"${structure._2}: ${structure._1.io}\n")
  }
  printf(p"instruction memory: ${io.imem}\n")
  printf(p"data memory: ${io.dmem}")
  printf("\n")

}
