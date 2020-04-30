// This file contains ALU control logic.

package dinocpu

import chisel3._
import chisel3.util._

/**
 * The ALU control unit
 *
 * Input:  add, if true, add no matter what the other bits are
 * Input:  immediate, if true, ignore funct7 when computing the operation
 * Input:  funct7, the most significant bits of the instruction
 * Input:  funct3, the middle three bits of the instruction (12-14)
 * Output: operation, What we want the ALU to do.
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy.
 * This is loosely based on figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val add       = Input(Bool())
    val immediate = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))

    val operation = Output(UInt(4.W))
  })

  // Do not modify 
  io.operation := 15.U // invalid operation

  // Your code goes here

  //  funct7[31:25]   funct3[14:12]   ALU control (output)
  //  0000000         111             0000 and
  //  0000000         110             0001 or
  //  0000000         000             0010 add
  //  0100000         000             0011 sub
  //  0000000         010             0100 slt
  //  0000000         011             0101 sltu
  //  0000000         001             0110 sll
  //  0000000         101             0111 srl
  //  0100000         101             1000 sra
  //  0000000         100             1001 xor


  switch(io.funct7) {
    is ("b0000000".U) {
      // sub, sra
      switch(io.funct3) {
        is ("b111".U) { io.operation := "b0000".U } // and
        is ("b110".U) { io.operation := "b0001".U } // or
        is ("b000".U) { io.operation := "b0010".U } // add
        is ("b010".U) { io.operation := "b0100".U } // slt
        is ("b011".U) { io.operation := "b0101".U } // sltu
        is ("b001".U) { io.operation := "b0110".U } // sll
        is ("b101".U) { io.operation := "b0111".U } // srl
        is ("b100".U) { io.operation := "b1001".U } // xor
      }
    }
    is ("b0100000".U) {
      // others
      switch(io.funct3) {
        is ("b000".U) { io.operation := "b0011".U } // sub
        is ("b101".U) { io.operation := "b1000".U } // sra
      }
    }
  }

}

