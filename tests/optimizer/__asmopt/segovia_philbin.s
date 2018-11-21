.bss
image:
	.zero 16000000
cols:
	.zero 8
rows:
	.zero 8
size:
	.zero 8
.text
.globl read
read:
	enter $-40, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
	movq $0, -32(%rbp)
	movq $0, -40(%rbp)
_4_r18_c3_call_call:
	movq $str_r18_c21, %rdi
	xor %rax, %rax
	call pgm_open_for_read
	addq $0, %rsp
_4_r18_c3_call_block:
_8_r19_c10_call_call:
	xor %rax, %rax
	call pgm_get_cols
	addq $0, %rsp
_8_r19_c10_call_block:
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	movq %rax, cols
_11_r20_c10_call_call:
	xor %rax, %rax
	call pgm_get_rows
	addq $0, %rsp
_11_r20_c10_call_block:
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq %rax, rows
	movq cols, %rax
	imul -16(%rbp), %rax
	movq %rax, size
	movq $0, %rax
	movq %rax, -24(%rbp)
_22_r22_c17_Logical_body:
	movq -24(%rbp), %rdx
	movq size, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -32(%rbp)
_18_r22_c3_For_cond:
	movq -32(%rbp), %rax
	test %rax, %rax
	je _34_r25_c3_call_call
_33_r23_c16_call_call:
	xor %rax, %rax
	call pgm_get_next_pixel
	addq $0, %rsp
