# Assignment 1: Playing with RISC-C Complier and Emulator

Table of Content

- [Task1](#implement-iterative-bubble)
- [Task2](#implement-recursive-bubble)
- [Task3](#a-static-look-with-disassembly)
- [Task4](#a-dynamic-look-with-emulator-statistics)

## Implement Itreative Bubble

``` c
void swap(int *x, int *y) {
  int tmp = *x;
  *x = *y;
  *y = tmp;
}

void bubble_sort_iter(int arr[], int n)
{
  int hasSwapped = 0;

  for (int i = 0; i < n; i++) {
    for (int j = n-1; j > i; j--) {
      if (arr[j] < arr[j-1]) {
        swap(&arr[j], &arr[j-1]);
        hasSwapped = 1;
      }
    }

    if (hasSwapped == 0) break;
  }
}
```


## Implement Recursive Bubble

``` c
void bubble_sort_recur(int arr[], int n)
{
  if (n == 1) return;

  int hasSwapped = 0;
  for (int i = 0; i < n; i++) {
    if (arr[i] < arr[i-1]) {
      swap(&arr[i], &arr[i-1]);
      hasSwapped = 1;
    }
  }

  bubble_sort_recur(arr, n-1);
}
```


## A static Look with Disassembly

Q1. How are arguments of bubble_sort_iter() and bubble_sort_recur() maintained in the stack?

Both of them make room on stack for 12 registers 
```
addi  sp, sp, -48
```

Q2. Does `bubble_sort_iter()` and `bubble_sort_recur()` use JAL, JALR, or both?

Both of them use `JAL` only.

Q3. How does bubble_sort_iter() and bubble_sort_recur() restore the stack before returning to a caller function?

At first, they store parameters as 
```
sw	ra,44(sp) 
sw	s0,40(sp)
```

At last, before return they restore the parameters like
```
lw ra, 44(sp)
sw s0, 40(sp)
```

Q4. What is ret instruction shown in objdump ? (RISC-V ISA does not have ret instruction

It opreates as `jalr` in RISC-V ISA, which is a instruction return to caller.

## A Dynamic Look with Emulator Statistics

Q5. Shortly explain how emu-r32i.c implements the RISC-V CPU emulation (i.e., how it emulates many different RISC-V instructions without running on a real RISC-V CPU). Your answer should be less than 40 words.

The RISC-V machine is simply implemented in `emu-r23i.c`. There are PC, RAM and clock you name it.
It interprets code in `riscv_cpu_interp_x32()` and run execution with `execute_instruction()`.

Q6. Compare the instructions counts between bubble_iter and bubble_recur . What's the notable differences between these two?

The number of `jal` instructions used are different between two.

Q7. Why do you think the differences in Q6 is observed? Relate your answer with the differences between iteration and resursion

Basically, Recursion is called more frequently by caller than Iteration does. (Actually, iteration fuction is not called by caller)

That is why `bubble_sort_recur()` has used more instruction of `jal`, `lw` and `sw` as well. (Since it needs to restore parameters.)
