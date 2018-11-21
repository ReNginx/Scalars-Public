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
_45_r22_c16_Oper_expr:
	movq -40(%rbp), %rax
	imul $303, %rax
	movq %rax, -72(%rbp)
	movq -72(%rbp), %rax
	addq -56(%rbp), %rax
	movq %rax, -80(%rbp)
_51_r22_c26_call_call:
	xor %rax, %rax
	call pgm_get_next_pixel
	addq $0, %rsp
_51_r22_c26_call_block:
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
_92_r33_c38_Oper_expr:
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
	movq imageOut(, %r10, 8), %rdi
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
.globl filter
filter:
	enter $-1176, $0
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
	movq $0, -352(%rbp)
	movq $0, -360(%rbp)
	movq $0, -368(%rbp)
	movq $0, -376(%rbp)
	movq $0, -384(%rbp)
	movq $0, -392(%rbp)
	movq $0, -400(%rbp)
	movq $0, -408(%rbp)
	movq $0, -416(%rbp)
	movq $0, -424(%rbp)
	movq $0, -432(%rbp)
	movq $0, -440(%rbp)
	movq $0, -448(%rbp)
	movq $0, -456(%rbp)
	movq $0, -464(%rbp)
	movq $0, -472(%rbp)
	movq $0, -480(%rbp)
	movq $0, -488(%rbp)
	movq $0, -496(%rbp)
	movq $0, -504(%rbp)
	movq $0, -512(%rbp)
	movq $0, -520(%rbp)
	movq $0, -528(%rbp)
	movq $0, -536(%rbp)
	movq $0, -544(%rbp)
	movq $0, -552(%rbp)
	movq $0, -560(%rbp)
	movq $0, -568(%rbp)
	movq $0, -576(%rbp)
	movq $0, -584(%rbp)
	movq $0, -592(%rbp)
	movq $0, -600(%rbp)
	movq $0, -608(%rbp)
	movq $0, -616(%rbp)
	movq $0, -624(%rbp)
	movq $0, -632(%rbp)
	movq $0, -640(%rbp)
	movq $0, -648(%rbp)
	movq $0, -656(%rbp)
	movq $0, -664(%rbp)
	movq $0, -672(%rbp)
	movq $0, -680(%rbp)
	movq $0, -688(%rbp)
	movq $0, -696(%rbp)
	movq $0, -704(%rbp)
	movq $0, -712(%rbp)
	movq $0, -720(%rbp)
	movq $0, -728(%rbp)
	movq $0, -736(%rbp)
	movq $0, -744(%rbp)
	movq $0, -752(%rbp)
	movq $0, -760(%rbp)
	movq $0, -768(%rbp)
	movq $0, -776(%rbp)
	movq $0, -784(%rbp)
	movq $0, -792(%rbp)
	movq $0, -800(%rbp)
	movq $0, -808(%rbp)
	movq $0, -816(%rbp)
	movq $0, -824(%rbp)
	movq $0, -832(%rbp)
	movq $0, -840(%rbp)
	movq $0, -848(%rbp)
	movq $0, -856(%rbp)
	movq $0, -864(%rbp)
	movq $0, -872(%rbp)
	movq $0, -880(%rbp)
	movq $0, -888(%rbp)
	movq $0, -896(%rbp)
	movq $0, -904(%rbp)
	movq $0, -912(%rbp)
	movq $0, -920(%rbp)
	movq $0, -928(%rbp)
	movq $0, -936(%rbp)
	movq $0, -944(%rbp)
	movq $0, -952(%rbp)
	movq $0, -960(%rbp)
	movq $0, -968(%rbp)
	movq $0, -976(%rbp)
	movq $0, -984(%rbp)
	movq $0, -992(%rbp)
	movq $0, -1000(%rbp)
	movq $0, -1008(%rbp)
	movq $0, -1016(%rbp)
	movq $0, -1024(%rbp)
	movq $0, -1032(%rbp)
	movq $0, -1040(%rbp)
	movq $0, -1048(%rbp)
	movq $0, -1056(%rbp)
	movq $0, -1064(%rbp)
	movq $0, -1072(%rbp)
	movq $0, -1080(%rbp)
	movq $0, -1088(%rbp)
	movq $0, -1096(%rbp)
	movq $0, -1104(%rbp)
	movq $0, -1112(%rbp)
	movq $0, -1120(%rbp)
	movq $0, -1128(%rbp)
	movq $0, -1136(%rbp)
	movq $0, -1144(%rbp)
	movq $0, -1152(%rbp)
	movq $0, -1160(%rbp)
	movq $0, -1168(%rbp)
	movq $0, -1176(%rbp)
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
	je _99_r38_c6_Method_ed
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
_137_r44_c22_Oper_expr:
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
	subq $1, %rax
	movq %rax, -128(%rbp)
	movq -128(%rbp), %rax
	imul $303, %rax
	movq %rax, -136(%rbp)
	movq -136(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -144(%rbp)
	movq -144(%rbp), %rax
	addq $1, %rax
	movq %rax, -152(%rbp)
	movq -152(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -152(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -168(%rbp)
	movq -168(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -176(%rbp)
	movq -176(%rbp), %rax
	subq $1, %rax
	movq %rax, -184(%rbp)
	movq -184(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -184(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -8(%rbp), %rax
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
	imul $303, %rax
	movq %rax, -224(%rbp)
	movq -224(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -232(%rbp)
	movq -232(%rbp), %rax
	addq $1, %rax
	movq %rax, -240(%rbp)
	movq -240(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -240(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -248(%rbp)
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -256(%rbp)
	movq -256(%rbp), %rax
	imul $303, %rax
	movq %rax, -264(%rbp)
	movq -264(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -272(%rbp)
	movq -272(%rbp), %rax
	subq $1, %rax
	movq %rax, -280(%rbp)
	movq -280(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -280(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -288(%rbp)
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -296(%rbp)
	movq -296(%rbp), %rax
	imul $303, %rax
	movq %rax, -304(%rbp)
	movq -304(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -312(%rbp)
	movq -312(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -312(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -320(%rbp)
	movq -8(%rbp), %rax
	addq $1, %rax
	movq %rax, -328(%rbp)
	movq -328(%rbp), %rax
	imul $303, %rax
	movq %rax, -336(%rbp)
	movq -336(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -344(%rbp)
	movq -344(%rbp), %rax
	addq $1, %rax
	movq %rax, -352(%rbp)
	movq -352(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -352(%rbp), %r11
	movq imageIn(, %r11, 8), %rax
	movq %rax, -360(%rbp)
	movq -192(%rbp), %rax
	subq -88(%rbp), %rax
	movq $0, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $0, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $0, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -528(%rbp)
	movq $0, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $0, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -536(%rbp)
_265_r54_c7_If_cond:
	movq -536(%rbp), %rax
	test %rax, %rax
	je _289_r58_c17_Oper_expr
_274_r55_c17_Oper_expr:
	movq $0, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $0, %r11
	movq -88(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -88(%rbp)
	movq -88(%rbp), %rax
	movq %rax, -544(%rbp)
	movq $0, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $0, %r11
	movq -192(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -552(%rbp)
	jmp _289_r58_c17_Oper_expr
	jmp _289_r58_c17_Oper_expr
_289_r58_c17_Oper_expr:
	movq -216(%rbp), %rax
	subq -120(%rbp), %rax
	movq $1, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $1, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $1, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -560(%rbp)
	movq $1, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $1, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -568(%rbp)
_294_r59_c7_If_cond:
	movq -568(%rbp), %rax
	test %rax, %rax
	je _318_r63_c17_Oper_expr
_303_r60_c17_Oper_expr:
	movq $1, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $1, %r11
	movq -120(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	movq %rax, -576(%rbp)
	movq $1, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $1, %r11
	movq -216(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -584(%rbp)
	jmp _318_r63_c17_Oper_expr
	jmp _318_r63_c17_Oper_expr
_318_r63_c17_Oper_expr:
	movq -248(%rbp), %rax
	subq -160(%rbp), %rax
	movq $2, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $2, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $2, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -592(%rbp)
	movq $2, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $2, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -600(%rbp)
_323_r64_c7_If_cond:
	movq -600(%rbp), %rax
	test %rax, %rax
	je _347_r68_c17_Oper_expr
_332_r65_c17_Oper_expr:
	movq $2, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $2, %r11
	movq -160(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq %rax, -608(%rbp)
	movq $2, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $2, %r11
	movq -248(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -248(%rbp)
	movq -248(%rbp), %rax
	movq %rax, -616(%rbp)
	jmp _347_r68_c17_Oper_expr
	jmp _347_r68_c17_Oper_expr
_347_r68_c17_Oper_expr:
	movq -120(%rbp), %rax
	subq -88(%rbp), %rax
	movq $3, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $3, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $3, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -624(%rbp)
	movq $3, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $3, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -632(%rbp)
_352_r69_c7_If_cond:
	movq -632(%rbp), %rax
	test %rax, %rax
	je _376_r73_c17_Oper_expr
_361_r70_c17_Oper_expr:
	movq $3, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $3, %r11
	movq -88(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -88(%rbp)
	movq -88(%rbp), %rax
	movq %rax, -640(%rbp)
	movq $3, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $3, %r11
	movq -120(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	movq %rax, -648(%rbp)
	jmp _376_r73_c17_Oper_expr
	jmp _376_r73_c17_Oper_expr
_376_r73_c17_Oper_expr:
	movq -160(%rbp), %rax
	subq -88(%rbp), %rax
	movq $4, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $4, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $4, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -656(%rbp)
	movq $4, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $4, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -664(%rbp)
_381_r74_c7_If_cond:
	movq -664(%rbp), %rax
	test %rax, %rax
	je _405_r78_c17_Oper_expr
_390_r75_c17_Oper_expr:
	movq $4, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $4, %r11
	movq -88(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -88(%rbp)
	movq -88(%rbp), %rax
	movq %rax, -672(%rbp)
	movq $4, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $4, %r11
	movq -160(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq %rax, -680(%rbp)
	jmp _405_r78_c17_Oper_expr
	jmp _405_r78_c17_Oper_expr
_405_r78_c17_Oper_expr:
	movq -248(%rbp), %rax
	subq -216(%rbp), %rax
	movq $5, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $5, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $5, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -688(%rbp)
	movq $5, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $5, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -696(%rbp)
_410_r79_c7_If_cond:
	movq -696(%rbp), %rax
	test %rax, %rax
	je _434_r83_c17_Oper_expr
_419_r80_c17_Oper_expr:
	movq $5, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $5, %r11
	movq -216(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -704(%rbp)
	movq $5, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $5, %r11
	movq -248(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -248(%rbp)
	movq -248(%rbp), %rax
	movq %rax, -712(%rbp)
	jmp _434_r83_c17_Oper_expr
	jmp _434_r83_c17_Oper_expr
_434_r83_c17_Oper_expr:
	movq -248(%rbp), %rax
	subq -192(%rbp), %rax
	movq $6, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $6, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $6, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -720(%rbp)
	movq $6, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $6, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -728(%rbp)
_439_r84_c7_If_cond:
	movq -728(%rbp), %rax
	test %rax, %rax
	je _463_r88_c17_Oper_expr
_448_r85_c17_Oper_expr:
	movq $6, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $6, %r11
	movq -192(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -736(%rbp)
	movq $6, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $6, %r11
	movq -248(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -248(%rbp)
	movq -248(%rbp), %rax
	movq %rax, -744(%rbp)
	jmp _463_r88_c17_Oper_expr
	jmp _463_r88_c17_Oper_expr
_463_r88_c17_Oper_expr:
	movq -160(%rbp), %rax
	subq -120(%rbp), %rax
	movq $7, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $7, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $7, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -752(%rbp)
	movq $7, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $7, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -760(%rbp)
_468_r89_c7_If_cond:
	movq -760(%rbp), %rax
	test %rax, %rax
	je _492_r93_c17_Oper_expr
_477_r90_c17_Oper_expr:
	movq $7, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $7, %r11
	movq -120(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	movq %rax, -768(%rbp)
	movq $7, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $7, %r11
	movq -160(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq %rax, -776(%rbp)
	jmp _492_r93_c17_Oper_expr
	jmp _492_r93_c17_Oper_expr
_492_r93_c17_Oper_expr:
	movq -216(%rbp), %rax
	subq -192(%rbp), %rax
	movq $8, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $8, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $8, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -784(%rbp)
	movq $8, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $8, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -792(%rbp)
_497_r94_c7_If_cond:
	movq -792(%rbp), %rax
	test %rax, %rax
	je _521_r98_c17_Oper_expr
_506_r95_c17_Oper_expr:
	movq $8, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $8, %r11
	movq -192(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -800(%rbp)
	movq $8, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $8, %r11
	movq -216(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -808(%rbp)
	jmp _521_r98_c17_Oper_expr
	jmp _521_r98_c17_Oper_expr
_521_r98_c17_Oper_expr:
	movq -192(%rbp), %rax
	subq -120(%rbp), %rax
	movq $9, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $9, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $9, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -816(%rbp)
	movq $9, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $9, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -824(%rbp)
_526_r99_c7_If_cond:
	movq -824(%rbp), %rax
	test %rax, %rax
	je _550_r103_c18_Oper_expr
_535_r100_c17_Oper_expr:
	movq $9, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $9, %r11
	movq -120(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	movq %rax, -832(%rbp)
	movq $9, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $9, %r11
	movq -192(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -840(%rbp)
	jmp _550_r103_c18_Oper_expr
	jmp _550_r103_c18_Oper_expr
_550_r103_c18_Oper_expr:
	movq -288(%rbp), %rax
	subq -120(%rbp), %rax
	movq $10, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $10, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $10, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -848(%rbp)
	movq $10, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $10, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -856(%rbp)
_555_r104_c7_If_cond:
	movq -856(%rbp), %rax
	test %rax, %rax
	je _579_r108_c18_Oper_expr
_564_r105_c17_Oper_expr:
	movq $10, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $10, %r11
	movq -120(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -120(%rbp), %rax
	movq %rax, -864(%rbp)
	movq $10, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $10, %r11
	movq -288(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -288(%rbp)
	movq -288(%rbp), %rax
	movq %rax, -872(%rbp)
	jmp _579_r108_c18_Oper_expr
	jmp _579_r108_c18_Oper_expr
_579_r108_c18_Oper_expr:
	movq -288(%rbp), %rax
	subq -216(%rbp), %rax
	movq $11, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $11, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $11, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -880(%rbp)
	movq $11, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $11, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -888(%rbp)
_584_r109_c7_If_cond:
	movq -888(%rbp), %rax
	test %rax, %rax
	je _608_r113_c18_Oper_expr
_593_r110_c17_Oper_expr:
	movq $11, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $11, %r11
	movq -216(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -896(%rbp)
	movq $11, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $11, %r11
	movq -288(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -288(%rbp)
	movq -288(%rbp), %rax
	movq %rax, -904(%rbp)
	jmp _608_r113_c18_Oper_expr
	jmp _608_r113_c18_Oper_expr
_608_r113_c18_Oper_expr:
	movq -288(%rbp), %rax
	subq -160(%rbp), %rax
	movq $12, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $12, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $12, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -912(%rbp)
	movq $12, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $12, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -920(%rbp)
_613_r114_c7_If_cond:
	movq -920(%rbp), %rax
	test %rax, %rax
	je _637_r118_c18_Oper_expr
_622_r115_c17_Oper_expr:
	movq $12, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $12, %r11
	movq -160(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq %rax, -928(%rbp)
	movq $12, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $12, %r11
	movq -288(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -288(%rbp)
	movq -288(%rbp), %rax
	movq %rax, -936(%rbp)
	jmp _637_r118_c18_Oper_expr
	jmp _637_r118_c18_Oper_expr
_637_r118_c18_Oper_expr:
	movq -192(%rbp), %rax
	subq -160(%rbp), %rax
	movq $13, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $13, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $13, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -944(%rbp)
	movq $13, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $13, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -952(%rbp)
_642_r119_c7_If_cond:
	movq -952(%rbp), %rax
	test %rax, %rax
	je _666_r123_c18_Oper_expr
_651_r120_c17_Oper_expr:
	movq $13, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $13, %r11
	movq -160(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq %rax, -960(%rbp)
	movq $13, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $13, %r11
	movq -192(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -968(%rbp)
	jmp _666_r123_c18_Oper_expr
	jmp _666_r123_c18_Oper_expr
_666_r123_c18_Oper_expr:
	movq -320(%rbp), %rax
	subq -216(%rbp), %rax
	movq $14, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $14, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $14, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -976(%rbp)
	movq $14, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $14, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -984(%rbp)
_671_r124_c7_If_cond:
	movq -984(%rbp), %rax
	test %rax, %rax
	je _695_r128_c18_Oper_expr
_680_r125_c17_Oper_expr:
	movq $14, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $14, %r11
	movq -216(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -992(%rbp)
	movq $14, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $14, %r11
	movq -320(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -320(%rbp)
	movq -320(%rbp), %rax
	movq %rax, -1000(%rbp)
	jmp _695_r128_c18_Oper_expr
	jmp _695_r128_c18_Oper_expr
_695_r128_c18_Oper_expr:
	movq -216(%rbp), %rax
	subq -160(%rbp), %rax
	movq $15, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $15, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $15, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -1008(%rbp)
	movq $15, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $15, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -1016(%rbp)
_700_r129_c7_If_cond:
	movq -1016(%rbp), %rax
	test %rax, %rax
	je _724_r133_c18_Oper_expr
_709_r130_c17_Oper_expr:
	movq $15, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $15, %r11
	movq -160(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq %rax, -1024(%rbp)
	movq $15, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $15, %r11
	movq -216(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -1032(%rbp)
	jmp _724_r133_c18_Oper_expr
	jmp _724_r133_c18_Oper_expr
_724_r133_c18_Oper_expr:
	movq -320(%rbp), %rax
	subq -192(%rbp), %rax
	movq $16, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $16, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $16, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -1040(%rbp)
	movq $16, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $16, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -1048(%rbp)
_729_r134_c7_If_cond:
	movq -1048(%rbp), %rax
	test %rax, %rax
	je _753_r138_c18_Oper_expr
_738_r135_c17_Oper_expr:
	movq $16, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $16, %r11
	movq -192(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -1056(%rbp)
	movq $16, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $16, %r11
	movq -320(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -320(%rbp)
	movq -320(%rbp), %rax
	movq %rax, -1064(%rbp)
	jmp _753_r138_c18_Oper_expr
	jmp _753_r138_c18_Oper_expr
_753_r138_c18_Oper_expr:
	movq -360(%rbp), %rax
	subq -216(%rbp), %rax
	movq $17, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $17, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $17, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -1072(%rbp)
	movq $17, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $17, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -1080(%rbp)
_758_r139_c7_If_cond:
	movq -1080(%rbp), %rax
	test %rax, %rax
	je _782_r143_c18_Oper_expr
_767_r140_c17_Oper_expr:
	movq $17, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $17, %r11
	movq -216(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -1088(%rbp)
	movq $17, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $17, %r11
	movq -360(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -360(%rbp)
	movq -360(%rbp), %rax
	movq %rax, -1096(%rbp)
	jmp _782_r143_c18_Oper_expr
	jmp _782_r143_c18_Oper_expr
_782_r143_c18_Oper_expr:
	movq -360(%rbp), %rax
	subq -192(%rbp), %rax
	movq $18, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $18, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $18, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -1104(%rbp)
	movq $18, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $18, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -1112(%rbp)
_787_r144_c7_If_cond:
	movq -1112(%rbp), %rax
	test %rax, %rax
	je _811_r148_c18_Oper_expr
_796_r145_c17_Oper_expr:
	movq $18, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $18, %r11
	movq -192(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -1120(%rbp)
	movq $18, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $18, %r11
	movq -360(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -360(%rbp)
	movq -360(%rbp), %rax
	movq %rax, -1128(%rbp)
	jmp _811_r148_c18_Oper_expr
	jmp _811_r148_c18_Oper_expr
_811_r148_c18_Oper_expr:
	movq -216(%rbp), %rax
	subq -192(%rbp), %rax
	movq $19, %r10
	movq %rax, -520(%rbp, %r10, 8)
	movq $19, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $19, %r11
	movq -520(%rbp, %r11, 8), %rax
	movq %rax, -1136(%rbp)
	movq $19, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $19, %r10
	movq -520(%rbp, %r10, 8), %rdx
	movq $0, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -1144(%rbp)
_816_r149_c7_If_cond:
	movq -1144(%rbp), %rax
	test %rax, %rax
	je _840_r153_c17_Oper_expr
_825_r150_c17_Oper_expr:
	movq $19, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $19, %r11
	movq -192(%rbp), %rax
	addq -520(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -1152(%rbp)
	movq $19, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $20, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $19, %r11
	movq -216(%rbp), %rax
	subq -520(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -216(%rbp), %rax
	movq %rax, -1160(%rbp)
	jmp _840_r153_c17_Oper_expr
	jmp _840_r153_c17_Oper_expr
_840_r153_c17_Oper_expr:
	movq -8(%rbp), %rax
	imul $303, %rax
	movq %rax, -1168(%rbp)
	movq -1168(%rbp), %rax
	addq -32(%rbp), %rax
	movq %rax, -1176(%rbp)
	movq -1176(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $130000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -1176(%rbp), %r10
	movq -216(%rbp), %rax
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
	jmp _99_r38_c6_Method_ed
_99_r38_c6_Method_ed:
	leave
	ret
.globl main
main:
	enter $-16, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
_849_r158_c3_call_call:
	xor %rax, %rax
	call read
	addq $0, %rsp
_850_r159_c3_call_call:
	xor %rax, %rax
	call start_caliper
	addq $0, %rsp
_850_r159_c3_call_block:
	movq %rax, -8(%rbp)
_851_r160_c3_call_call:
	xor %rax, %rax
	call filter
	addq $0, %rsp
_852_r161_c3_call_call:
	xor %rax, %rax
	call end_caliper
	addq $0, %rsp
_852_r161_c3_call_block:
	movq %rax, -16(%rbp)
_853_r162_c3_call_call:
	xor %rax, %rax
	call write
	addq $0, %rsp
_847_r157_c6_Method_ed:
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
		.string "noise.pgm"
	str_r29_c22:
		.string "noise_median.pgm"
