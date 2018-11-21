.bss
image:
	.zero 1040000
cols:
	.zero 8
rows:
	.zero 8
size:
	.zero 8
.text
.globl read
read:
	enter $-96, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
	movq $0, -32(%rbp)
	movq $0, -40(%rbp)
	movq $0, -48(%rbp)
	movq $0, -56(%rbp)
	movq $0, -64(%rbp)
	movq $0, -72(%rbp)
	movq $0, -80(%rbp)
	movq $0, -88(%rbp)
	movq $0, -96(%rbp)
_4_r16_c3_call_call:
	movq $str_r16_c21, %rdi
	xor %rax, %rax
	call pgm_open_for_read
	addq $0, %rsp
_4_r16_c3_call_block:
	movq %rax, -8(%rbp)
_8_r17_c10_call_call:
	xor %rax, %rax
	call pgm_get_cols
	addq $0, %rsp
_8_r17_c10_call_block:
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq %rax, cols
_11_r18_c10_call_call:
	xor %rax, %rax
	call pgm_get_rows
	addq $0, %rsp
_11_r18_c10_call_block:
	movq %rax, -24(%rbp)
	movq -24(%rbp), %rax
	movq %rax, rows
	movq cols, %rax
	imul rows, %rax
	movq %rax, size
	movq size, %rax
	movq %rax, -32(%rbp)
	movq $0, %rax
	movq %rax, -40(%rbp)
