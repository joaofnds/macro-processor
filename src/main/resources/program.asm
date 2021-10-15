Dados SEGMENT
Var1 DW 5
Var2 DW 8
Var3 DW 3
Dados ENDS

Codigo SEGMENT
ASSUME CS: Codigo
ASSUME DS: Dados

Inicio:
mov AX, Dados
mov DX, AX
mov AX,2
mov AX, DX
mul DX
mov  AX,Var1
push AX
mov  AX,Var2
mov  DX,AX
pop  AX
add  AX,DX
mov  Var1,AX

mov AX, Var1
add AX, DX
mov Var2, AX
mov  AX,Var1
push AX
mov  AX,3
mov  DX,AX
pop  AX
add  AX,DX
mov  Var1,AX

mov  AX,Var1
push AX
mov  AX,Var3
mov  DX,AX
pop  AX
add  AX,DX
mov  Var1,AX

CODIGO ENDS
END Inicio
