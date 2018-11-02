.bss
A:
	.zero 80
i:
	.zero 8
j:
	.zero 8
.text
.globl main
main:
	enter $-112, $0
_4_r10_c7_Assign_assign:
	movq $1, %rax
	movq %rax, i
	movq $2, %rax
	movq %rax, j
	movq j, %rax
	imul j, %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	movq %rax, -16(%rbp)
_15_r13_c5_call_call:
	movq $str_r13_c12, %rdi
	xor %rax, %rax
	call printf
	addq $0, %rsp
_15_r13_c5_call_block:
	movq %rax, -24(%rbp)
	movq -16(%rbp), %rax
	addq j, %rax
	movq %rax, -32(%rbp)
	movq -32(%rbp), %r10
	movq A(, %r10, 8), %rax
	addq $1, %rax
	movq %rax, -40(%rbp)
	movq -40(%rbp), %r10
	movq j, %rax
	movq %rax, A(, %r10, 8)
	movq $0, %r10
	movq $1, %rax
	movq %rax, -48(%rbp, %r10, 8)
_30_r16_c5_call_call:
	movq $str_r16_c12, %rdi
	xor %rax, %rax
	call printf
	addq $0, %rsp
_30_r16_c5_call_block:
	movq %rax, -88(%rbp)
	movq $1, %r11
	movq A(, %r11, 8), %rax
	movq %rax, i
_36_r18_c5_call_call:
	movq $str_r18_c12, %rdi
	xor %rax, %rax
	call printf
	addq $0, %rsp
_36_r18_c5_call_block:
	movq %rax, -96(%rbp)
_38_r19_c5_call_call:
	movq $str_r19_c12, %rdi
	movq $0, %r10
	movq -48(%rbp, %r10, 8), %rsi
	xor %rax, %rax
	call printf
	addq $0, %rsp
_38_r19_c5_call_block:
	movq %rax, -104(%rbp)
_42_r20_c5_call_call:
	movq $str_r20_c12, %rdi
	movq i, %rsi
	xor %rax, %rax
	call printf
	addq $0, %rsp
_42_r20_c5_call_block:
	movq %rax, -112(%rbp)
_3_r0_c0_Block_ed:
	leave
	ret
noReturn:
	movq $-2, %rdi
	call exit
outOfBound:
	movq $-1, %rdi
	call exit
.section .rodata
	str_r13_c12:
		.string "CP2\n"
	str_r16_c12:
		.string "CP3\n"
	str_r18_c12:
		.string "CP4\n"
	str_r19_c12:
		.string "B[0] is %d\n"
	str_r20_c12:
		.string "A[i + j] is %d\n"