_22_r20_c17_Logical_body:
	movq -40(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -48(%rbp)
_18_r20_c3_For_cond:
	movq -48(%rbp), %rax
	test %rax, %rax
	je _52_r25_c3_call_call
_31_r21_c12_Assign_assign:
	movq $0, %rax
	movq %rax, -56(%rbp)
_34_r21_c19_Logical_body:
	movq -56(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -64(%rbp)
_30_r21_c5_For_cond:
	movq -64(%rbp), %rax
	test %rax, %rax
	je _26_r20_c27_Assign_assign
_45_r22_c14_Oper_expr:
	movq -40(%rbp), %rax
	imul $303, %rax
	movq %rax, -72(%rbp)
	movq -72(%rbp), %rax
	addq -56(%rbp), %rax
	movq %rax, -80(%rbp)
_51_r22_c24_call_call:
	xor %rax, %rax
	call pgm_get_next_pixel
	addq $0, %rsp
_51_r22_c24_call_block:
	movq %rax, -88(%rbp)
	movq -80(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -80(%rbp), %r10
	movq -88(%rbp), %rax
	movq %rax, image(, %r10, 8)
	movq -56(%rbp), %rax
	addq $1, %rax
	movq %rax, -56(%rbp)
	jmp _34_r21_c19_Logical_body
	jmp _26_r20_c27_Assign_assign
_26_r20_c27_Assign_assign:
	movq -40(%rbp), %rax
	addq $1, %rax
	movq %rax, -40(%rbp)
	jmp _22_r20_c17_Logical_body
	jmp _52_r25_c3_call_call
_52_r25_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_52_r25_c3_call_block:
	movq %rax, -96(%rbp)
_2_r14_c6_Method_ed:
	leave
	ret
.globl write
write:
	enter $-80, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
	movq $0, -32(%rbp)
	movq $0, -40(%rbp)
	movq $0, -48(%rbp)
	movq $0, -56(%rbp)
	movq $0, -64(%rbp)
	movq $0, -72(%rbp)
	movq $0, -80(%rbp)
_55_r29_c3_call_call:
	movq $str_r29_c22, %rdi
	movq cols, %rsi
	movq rows, %rdx
	xor %rax, %rax
	call pgm_open_for_write
	addq $0, %rsp
_55_r29_c3_call_block:
	movq %rax, -8(%rbp)
	movq cols, %rax
	imul rows, %rax
	movq %rax, size
	movq size, %rax
	movq %rax, -16(%rbp)
	movq $0, %rax
	movq %rax, -24(%rbp)
_69_r31_c17_Logical_body:
	movq -24(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -32(%rbp)
_65_r31_c3_For_cond:
	movq -32(%rbp), %rax
	test %rax, %rax
	je _98_r36_c3_call_call
_78_r32_c12_Assign_assign:
	movq $0, %rax
	movq %rax, -40(%rbp)
_81_r32_c19_Logical_body:
	movq -40(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -48(%rbp)
_77_r32_c5_For_cond:
	movq -48(%rbp), %rax
	test %rax, %rax
	je _73_r31_c27_Assign_assign
_92_r33_c35_Oper_expr:
	movq -24(%rbp), %rax
	imul $303, %rax
	movq %rax, -56(%rbp)
	movq -56(%rbp), %rax
	addq -40(%rbp), %rax
	movq %rax, -64(%rbp)
_89_r33_c7_call_call:
	movq -64(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -64(%rbp), %r10
	movq image(, %r10, 8), %rdi
	xor %rax, %rax
	call pgm_write_next_pixel
	addq $0, %rsp
_89_r33_c7_call_block:
	movq %rax, -72(%rbp)
	movq -40(%rbp), %rax
	addq $1, %rax
	movq %rax, -40(%rbp)
	jmp _81_r32_c19_Logical_body
	jmp _73_r31_c27_Assign_assign
_73_r31_c27_Assign_assign:
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _69_r31_c17_Logical_body
	jmp _98_r36_c3_call_call
_98_r36_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_98_r36_c3_call_block:
	movq %rax, -80(%rbp)
_53_r27_c6_Method_ed:
	leave
	ret
.globl shift_left
shift_left:
	enter $-120, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
	movq $0, -32(%rbp)
	movq $0, -40(%rbp)
	movq $0, -48(%rbp)
	movq $0, -56(%rbp)
	movq $0, -64(%rbp)
	movq $0, -72(%rbp)
	movq $0, -80(%rbp)
	movq $0, -88(%rbp)
	movq $0, -96(%rbp)
	movq $0, -104(%rbp)
	movq $0, -112(%rbp)
	movq $0, -120(%rbp)
_102_r40_c10_Assign_assign:
	movq $0, %rax
	movq %rax, -8(%rbp)
_105_r40_c17_Logical_body:
	movq -8(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -16(%rbp)
_101_r40_c3_For_cond:
	movq -16(%rbp), %rax
	test %rax, %rax
	je _149_r45_c10_Assign_assign
_114_r41_c12_Assign_assign:
	movq $0, %rax
	movq %rax, -24(%rbp)
_119_r41_c26_Oper_expr:
	movq cols, %rax
	subq $30, %rax
	movq %rax, -32(%rbp)
	movq -24(%rbp), %rdx
	movq -32(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -40(%rbp)
_113_r41_c5_For_cond:
	movq -40(%rbp), %rax
	test %rax, %rax
	je _109_r40_c27_Assign_assign
_131_r42_c14_Oper_expr:
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -48(%rbp)
	movq -48(%rbp), %rax
	addq -24(%rbp), %rax
	movq %rax, -56(%rbp)
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -64(%rbp)
	movq -64(%rbp), %rax
	addq -24(%rbp), %rax
	movq %rax, -72(%rbp)
	movq -72(%rbp), %rax
	addq $30, %rax
	movq %rax, -80(%rbp)
	movq -56(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -80(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -56(%rbp), %r10
	movq -80(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, image(, %r10, 8)
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _119_r41_c26_Oper_expr
	jmp _109_r40_c27_Assign_assign
_109_r40_c27_Assign_assign:
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -8(%rbp)
	jmp _105_r40_c17_Logical_body
	jmp _149_r45_c10_Assign_assign
_149_r45_c10_Assign_assign:
	movq $0, %rax
	movq %rax, -8(%rbp)
_152_r45_c17_Logical_body:
	movq -8(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -88(%rbp)
_148_r45_c3_For_cond:
	movq -88(%rbp), %rax
	test %rax, %rax
	je _99_r38_c6_Method_ed
_163_r46_c19_Oper_expr:
	movq cols, %rax
	subq $30, %rax
	movq %rax, -24(%rbp)
	movq -24(%rbp), %rax
	movq %rax, -96(%rbp)
_167_r46_c27_Logical_body:
	movq -24(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -104(%rbp)
_160_r46_c5_For_cond:
	movq -104(%rbp), %rax
	test %rax, %rax
	je _156_r45_c27_Assign_assign
_178_r47_c14_Oper_expr:
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -112(%rbp)
	movq -112(%rbp), %rax
	addq -24(%rbp), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -120(%rbp), %r10
	movq $0, %rax
	movq %rax, image(, %r10, 8)
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _167_r46_c27_Logical_body
	jmp _156_r45_c27_Assign_assign
_156_r45_c27_Assign_assign:
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -8(%rbp)
	jmp _152_r45_c17_Logical_body
	jmp _99_r38_c6_Method_ed
_99_r38_c6_Method_ed:
	leave
	ret
.globl main
main:
	enter $-16, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
_187_r52_c3_call_call:
	xor %rax, %rax
	call read
	addq $0, %rsp
_188_r53_c3_call_call:
	xor %rax, %rax
	call start_caliper
	addq $0, %rsp
_188_r53_c3_call_block:
	movq %rax, -8(%rbp)
_189_r54_c3_call_call:
	xor %rax, %rax
	call shift_left
	addq $0, %rsp
_190_r55_c3_call_call:
	xor %rax, %rax
	call end_caliper
	addq $0, %rsp
_190_r55_c3_call_block:
	movq %rax, -16(%rbp)
_191_r56_c3_call_call:
	xor %rax, %rax
	call write
	addq $0, %rsp
_185_r51_c6_Method_ed:
	movq $0, %rax
	leave
	ret
.globl noReturn
noReturn:
	movq $-2, %rdi
	call exit
.globl outOfBound
outOfBound:
	movq $-1, %rdi
	call exit
.section .rodata
	str_r16_c21:
		.string "saman.pgm"
	str_r29_c22:
		.string "saman_sl.pgm"
