.bss
image:
	.zero 6400000
cols:
	.zero 8
rows:
	.zero 8
size:
	.zero 8
.text
.globl read
read:
	enter $-64, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
	movq $0, -32(%rbp)
	movq $0, -40(%rbp)
	movq $0, -48(%rbp)
	movq $0, -56(%rbp)
	movq $0, -64(%rbp)
_4_r16_c3_call_call:
	movq $str_r16_c22, %rdi
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
	movq size, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -48(%rbp)
_18_r20_c3_For_cond:
	movq -48(%rbp), %rax
	test %rax, %rax
	je _34_r23_c3_call_call
_33_r21_c16_call_call:
	xor %rax, %rax
	call pgm_get_next_pixel
	addq $0, %rsp
_33_r21_c16_call_block:
	movq %rax, -56(%rbp)
	movq -40(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -40(%rbp), %r10
	movq -56(%rbp), %rax
	movq %rax, image(, %r10, 8)
	movq -40(%rbp), %rax
	addq $1, %rax
	movq %rax, -40(%rbp)
	jmp _22_r20_c17_Logical_body
	jmp _34_r23_c3_call_call
_34_r23_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_34_r23_c3_call_block:
	movq %rax, -64(%rbp)
_2_r14_c6_Method_ed:
	leave
	ret
.globl write
write:
	enter $-48, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
	movq $0, -24(%rbp)
	movq $0, -32(%rbp)
	movq $0, -40(%rbp)
	movq $0, -48(%rbp)
_37_r27_c3_call_call:
	movq $str_r27_c23, %rdi
	movq cols, %rsi
	movq rows, %rdx
	xor %rax, %rax
	call pgm_open_for_write
	addq $0, %rsp
_37_r27_c3_call_block:
	movq %rax, -8(%rbp)
	movq cols, %rax
	imul rows, %rax
	movq %rax, size
	movq size, %rax
	movq %rax, -16(%rbp)
	movq $0, %rax
	movq %rax, -24(%rbp)
_51_r29_c17_Logical_body:
	movq -24(%rbp), %rdx
	movq size, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -32(%rbp)
_47_r29_c3_For_cond:
	movq -32(%rbp), %rax
	test %rax, %rax
	je _62_r32_c3_call_call
_59_r30_c5_call_call:
	movq -24(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -24(%rbp), %r10
	movq image(, %r10, 8), %rdi
	xor %rax, %rax
	call pgm_write_next_pixel
	addq $0, %rsp
_59_r30_c5_call_block:
	movq %rax, -40(%rbp)
	movq -24(%rbp), %rax
	addq $1, %rax
	movq %rax, -24(%rbp)
	jmp _51_r29_c17_Logical_body
	jmp _62_r32_c3_call_call
_62_r32_c3_call_call:
	xor %rax, %rax
	call pgm_close
	addq $0, %rsp
_62_r32_c3_call_block:
	movq %rax, -48(%rbp)
_35_r25_c6_Method_ed:
	leave
	ret
.globl gaussian_blur
gaussian_blur:
	enter $-376, $0
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
_65_r38_c13_Assign_assign:
	movq $0, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $0, %r10
	movq $4433, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $1, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $1, %r10
	movq $54006, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $2, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $2, %r10
	movq $242036, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $3, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $3, %r10
	movq $399050, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $4, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $4, %r10
	movq $242036, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $5, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $5, %r10
	movq $54006, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $6, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $6, %r10
	movq $4433, %rax
	movq %rax, -56(%rbp, %r10, 8)
	movq $0, %rax
	movq %rax, -64(%rbp)
_97_r45_c17_Logical_body:
	movq -64(%rbp), %rdx
	movq $7, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -72(%rbp)
_93_r45_c3_For_cond:
	movq -72(%rbp), %rax
	test %rax, %rax
	je _113_r49_c10_Assign_assign
_107_r46_c29_Oper_expr:
	movq -64(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -64(%rbp), %r11
	movq -80(%rbp), %rax
	addq -56(%rbp, %r11, 8), %rax
	movq %rax, -80(%rbp)
	movq -80(%rbp), %rax
	movq %rax, -88(%rbp)
	movq -64(%rbp), %rax
	addq $1, %rax
	movq %rax, -64(%rbp)
	jmp _97_r45_c17_Logical_body
	jmp _113_r49_c10_Assign_assign
_113_r49_c10_Assign_assign:
	movq $0, %rax
	movq %rax, -96(%rbp)
_116_r49_c17_Logical_body:
	movq -96(%rbp), %rdx
	movq rows, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -104(%rbp)
_112_r49_c3_For_cond:
	movq -104(%rbp), %rax
	test %rax, %rax
	je _63_r34_c6_Method_ed
_127_r51_c17_Oper_expr:
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -112(%rbp)
	movq -112(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -112(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -120(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -128(%rbp)
	movq -128(%rbp), %rax
	addq $1, %rax
	movq %rax, -136(%rbp)
	movq -136(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -136(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -144(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -152(%rbp)
	movq -152(%rbp), %rax
	addq $2, %rax
	movq %rax, -160(%rbp)
	movq -160(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -160(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -168(%rbp)
	movq $3, %rax
	movq %rax, -176(%rbp)
_155_r54_c19_Logical_body:
	movq -176(%rbp), %rdx
	movq cols, %rdi
	xor %rax, %rax
	cmpq %rdi, %rdx
	setl %al
	movzbl %al, %eax
	movq %rax, -184(%rbp)
_151_r54_c5_For_cond:
	movq -184(%rbp), %rax
	test %rax, %rax
	je _120_r49_c27_Assign_assign
_165_r56_c17_Oper_expr:
	movq $0, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $0, %r11
	movq -120(%rbp), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -192(%rbp)
	movq -192(%rbp), %rax
	movq %rax, -200(%rbp)
	movq $1, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $1, %r11
	movq -144(%rbp), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -208(%rbp)
	movq -192(%rbp), %rax
	addq -208(%rbp), %rax
	movq %rax, -192(%rbp)
	movq $2, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $2, %r11
	movq -168(%rbp), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -216(%rbp)
	movq -192(%rbp), %rax
	addq -216(%rbp), %rax
	movq %rax, -192(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -224(%rbp)
	movq -224(%rbp), %rax
	addq -176(%rbp), %rax
	movq %rax, -232(%rbp)
	movq -232(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $3, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -232(%rbp), %r10
	movq $3, %r11
	movq image(, %r10, 8), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -240(%rbp)
	movq -192(%rbp), %rax
	addq -240(%rbp), %rax
	movq %rax, -192(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -248(%rbp)
	movq -248(%rbp), %rax
	addq -176(%rbp), %rax
	movq %rax, -256(%rbp)
	movq -256(%rbp), %rax
	addq $1, %rax
	movq %rax, -264(%rbp)
	movq -264(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $4, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -264(%rbp), %r10
	movq $4, %r11
	movq image(, %r10, 8), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -272(%rbp)
	movq -192(%rbp), %rax
	addq -272(%rbp), %rax
	movq %rax, -192(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -280(%rbp)
	movq -280(%rbp), %rax
	addq -176(%rbp), %rax
	movq %rax, -288(%rbp)
	movq -288(%rbp), %rax
	addq $2, %rax
	movq %rax, -296(%rbp)
	movq -296(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $5, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -296(%rbp), %r10
	movq $5, %r11
	movq image(, %r10, 8), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -304(%rbp)
	movq -192(%rbp), %rax
	addq -304(%rbp), %rax
	movq %rax, -192(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -312(%rbp)
	movq -312(%rbp), %rax
	addq -176(%rbp), %rax
	movq %rax, -320(%rbp)
	movq -320(%rbp), %rax
	addq $3, %rax
	movq %rax, -328(%rbp)
	movq -328(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq $6, %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $7, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -328(%rbp), %r10
	movq $6, %r11
	movq image(, %r10, 8), %rax
	imul -56(%rbp, %r11, 8), %rax
	movq %rax, -336(%rbp)
	movq -192(%rbp), %rax
	addq -336(%rbp), %rax
	movq %rax, -192(%rbp)
	movq -144(%rbp), %rax
	movq %rax, -120(%rbp)
	movq -168(%rbp), %rax
	movq %rax, -144(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -344(%rbp)
	movq -344(%rbp), %rax
	addq -176(%rbp), %rax
	movq %rax, -352(%rbp)
	movq -352(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -352(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -168(%rbp)
	movq -96(%rbp), %rax
	imul $768, %rax
	movq %rax, -360(%rbp)
	movq -360(%rbp), %rax
	addq -176(%rbp), %rax
	movq %rax, -368(%rbp)
	movq -192(%rbp), %rax
	movq -80(%rbp), %rsi
	cqto
	idivq %rsi
	movq -368(%rbp), %r10
	movq %rax, image(, %r10, 8)
	movq -368(%rbp), %rax
	movq $0, %r15
	cmpq %r15, %rax
	jl outOfBound
	movq $800000, %r15
	cmpq %r15, %rax
	jge outOfBound
	movq -368(%rbp), %r11
	movq image(, %r11, 8), %rax
	movq %rax, -376(%rbp)
	movq -176(%rbp), %rax
	addq $1, %rax
	movq %rax, -176(%rbp)
	jmp _155_r54_c19_Logical_body
	jmp _120_r49_c27_Assign_assign
_120_r49_c27_Assign_assign:
	movq -96(%rbp), %rax
	addq $1, %rax
	movq %rax, -96(%rbp)
	jmp _116_r49_c17_Logical_body
	jmp _63_r34_c6_Method_ed
_63_r34_c6_Method_ed:
	leave
	ret
.globl main
main:
	enter $-16, $0
	movq $0, -8(%rbp)
	movq $0, -16(%rbp)
_281_r71_c3_call_call:
	xor %rax, %rax
	call read
	addq $0, %rsp
_282_r72_c3_call_call:
	xor %rax, %rax
	call start_caliper
	addq $0, %rsp
_282_r72_c3_call_block:
	movq %rax, -8(%rbp)
_283_r73_c3_call_call:
	xor %rax, %rax
	call gaussian_blur
	addq $0, %rsp
_284_r74_c3_call_call:
	xor %rax, %rax
	call end_caliper
	addq $0, %rsp
_284_r74_c3_call_block:
	movq %rax, -16(%rbp)
_285_r75_c3_call_call:
	xor %rax, %rax
	call write
	addq $0, %rsp
_279_r70_c6_Method_ed:
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
	str_r16_c22:
		.string "segovia.pgm"
	str_r27_c23:
		.string "segovia_blur.pgm"
