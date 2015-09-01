package specializ;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class Dumper {
  public static byte[] dumpArrayList() {
    ClassWriter cw = new ClassWriter(0);
    FieldVisitor fv;
    MethodVisitor mv;

    cw.newUTF8("$[E");       // 1: L[java/lang/Object;
    cw.newUTF8("$(I)[E");    // 2: (I)L[java/lang/Object;
    cw.newUTF8("$E");        // 3: Ljava.lang.Object;
    cw.newUTF8("$(E)V");     // 4: (Ljava/lang/Object;)V
    cw.newUTF8("$([EI)[E");  // 5: (L[java/lang/Object;I)[Ljava/lang/Object;
    cw.newUTF8("$([EIE)V");  // 6: (L[java/lang/Object;ILjava/lang/Object;)V
    cw.newUTF8("$(I)E");     // 7: (I)Ljava/lang/Object;
    cw.newUTF8("$([EI)E");   // 8: (L[java/lang/Object;I)Ljava/lang/Object;
    cw.newUTF8("$(IE)V");    // 9: (ILjava/lang/Object;)V
    cw.newUTF8("$(E)Z");     //10: (Ljava/lang/Object;)Z
    cw.newUTF8("$(EE)Z");    //11: (Ljava/lang/Object;Ljava/lang/Object;)Z
    
    Handle BSM = new Handle(H_INVOKESTATIC, "specializ/java/util/SpecializMetaFactory", "bsm",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;");
    
    cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "specializ/java/util/ArrayList", "<E:Ljava/lang/Object;>Ljava/lang/Object;", "java/lang/Object", null);

    {
      fv = cw.visitField(ACC_PRIVATE, "data", "$[E", null, null);
      fv.visitEnd();
    }
    {
      fv = cw.visitField(ACC_PRIVATE, "size", "I", null, null);
      fv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitIntInsn(BIPUSH, 16);
      //mv.visitIntInsn(BIPUSH, 1);  // test resize()
      mv.visitInvokeDynamicInsn("newArrayInstance", "$(I)[E", BSM, "$E");
      mv.visitFieldInsn(PUTFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitInsn(RETURN);
      mv.visitMaxs(3, 1);
      mv.visitEnd();
    }    
    {
      mv = cw.visitMethod(ACC_PUBLIC, "size", "()I", null, null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "size", "I");
      mv.visitInsn(IRETURN);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "add", "$(E)V", "(TE;)V", null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitInsn(ARRAYLENGTH);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "size", "I");
      Label l0 = new Label();
      mv.visitJumpInsn(IF_ICMPNE, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "specializ/java/util/ArrayList", "resize", "()V", false);
      mv.visitLabel(l0);
      mv.visitFrame(F_SAME, 0, null, 0, null);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(DUP);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "size", "I");
      mv.visitInsn(DUP_X1);
      mv.visitInsn(ICONST_1);
      mv.visitInsn(IADD);
      mv.visitFieldInsn(PUTFIELD, "specializ/java/util/ArrayList", "size", "I");
      uload(mv, 1, "$E");
      mv.visitInvokeDynamicInsn("arraySet", "$([EIE)V", BSM, "$E");
      mv.visitInsn(RETURN);
      mv.visitMaxs(5, 3);   // locals: 1 + 2 (maybe a long)
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PRIVATE, "resize", "()V", null, null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "size", "I");
      mv.visitInsn(ICONST_1);
      mv.visitInsn(ISHL);
      mv.visitInvokeDynamicInsn("arrayCopyOf", "$([EI)[E", BSM, "$E");
      mv.visitFieldInsn(PUTFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitInsn(RETURN);
      mv.visitMaxs(4, 1);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "get", "$(I)E", "(I)TE;", null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitVarInsn(ILOAD, 1);
      mv.visitInvokeDynamicInsn("arrayGet", "$([EI)E", BSM, "$E");
      ureturn(mv, "$E");
      mv.visitMaxs(2, 2);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "set", "$(IE)V", "(ITE;)V", null);
      mv.visitCode();
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitVarInsn(ILOAD, 1);
      uload(mv, 2, "$E");
      mv.visitInvokeDynamicInsn("arraySet", "$([EIE)V", BSM, "$E");
      mv.visitInsn(RETURN);
      mv.visitMaxs(4, 4);   // stacks: 1 + 1 + 2 locals: 1 + 1 + 2 
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "contains", "$(E)Z", "(TE;)Z", null);
      mv.visitCode();
      mv.visitInsn(ICONST_0);
      mv.visitVarInsn(ISTORE, 3);
      Label l0 = new Label();
      mv.visitJumpInsn(GOTO, l0);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitFrame(F_APPEND,1, new Object[] { INTEGER }, 0, null);
      uload(mv, 1, "$E");
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "data", "$[E");
      mv.visitVarInsn(ILOAD, 3);
      mv.visitInvokeDynamicInsn("arrayGet", "$([EI)E", BSM, "$E");
      mv.visitInvokeDynamicInsn("equals", "$(EE)Z", BSM, "$E");
      Label l2 = new Label();
      mv.visitJumpInsn(IFEQ, l2);
      mv.visitInsn(ICONST_1);
      mv.visitInsn(IRETURN);
      mv.visitLabel(l2);
      mv.visitFrame(F_SAME, 0, null, 0, null);
      mv.visitIincInsn(3, 1);
      mv.visitLabel(l0);
      mv.visitFrame(F_SAME, 0, null, 0, null);
      mv.visitVarInsn(ILOAD, 3);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, "specializ/java/util/ArrayList", "size", "I");
      mv.visitJumpInsn(IF_ICMPLT, l1);
      mv.visitInsn(ICONST_0);
      mv.visitInsn(IRETURN);
      mv.visitMaxs(4, 4);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }
  
  private static void uload(MethodVisitor mv, int slot, String desc) {
    Label jumpI = new Label();
    Label jumpL = new Label();
    Label jumpF = new Label();
    Label jumpD = new Label();
    Label end = new Label();
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("I");
    mv.visitJumpInsn(IF_ACMPNE, jumpI);
    mv.visitVarInsn(ILOAD, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpI);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("J");
    mv.visitJumpInsn(IF_ACMPNE, jumpL);
    mv.visitVarInsn(LLOAD, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpL);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("F");
    mv.visitJumpInsn(IF_ACMPNE, jumpF);
    mv.visitVarInsn(FLOAD, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpF);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("D");
    mv.visitJumpInsn(IF_ACMPNE, jumpD);
    mv.visitVarInsn(DLOAD, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpD);
    
    mv.visitVarInsn(ALOAD, slot);
    mv.visitLabel(end);
  }
  
  private static void ustore(MethodVisitor mv, int slot, String desc) {
    Label jumpI = new Label();
    Label jumpL = new Label();
    Label jumpF = new Label();
    Label jumpD = new Label();
    Label end = new Label();
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("I");
    mv.visitJumpInsn(IF_ACMPNE, jumpI);
    mv.visitVarInsn(ISTORE, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpI);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("J");
    mv.visitJumpInsn(IF_ACMPNE, jumpL);
    mv.visitVarInsn(LSTORE, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpL);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("F");
    mv.visitJumpInsn(IF_ACMPNE, jumpF);
    mv.visitVarInsn(FSTORE, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpF);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("D");
    mv.visitJumpInsn(IF_ACMPNE, jumpD);
    mv.visitVarInsn(DSTORE, slot);
    mv.visitJumpInsn(GOTO, end);
    mv.visitLabel(jumpD);
    
    mv.visitVarInsn(ALOAD, slot);
    mv.visitLabel(end);
  }
  
  private static void ureturn(MethodVisitor mv, String desc) {
    Label jumpI = new Label();
    Label jumpL = new Label();
    Label jumpF = new Label();
    Label jumpD = new Label();
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("I");
    mv.visitJumpInsn(IF_ACMPNE, jumpI);
    mv.visitInsn(IRETURN);
    mv.visitLabel(jumpI);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("J");
    mv.visitJumpInsn(IF_ACMPNE, jumpL);
    mv.visitInsn(LRETURN);
    mv.visitLabel(jumpL);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("F");
    mv.visitJumpInsn(IF_ACMPNE, jumpF);
    mv.visitInsn(FRETURN);
    mv.visitLabel(jumpF);
    
    mv.visitLdcInsn(desc);
    mv.visitLdcInsn("D");
    mv.visitJumpInsn(IF_ACMPNE, jumpD);
    mv.visitInsn(DRETURN);
    mv.visitLabel(jumpD);
    
    mv.visitInsn(ARETURN);
  }
  
  private static final Handle INDY = new Handle(H_INVOKESTATIC, "specializ/java/util/SpecializMetaFactory", "indy",
      "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;");
  
  public static byte[] dumpTest() {
    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;

    cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "specializ/Test", null, "java/lang/Object", null);
    {
      mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      mv.visitInvokeDynamicInsn("new", "()Ljava/lang/Object;", INDY, "specializ/java/util/ArrayList", "L");
      mv.visitVarInsn(ASTORE, 1);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn("foo");
      mv.visitInvokeDynamicInsn("add", "(Ljava/lang/Object;Ljava/lang/Object;)V", INDY, "specializ/java/util/ArrayList", "L");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn("bar");
      mv.visitInvokeDynamicInsn("add", "(Ljava/lang/Object;Ljava/lang/Object;)V", INDY, "specializ/java/util/ArrayList", "L");
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitInsn(ICONST_0);
      mv.visitInvokeDynamicInsn("get", "(Ljava/lang/Object;I)Ljava/lang/Object;", INDY, "specializ/java/util/ArrayList", "L");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn("baz");
      mv.visitInvokeDynamicInsn("contains", "(Ljava/lang/Object;Ljava/lang/Object;)Z", INDY, "specializ/java/util/ArrayList", "L");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
      mv.visitInsn(RETURN);
      mv.visitMaxs(3, 2);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }
  
  public static byte[] dumpTest2() {
    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;

    cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "specializ/Test2", null, "java/lang/Object", null);
    {
      mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      mv.visitInvokeDynamicInsn("new", "()Ljava/lang/Object;", INDY, "specializ/java/util/ArrayList", "J");
      mv.visitVarInsn(ASTORE, 1);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn(1L);
      mv.visitInvokeDynamicInsn("add", "(Ljava/lang/Object;J)V", INDY, "specializ/java/util/ArrayList", "J");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn(2L);
      mv.visitInvokeDynamicInsn("add", "(Ljava/lang/Object;J)V", INDY, "specializ/java/util/ArrayList", "J");
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitInsn(ICONST_0);
      mv.visitInvokeDynamicInsn("get", "(Ljava/lang/Object;I)J", INDY, "specializ/java/util/ArrayList", "J");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn(3L);
      mv.visitInvokeDynamicInsn("contains", "(Ljava/lang/Object;J)Z", INDY, "specializ/java/util/ArrayList", "J");
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
      mv.visitInsn(RETURN);
      mv.visitMaxs(4, 2);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }
  
  public static byte[] dumpTestJIT() {
    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;

    cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "specializ/TestJIT", null, "java/lang/Object", null);
    {
      mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      mv.visitInvokeDynamicInsn("new", "()Ljava/lang/Object;", INDY, "specializ/java/util/ArrayList", "I"); 
      mv.visitVarInsn(ASTORE, 1);
      mv.visitInsn(ICONST_0);
      mv.visitVarInsn(ISTORE, 2);
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitFrame(F_APPEND, 2, new Object[] { "java/lang/Object", INTEGER }, 0, null);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitLdcInsn(100_000);
      Label l1 = new Label();
      mv.visitJumpInsn(IF_ICMPGE, l1);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitInvokeDynamicInsn("add", "(Ljava/lang/Object;I)V", INDY, "specializ/java/util/ArrayList", "I"); 
      mv.visitIincInsn(2, 1);
      mv.visitJumpInsn(GOTO, l0);
      mv.visitLabel(l1);
      mv.visitFrame(F_CHOP, 1, null, 0, null);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitInvokeDynamicInsn("size", "(Ljava/lang/Object;)I", INDY, "specializ/java/util/ArrayList", "I"); 
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
      mv.visitInsn(RETURN);
      mv.visitMaxs(2, 3);
      mv.visitEnd();
    }
    cw.visitEnd();

    return cw.toByteArray();
  }

  
  public static void main(String[] args) throws IOException {
    Files.write(Paths.get("classes/specializ/java/util/ArrayList.class"), dumpArrayList());
    Files.write(Paths.get("classes/specializ/Test.class"), dumpTest());
    Files.write(Paths.get("classes/specializ/Test2.class"), dumpTest2());
    Files.write(Paths.get("classes/specializ/TestJIT.class"), dumpTestJIT());
  }
}
