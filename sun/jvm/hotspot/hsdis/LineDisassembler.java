/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package sun.jvm.hotspot.hsdis;

import java.util.ArrayList;
import java.util.List;

public class LineDisassembler {
    private String architecture;
    private List<String> lines;
    private List<Integer> offsets;

    public LineDisassembler(long startPc, byte[] code, int length, boolean printPc) {
        Disassembler dis = new Disassembler(startPc, code, length);
        dis.decode(new Visitor(startPc, printPc));
    }

    public String getArchitecture() {
        return architecture;
    }

    public int size() {
        return lines.size();
    }

    public int getOffset(int i) {
        return offsets.get(i);
    }

    public String getLine(int i) {
        return lines.get(i);
    }

    class Visitor implements DisassemblyVisitor {
        private final boolean printPc;
        private final long startPc;
        private boolean is64bit;
        private StringBuilder buffer;
        private long currentPc;

        Visitor(long startPc, boolean printPc) {
            this.startPc = startPc;
            this.printPc = printPc;
        }

        public void prologue(boolean is64bitSetting) {
            is64bit = is64bitSetting;
            buffer = new StringBuilder();
            lines = new ArrayList<>();
            offsets = new ArrayList<>();
        }

        public String toAddress(long address) {
            if (is64bit) {
                return Long.toHexString(address);
            } else {
                return Integer.toHexString((int) address);
            }
        }

        public void beginInstruction(long newPc) {
            currentPc = newPc;
            if (printPc) {
                buffer.append(toAddress(currentPc));
                buffer.append(": ");
            }
        }

        public void print(String s) {
            buffer.append(s);
        }

        public void endInstruction(long endPc) {
            lines.add(buffer.toString());
            buffer.setLength(0);
            offsets.add((int) (currentPc - startPc));
        }

        public void reportArchitecture(String arch) {
            architecture = arch;
        }

        public void epilogue() {
            buffer = null;
        }

    }
}
