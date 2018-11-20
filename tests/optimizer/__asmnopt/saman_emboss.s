.bss
imageIn:
	.zero 1040000
imageOut:
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
_4_r17_c3_call_call:
	movq $str_r17_c22, %rdi
	xor %rax, %rax
	call pgm_open_for_read
	addq $0, %rsp
_4_r17_c3_call_block:
	movq %rax, -8(%rbp)
_8_r18_c10_call_call:
	xor %rax, %rax
	call pgm_get_cols
	addq $0, %rsp
_8_r18_c10_call_block:
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq %rax, cols
_11_r19_c10_call_call:
	xor %rax, %rax
	call pgm_get_rows
	addq $0, %rsp
_11_r19_c10_call_block:
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
_22_r21_c17_Logical_body:
	movq -40(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -48(%rbp)
_18_r21_c3_For_cond:
	movq -48(%rbp), %rax
	test %rax, %rax
	je _52_r26_c3_call_call
_31_r22_c12_Assign_assign:
	movq $0, %rax
	movq %rax, -56(%rbp)
_34_r22_c19_Logical_body:
	movq -56(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -64(%rbp)
_30_r22_c5_For_cond:
	movq -64(%rbp), %rax
	test %rax, %rax
	je _26_r21_c27_Assign_assign
_45_r23_c16_Oper_expr:
	movq -40(%rbp), %rax
	imul $303, %rax
	movq %rax, -72(%rbp)
	movq -72(%rbp), %rax
	addq -56(%rbp), %rax
	movq %rax, -80(%rbp)
_51_r23_c26_call_call:
	xor %rax, %rax
	call pgm_get_next_pixel
	addq $0, %rsp
_51_r23_c26_call_block:
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
	movq %rax, imageIn(, %r10, 8)
	movq -56(%rbp), %rax
	addq $1, %rax
	movq %rax, -56(%rbp)
	jmp _34_r22_c19_Logical_body
	jmp _26_r21_c27_Assign_assign
_26_r21_c27_Assign_assign:
	movq -40(%rbp), %rax
	addq $1, %rax
	movq %rax, -40(%rbp)
	jmp _22_r21_c17_Logical_body
	jmp _52_r26_c3_call_call
_52_r26_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_52_r26_c3_call_block:
	movq %rax, -96(%rbp)
_2_r15_c6_Method_ed:
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
_55_r30_c3_call_call:
	movq $str_r30_c23, %rdi
	movq cols, %rsi
	movq rows, %rdx
	xor %rax, %rax
	call pgm_open_for_write
	addq $0, %rsp
_55_r30_c3_call_block:
	movq %rax, -8(%rbp)
	movq cols, %rax
	imul rows, %rax
	movq %rax, size
	movq size, %rax
	movq %rax, -16(%rbp)
	movq $0, %rax
	movq %rax, -24(%rbp)
_69_r32_c17_Logical_body:
	movq -24(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -32(%rbp)
_65_r32_c3_For_cond:
	movq -32(%rbp), %rax
	test %rax, %rax
	je _98_r37_c3_call_call
_78_r33_c12_Assign_assign:
	movq $0, %rax
	movq %rax, -40(%rbp)
_81_r33_c19_Logical_body:
	movq -40(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -48(%rbp)
_77_r33_c5_For_cond:
	movq -48(%rbp), %rax
	test %rax, %rax
	je _73_r32_c27_Assign_assign
_92_r34_c39_Oper_expr:
	movq -24(%rbp), %rax
	imul $303, %rax
	movq %rax, -56(%rbp)
	movq -56(%rbp), %rax
	addq -40(%rbp), %rax
	movq %rax, -64(%rbp)
_89_r34_c7_call_call:
	movq -64(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -64(%rbp), %r10
	movq imageOut(, %r10, 8), %rdi
	xor %rax, %rax
	call pgm_write_next_pixel
	addq $0, %rsp
_89_r34_c7_call_block:
	movq %rax, -72(%rbp)
	movq -40(%rbp), %rax
	addq $1, %rax
	movq %rax, -40(%rbp)
	jmp _81_r33_c19_Logical_body
	jmp _73_r32_c27_Assign_assign
_73_r32_c27_Assign_assign:
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _69_r32_c17_Logical_body
	jmp _98_r37_c3_call_call
_98_r37_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_98_r37_c3_call_block:
	movq %rax, -80(%rbp)
_53_r28_c6_Method_ed:
	leave
	ret
.globl emboss
emboss:
	enter $-344, $0
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
	movq $0, -128(%rbp)
	movq $0, -136(%rbp)
	movq $0, -144(%rbp)
	movq $0, -152(%rbp)
	movq $0, -160(%rbp)
	movq $0, -168(%rbp)
	movq $0, -176(%rbp)
	movq $0, -184(%rbp)
	movq $0, -192(%rbp)
	movq $0, -200(%rbp)
	movq $0, -208(%rbp)
	movq $0, -216(%rbp)
	movq $0, -224(%rbp)
	movq $0, -232(%rbp)
	movq $0, -240(%rbp)
	movq $0, -248(%rbp)
	movq $0, -256(%rbp)
	movq $0, -264(%rbp)
	movq $0, -272(%rbp)
	movq $0, -280(%rbp)
	movq $0, -288(%rbp)
	movq $0, -296(%rbp)
	movq $0, -304(%rbp)
	movq $0, -312(%rbp)
	movq $0, -320(%rbp)
	movq $0, -328(%rbp)
	movq $0, -336(%rbp)
	movq $0, -344(%rbp)
_102_r41_c10_Assign_assign:
	movq $1, %rax
	movq %rax, -8(%rbp)
_107_r41_c23_Oper_expr:
	movq rows, %rax
	subq $1, %rax
	movq %rax, -16(%rbp)
	movq -8(%rbp), %rdx
	movq -16(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -24(%rbp)
_101_r41_c3_For_cond:
	movq -24(%rbp), %rax
	test %rax, %rax
	je _99_r39_c6_Method_ed
_117_r42_c12_Assign_assign:
	movq $1, %rax
	movq %rax, -32(%rbp)
_122_r42_c25_Oper_expr:
	movq cols, %rax
	subq $1, %rax
	movq %rax, -40(%rbp)
	movq -32(%rbp), %rdx
	movq -40(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -48(%rbp)
_116_r42_c5_For_cond:
	movq -48(%rbp), %rax
	test %rax, %rax
	je _112_r41_c29_Assign_assign
_137_r44_c23_Oper_expr:
	movq -8(%rbp), %rax
	subq $1, %rax
	movq %rax, -56(%rbp)
	movq -56(%rbp), %rax
	imul $303, %rax
	movq %rax, -64(%rbp)
	movq -64(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -72(%rbp)
	movq -72(%rbp), %rax
	subq $1, %rax
	movq %rax, -80(%rbp)
	movq -80(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -80(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -88(%rbp)
	movq -8(%rbp), %rax
	subq $1, %rax
	movq %rax, -96(%rbp)
	movq -96(%rbp), %rax
	imul $303, %rax
	movq %rax, -104(%rbp)
	movq -104(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -112(%rbp)
	movq -112(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -112(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -128(%rbp)
	movq -128(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -136(%rbp)
	movq -136(%rbp), %rax
	subq $1, %rax
	movq %rax, -144(%rbp)
	movq -144(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -144(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -152(%rbp)
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -168(%rbp)
	movq -168(%rbp), %rax
	addq $1, %rax
	movq %rax, -176(%rbp)
	movq -176(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -176(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -184(%rbp)
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	imul $303, %rax
	movq %rax, -200(%rbp)
	movq -200(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -208(%rbp)
	movq -208(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -208(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -224(%rbp)
	movq -224(%rbp), %rax
	imul $303, %rax
	movq %rax, -232(%rbp)
	movq -232(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -240(%rbp)
	movq -240(%rbp), %rax
	addq $1, %rax
	movq %rax, -248(%rbp)
	movq -248(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -248(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -256(%rbp)
	movq $128, %rax
	addq -184(%rbp), %rax
	movq %rax, -264(%rbp)
	movq -264(%rbp), %rax
	addq -216(%rbp), %rax
	movq %rax, -272(%rbp)
	movq -272(%rbp), %rax
	addq -256(%rbp), %rax
	movq %rax, -280(%rbp)
	movq -280(%rbp), %rax
	subq -88(%rbp), %rax
	movq %rax, -288(%rbp)
	movq -288(%rbp), %rax
	subq -120(%rbp), %rax
	movq %rax, -296(%rbp)
	movq -296(%rbp), %rax
	subq -152(%rbp), %rax
	movq %rax, -304(%rbp)
	movq -304(%rbp), %rax
	movq %rax, -312(%rbp)
	movq -304(%rbp), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -320(%rbp)
_236_r52_c7_If_cond:
	movq -320(%rbp), %rax
	test %rax, %rax
	je _246_r55_c13_Logical_body
_242_r53_c11_Assign_assign:
	movq $0, %rax
	movq %rax, -304(%rbp)
	jmp _246_r55_c13_Logical_body
	jmp _246_r55_c13_Logical_body
_246_r55_c13_Logical_body:
	movq -304(%rbp), %rdx
	movq $255, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setg %al
	movzbl %al, %eax
	movq %rax, -328(%rbp)
_245_r55_c7_If_cond:
	movq -328(%rbp), %rax
	test %rax, %rax
	je _257_r58_c17_Oper_expr
_251_r56_c11_Assign_assign:
	movq $255, %rax
	movq %rax, -304(%rbp)
	jmp _257_r58_c17_Oper_expr
	jmp _257_r58_c17_Oper_expr
_257_r58_c17_Oper_expr:
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -336(%rbp)
	movq -336(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -344(%rbp)
	movq -344(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -344(%rbp), %r10
	movq -304(%rbp), %rax
	movq %rax, imageOut(, %r10, 8)
	movq -32(%rbp), %rax
	addq $1, %rax
	movq %rax, -32(%rbp)
	jmp _122_r42_c25_Oper_expr
	jmp _112_r41_c29_Assign_assign
_112_r41_c29_Assign_assign:
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -8(%rbp)
	jmp _107_r41_c23_Oper_expr
	jmp _99_r39_c6_Method_ed
_99_r39_c6_Method_ed:
	leave
	ret
.globl main
main:
	enter $-16, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
_266_r63_c3_call_call:
	xor %rax, %rax
	call read
	addq $0, %rsp
_267_r64_c3_call_call:
	xor %rax, %rax
	call start_caliper
	addq $0, %rsp
_267_r64_c3_call_block:
	movq %rax, -8(%rbp)
_268_r65_c3_call_call:
	xor %rax, %rax
	call emboss
	addq $0, %rsp
_269_r66_c3_call_call:
	xor %rax, %rax
	call end_caliper
	addq $0, %rsp
_269_r66_c3_call_block:
	movq %rax, -16(%rbp)
_270_r67_c3_call_call:
	xor %rax, %rax
	call write
	addq $0, %rsp
_264_r62_c6_Method_ed:
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
	str_r17_c22:
		.string "saman.pgm"
	str_r30_c23:
		.string "saman_emboss.pgm"