_33_r23_c16_call_block:
	movq %rax, -40(%rbp)
	movq -24(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $2000000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -24(%rbp), %r10
	movq -40(%rbp), %rax
	movq %rax, image(, %r10, 8)
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _22_r22_c17_Logical_body
	jmp _34_r25_c3_call_call
_34_r25_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_34_r25_c3_call_block:
_2_r16_c6_Method_ed:
	leave
	ret
.globl write
write:
	enter $-16, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
_37_r29_c3_call_call:
	movq $str_r29_c22, %rdi
	movq cols, %rsi
	movq rows, %rdx
	xor %rax, %rax
	call pgm_open_for_write
	addq $0, %rsp
_37_r29_c3_call_block:
	movq cols, %rax
	imul rows, %rax
	movq %rax, size
	movq $0, %rax
	movq %rax, -8(%rbp)
_51_r31_c17_Logical_body:
	movq -8(%rbp), %rdx
	movq size, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -16(%rbp)
_47_r31_c3_For_cond:
	movq -16(%rbp), %rax
	test %rax, %rax
	je _62_r34_c3_call_call
_59_r32_c5_call_call:
	movq -8(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $2000000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -8(%rbp), %r10
	movq image(, %r10, 8), %rdi
	xor %rax, %rax
	call pgm_write_next_pixel
	addq $0, %rsp
_59_r32_c5_call_block:
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -8(%rbp)
	jmp _51_r31_c17_Logical_body
	jmp _62_r34_c3_call_call
_62_r34_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_62_r34_c3_call_block:
_35_r27_c6_Method_ed:
	leave
	ret
.globl philbin
philbin:
	enter $-336, $0
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
_65_r38_c7_Assign_assign:
	movq $0, %rax
	movq %rax, -8(%rbp)
_75_r40_c21_Logical_body:
	movq -8(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -16(%rbp)
_71_r40_c3_For_cond:
	movq -16(%rbp), %rax
	test %rax, %rax
	je _63_r36_c6_Method_ed
_84_r41_c14_Assign_assign:
	movq $0, %rax
	movq %rax, -24(%rbp)
_87_r41_c23_Logical_body:
	movq -24(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -32(%rbp)
_83_r41_c5_For_cond:
	movq -32(%rbp), %rax
	test %rax, %rax
	je _79_r40_c33_Assign_assign
_102_r46_c27_Oper_expr:
	movq $3, %rax
	imul cols, %rax
	movq %rax, -40(%rbp)
	movq -8(%rbp), %rax
	imul -40(%rbp), %rax
	movq %rax, -48(%rbp)
	movq -24(%rbp), %rax
	imul $3, %rax
	movq %rax, -56(%rbp)
	movq -48(%rbp), %rax
	addq -56(%rbp), %rax
	movq %rax, -64(%rbp)
	movq -64(%rbp), %rax
	addq $0, %rax
	movq %rax, -72(%rbp)
	movq -72(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $2000000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -72(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -80(%rbp)
	movq -8(%rbp), %rax
	imul -40(%rbp), %rax
	movq %rax, -88(%rbp)
	movq -88(%rbp), %rax
	addq -56(%rbp), %rax
	movq %rax, -96(%rbp)
	movq -96(%rbp), %rax
	addq $1, %rax
	movq %rax, -104(%rbp)
	movq -104(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $2000000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -104(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -112(%rbp)
	movq -8(%rbp), %rax
	imul -40(%rbp), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	addq -56(%rbp), %rax
	movq %rax, -128(%rbp)
	movq -128(%rbp), %rax
	addq $2, %rax
	movq %rax, -136(%rbp)
	movq -136(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $2000000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -136(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -144(%rbp)
	movq $-1, %rax
	movq %rax, -152(%rbp)
	movq -80(%rbp), %rax
	movq %rax, -160(%rbp)
	movq -112(%rbp), %rax
	movq %rax, -168(%rbp)
	movq -144(%rbp), %rdx
	movq -80(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setge %al
	movzbl %al, %eax
	movq %rax, -176(%rbp)
_287_r53_c17_Logical_cond1:
	movq -176(%rbp), %rax
	test %rax, %rax
	je _291_r53_c17_Assign_assign
_170_r53_c21_Logical_body:
	movq -144(%rbp), %rdx
	movq -80(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setge %al
	movzbl %al, %eax
	movq %rax, -184(%rbp)
_287_r53_c17_Logical_cond2:
	movq -184(%rbp), %rax
	test %rax, %rax
	je _291_r53_c17_Assign_assign
_288_r53_c17_Assign_assign:
	movq $1, %rax
	movq %rax, -192(%rbp)
	jmp _164_r53_c7_If_cond
_291_r53_c17_Assign_assign:
	movq $0, %rax
	movq %rax, -192(%rbp)
	jmp _164_r53_c7_If_cond
	jmp _164_r53_c7_If_cond
	jmp _291_r53_c17_Assign_assign
_164_r53_c7_If_cond:
	movq -192(%rbp), %rax
	test %rax, %rax
	je _181_r56_c11_Logical_body
_176_r54_c12_Assign_assign:
	movq -144(%rbp), %rax
	movq %rax, -160(%rbp)
	jmp _181_r56_c11_Logical_body
	jmp _181_r56_c11_Logical_body
_181_r56_c11_Logical_body:
	movq -112(%rbp), %rdx
	movq -160(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setge %al
	movzbl %al, %eax
	movq %rax, -200(%rbp)
_294_r56_c17_Logical_cond1:
	movq -200(%rbp), %rax
	test %rax, %rax
	je _298_r56_c17_Assign_assign
_185_r56_c21_Logical_body:
	movq -112(%rbp), %rdx
	movq -160(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setge %al
	movzbl %al, %eax
	movq %rax, -208(%rbp)
_294_r56_c17_Logical_cond2:
	movq -208(%rbp), %rax
	test %rax, %rax
	je _298_r56_c17_Assign_assign
_295_r56_c17_Assign_assign:
	movq $1, %rax
	movq %rax, -216(%rbp)
	jmp _179_r56_c7_If_cond
_298_r56_c17_Assign_assign:
	movq $0, %rax
	movq %rax, -216(%rbp)
	jmp _179_r56_c7_If_cond
	jmp _179_r56_c7_If_cond
	jmp _298_r56_c17_Assign_assign
_179_r56_c7_If_cond:
	movq -216(%rbp), %rax
	test %rax, %rax
	je _196_r59_c11_Logical_body
_191_r57_c12_Assign_assign:
	movq -112(%rbp), %rax
	movq %rax, -160(%rbp)
	jmp _196_r59_c11_Logical_body
	jmp _196_r59_c11_Logical_body
_196_r59_c11_Logical_body:
	movq -144(%rbp), %rdx
	movq -112(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setle %al
	movzbl %al, %eax
	movq %rax, -224(%rbp)
_280_r59_c17_Logical_cond1:
	movq -224(%rbp), %rax
	test %rax, %rax
	je _284_r59_c17_Assign_assign
_200_r59_c21_Logical_body:
	movq -144(%rbp), %rdx
	movq -112(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setle %al
	movzbl %al, %eax
	movq %rax, -232(%rbp)
_280_r59_c17_Logical_cond2:
	movq -232(%rbp), %rax
	test %rax, %rax
	je _284_r59_c17_Assign_assign
_281_r59_c17_Assign_assign:
	movq $1, %rax
	movq %rax, -240(%rbp)
	jmp _194_r59_c7_If_cond
_284_r59_c17_Assign_assign:
	movq $0, %rax
	movq %rax, -240(%rbp)
	jmp _194_r59_c7_If_cond
	jmp _194_r59_c7_If_cond
	jmp _284_r59_c17_Assign_assign
_194_r59_c7_If_cond:
	movq -240(%rbp), %rax
	test %rax, %rax
	je _211_r62_c11_Logical_body
_206_r60_c12_Assign_assign:
	movq -144(%rbp), %rax
	movq %rax, -168(%rbp)
	jmp _211_r62_c11_Logical_body
	jmp _211_r62_c11_Logical_body
_211_r62_c11_Logical_body:
	movq -112(%rbp), %rdx
	movq -168(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setle %al
	movzbl %al, %eax
	movq %rax, -248(%rbp)
_301_r62_c17_Logical_cond1:
	movq -248(%rbp), %rax
	test %rax, %rax
	je _305_r62_c17_Assign_assign
_215_r62_c21_Logical_body:
	movq -112(%rbp), %rdx
	movq -168(%rbp), %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setle %al
	movzbl %al, %eax
	movq %rax, -256(%rbp)
_301_r62_c17_Logical_cond2:
	movq -256(%rbp), %rax
	test %rax, %rax
	je _305_r62_c17_Assign_assign
_302_r62_c17_Assign_assign:
	movq $1, %rax
	movq %rax, -264(%rbp)
	jmp _209_r62_c7_If_cond
_305_r62_c17_Assign_assign:
	movq $0, %rax
	movq %rax, -264(%rbp)
	jmp _209_r62_c7_If_cond
	jmp _209_r62_c7_If_cond
	jmp _305_r62_c17_Assign_assign
_209_r62_c7_If_cond:
	movq -264(%rbp), %rax
	test %rax, %rax
	je _226_r65_c19_Oper_expr
_221_r63_c12_Assign_assign:
	movq -112(%rbp), %rax
	movq %rax, -168(%rbp)
	jmp _226_r65_c19_Oper_expr
	jmp _226_r65_c19_Oper_expr
_226_r65_c19_Oper_expr:
	movq -160(%rbp), %rax
	subq -168(%rbp), %rax
	movq %rax, -272(%rbp)
	movq -272(%rbp), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setne %al
	movzbl %al, %eax
	movq %rax, -280(%rbp)
_230_r66_c7_If_cond:
	movq -280(%rbp), %rax
	test %rax, %rax
	je _254_r69_c23_Oper_expr
_241_r67_c20_Oper_expr:
	movq -112(%rbp), %rax
	subq -144(%rbp), %rax
	movq %rax, -288(%rbp)
	movq -288(%rbp), %rax
	movq -272(%rbp), %rsi
	cqto
	idivq %rsi
	movq %rax, -296(%rbp)
	movq $60, %rax
	imul -296(%rbp), %rax
	movq %rax, -152(%rbp)
	jmp _254_r69_c23_Oper_expr
	jmp _254_r69_c23_Oper_expr
_254_r69_c23_Oper_expr:
	movq $3, %rax
	imul cols, %rax
	movq %rax, -304(%rbp)
	movq -8(%rbp), %rax
	imul -304(%rbp), %rax
	movq %rax, -312(%rbp)
	movq -24(%rbp), %rax
	imul $3, %rax
	movq %rax, -320(%rbp)
	movq -312(%rbp), %rax
	addq -320(%rbp), %rax
	movq %rax, -328(%rbp)
	movq -328(%rbp), %rax
	addq $0, %rax
	movq %rax, -336(%rbp)
	movq -336(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $2000000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -336(%rbp), %r10
	movq -152(%rbp), %rax
	movq %rax, image(, %r10, 8)
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _87_r41_c23_Logical_body
	jmp _79_r40_c33_Assign_assign
_79_r40_c33_Assign_assign:
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -8(%rbp)
	jmp _75_r40_c21_Logical_body
	jmp _63_r36_c6_Method_ed
_63_r36_c6_Method_ed:
	leave
	ret
.globl main
main:
	enter $0, $0
_269_r74_c3_call_call:
	xor %rax, %rax
	call read
	addq $0, %rsp
_270_r75_c3_call_call:
	xor %rax, %rax
	call start_caliper
	addq $0, %rsp
_270_r75_c3_call_block:
	movq cols, %rax
	movq $3, %rsi
	cqto
	idivq %rsi
	movq %rax, cols
_277_r77_c3_call_call:
	xor %rax, %rax
	call philbin
	addq $0, %rsp
_278_r78_c3_call_call:
	xor %rax, %rax
	call end_caliper
	addq $0, %rsp
_278_r78_c3_call_block:
_279_r79_c3_call_call:
	xor %rax, %rax
	call write
	addq $0, %rsp
_267_r73_c6_Method_ed:
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
	str_r18_c21:
		.string "segovia.pgm"
	str_r29_c22:
		.string "segovia_philbin.pgm"
