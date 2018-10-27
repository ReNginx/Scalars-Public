.bss
.text
.globl l1call6_start
l1call6_start:
enter $0, $0
l0call0_start:
.noReturn
movq $-2, %rdi
call exit
.outOfBound
movq $-1, %rdi
call exit
.section .rodata
