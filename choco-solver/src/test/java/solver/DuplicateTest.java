/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.binary.PropScale;
import solver.constraints.extension.TuplesFactory;
import solver.constraints.nary.circuit.CircuitConf;
import solver.constraints.ternary.PropTimesNaive;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;
import util.objects.setDataStructures.SetType;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 29/08/2014
 */
public class DuplicateTest {

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver("ocohc");
        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver("Choco");
        VF.fixed(-2, solver);
        VF.fixed(0, solver);
        VF.fixed("my cste 3", 3, solver);

        VF.bool("bool", solver);
        VF.bounded("bounded", 2, 4, solver);
        VF.enumerated("enum1", 1, 3, solver);
        VF.enumerated("enum2", new int[]{3, 4, 5}, solver);

        VF.set("set1", 2, 4, solver);
        VF.set("set2", new int[]{1, 2}, solver);
        VF.set("set3", new int[]{1, 2, 3, 4}, new int[]{2, 3}, solver);
        VF.set("set4", new int[]{3, 4, 5, 6}, SetType.BITSET, new int[]{5, 6}, SetType.BOOL_ARRAY, solver);

        VF.real("real", 1.1, 2.2, .001, solver);
        VF.real(VF.bounded("bounded", 2, 4, solver), 0.01);

        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver("Choco");
        BoolVar b = VF.bool("b", solver);
        VF.not(b);
        b.not();
        VF.eq(b);
        IntVar e = VF.enumerated("e", 1, 3, solver);
        VF.offset(e, -2);
        VF.scale(e, 3);
        VF.minus(e);
        VF.eq(e);
        VF.task(e, solver.ONE, VF.offset(e, 1));

        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver("Choco");
        solver.post(ICF.TRUE(solver));
        solver.post(ICF.FALSE(solver));
        Solver copy = solver.duplicateModel();
        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
    }

    @Test(groups = "1s")
    public void test5() {
        for (String op : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            solver.post(ICF.arithm(v, op, 3));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();
            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        ICF.arithm(v, "=", 3).reif();

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test7() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);

        solver.post(ICF.member(v, 2, 3));
        solver.post(ICF.member(v, new int[]{2}));
        solver.post(ICF.not_member(v, 0, 1));
        solver.post(ICF.not_member(v, new int[]{7}));


        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test8() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", -6, 4, solver);
        solver.post(ICF.absolute(v, w));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test9() {
        for (String op : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", 1, 4, solver);
            solver.post(ICF.arithm(v, op, w));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();
            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test10() {
        for (String op1 : new String[]{"+", "-"}) {
            for (String op2 : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
                Solver solver = new Solver("Choco");
                IntVar v = VF.enumerated("v", 1, 4, solver);
                IntVar w = VF.enumerated("v", 1, 4, solver);
                solver.post(ICF.arithm(v, op1, w, op2, 1));

                Solver copy = solver.duplicateModel();

                solver.findAllSolutions();
                copy.findAllSolutions();
                Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
                Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
                Assert.assertEquals(copy.toString(), solver.toString());
                Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            }
        }
    }

    @Test(groups = "1s")
    public void test11() {
        for (String op1 : new String[]{"+", "-"}) {
            for (String op2 : new String[]{"=", "!=", ">", "<", ">=", "<="}) {
                Solver solver = new Solver("Choco");
                IntVar v = VF.enumerated("v", 1, 4, solver);
                IntVar w = VF.enumerated("v", 1, 4, solver);
                solver.post(ICF.arithm(v, op2, w, op1, 1));

                Solver copy = solver.duplicateModel();

                solver.findAllSolutions();
                copy.findAllSolutions();
                Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
                Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
                Assert.assertEquals(copy.toString(), solver.toString());
                Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
            }
        }
    }

    @Test(groups = "1s")
    public void test12() {
        for (String op : new String[]{"=", "!=", ">", "<"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            solver.post(ICF.distance(v, w, op, 1));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test13() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.element(v, new int[]{4, 3, 2, 1}, w));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test14() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 16, solver);
        solver.post(ICF.square(w, v));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test15() {
        for (String op : new String[]{"AC3", "AC3rm", "AC3bit+rm", "AC2001", "FC"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            solver.post(ICF.table(v, w, TuplesFactory.allEquals(v, w), op));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test16() {
        for (String op : new String[]{"=", ">", "<"}) {
            Solver solver = new Solver("Choco");
            IntVar v = VF.enumerated("v", 1, 4, solver);
            IntVar w = VF.enumerated("v", -6, 4, solver);
            IntVar x = VF.enumerated("v", 2, 4, solver);
            solver.post(ICF.distance(v, w, op, x));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test17() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.eucl_div(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test18() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.maximum(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test19() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.minimum(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test20() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(ICF.mod(v, w, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test21() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar w = VF.enumerated("v", 1, 2, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(new Constraint("times", new PropTimesNaive(v, w, x)));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test22() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(new Constraint("times", new PropScale(v, 3, x)));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test23() {
        Solver solver = new Solver("Choco");
        IntVar v = VF.enumerated("v", 1, 4, solver);
        IntVar x = VF.enumerated("v", 0, 3, solver);
        solver.post(new Constraint("times", new PropScale(v, 3, x)));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test24() {
        for (String CS : new String[]{"BC", "AC", "FC", "DEFAULT"}) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            solver.post(ICF.alldifferent(vs, CS));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test25() {
        Solver solver = new Solver("Choco");
        IntVar x = VF.enumerated("x", 1, 4, solver);
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        solver.post(ICF.among(x, vs, new int[]{1, 2}));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test26() {
        for (boolean ac : new boolean[]{true, false}) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            IntVar x = VF.enumerated("x", 1, 4, solver);
            solver.post(ICF.atleast_nvalues(vs, x, ac));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test27() {
        for (boolean ac : new boolean[]{false, true}) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            IntVar x = VF.enumerated("x", 1, 4, solver);
            solver.post(ICF.atmost_nvalues(vs, x, ac));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test28() {
        Solver solver = new Solver("Choco");
        IntVar x = VF.enumerated("x", 1, 4, solver);
        BoolVar[] bs = VF.boolArray("bs", 4, solver);
        solver.post(ICF.boolean_channeling(bs, x, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test29() {
        for (CircuitConf cf : CircuitConf.values()) {
            Solver solver = new Solver("Choco");
            IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
            solver.post(ICF.circuit(vs, 1, cf));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test30() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        IntVar x = VF.enumerated("x", 1, 4, solver);
        solver.post(ICF.cost_regular(vs, x, null));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test31() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        IntVar x = VF.enumerated("x", 1, 4, solver);
        solver.post(ICF.count(2, vs, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test32() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        IntVar x = VF.bounded("x", 1, 4, solver);
        IntVar z = VF.enumerated("x", 2, 3, solver);
        solver.post(ICF.count(z, vs, x));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test33() {
        Solver solver = new Solver("Choco");
        // todo
        solver.post(ICF.cumulative(null, null, null, true));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test34() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.enumeratedArray("vs", 2, 1, 4, solver);
        IntVar[] Y = VF.enumeratedArray("vs", 2, 2, 4, solver);

        IntVar[] dX = VF.enumeratedArray("vs", 2, 1, 3, solver);
        IntVar[] dY = VF.enumeratedArray("vs", 2, 3, 4, solver);

        solver.post(ICF.diffn(X, Y, dX, dY, false));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test35() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.enumeratedArray("vs", 3, 1, 4, solver);
        IntVar V = VF.enumerated("V", 2, 4, solver);
        IntVar I = VF.enumerated("I", 0, 1, solver);


        solver.post(ICF.element(V, X, I, 0));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test36() {
        for (boolean cl : new boolean[]{true, false}) {
            Solver solver = new Solver("Choco");
            IntVar[] X = VF.enumeratedArray("vs", 3, 1, 4, solver);
            IntVar[] Y = VF.enumeratedArray("vs", 2, 1, 2, solver);

            solver.post(ICF.global_cardinality(X, new int[]{2, 3}, Y, cl));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test37() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.enumeratedArray("vs", 3, 1, 4, solver);
        IntVar[] Y = VF.enumeratedArray("xs", 3, 1, 4, solver);

        solver.post(ICF.inverse_channeling(X, Y, 1, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test38() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar[] Y = VF.boundedArray("xs", 3, 1, 4, solver);

        solver.post(ICF.inverse_channeling(X, Y, 1, 1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }


    @Test(groups = "1s")
    public void test39() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar C = VF.enumerated("C", 0, 10, solver);
        IntVar E = VF.bounded("E", 0, 15, solver);
        solver.post(ICF.knapsack(X, C, E, new int[]{2, 3, 4, 1}, new int[]{5, 2, 3, 4}));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test40() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 3, 3, 1, 4, solver);
        solver.post(ICF.lex_chain_less(X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test41() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 3, 3, 1, 4, solver);
        solver.post(ICF.lex_chain_less_eq(X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test42() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 2, 3, 1, 4, solver);
        solver.post(ICF.lex_less(X[0], X[1]));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test43() {
        Solver solver = new Solver("Choco");
        IntVar[][] X = VF.boundedMatrix("vs", 2, 3, 1, 4, solver);
        solver.post(ICF.lex_less_eq(X[0], X[1]));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test44() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar M = VF.bounded("M", 0, 5, solver);
        solver.post(ICF.maximum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test45() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar M = VF.bounded("M", 0, 5, solver);
        solver.post(ICF.minimum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test46() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        BoolVar M = VF.bool("M", solver);
        solver.post(ICF.maximum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test47() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        BoolVar M = VF.bool("M", solver);
        solver.post(ICF.minimum(M, X));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test48() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        IntVar[] ws = VF.enumeratedArray("ws", 4, 1, 4, solver);
        solver.post(ICF.multicost_regular(vs, ws, null));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test49() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        BoolVar M = VF.bool("M", solver);
        solver.post(ICF.nvalues(X, M));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test50() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 5, solver);
        IntVar E = VF.bounded("E", 0, 5, solver);
        solver.post(ICF.path(X, S, E, 0));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test51() {
        Solver solver = new Solver("Choco");
        IntVar[] vs = VF.enumeratedArray("vs", 4, 1, 4, solver);
        solver.post(ICF.regular(vs, null));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test52() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 5, solver);
        solver.post(ICF.scalar(X, new int[]{1, 2, 3}, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test53() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar[] Y = VF.boundedArray("ws", 3, 1, 4, solver);
        solver.post(ICF.sort(X, Y));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test54() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 2, solver);
        solver.post(ICF.subcircuit(X, -1, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test55() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 5, solver);
        solver.post(ICF.sum(X, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test56() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 3, solver);
        IntVar S = VF.bounded("S", 1, 2, solver);
        solver.post(ICF.sum(X, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test57() {
        Solver solver = new Solver("Choco");
        BoolVar[] X = VF.boolArray("vs", 12, solver);
        IntVar S = VF.bounded("S", 1, 2, solver);
        solver.post(ICF.sum(X, S));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test58() {
        for (String op : new String[]{"GAC3rm", "GAC2001", "GACSTR", "GAC2001+", "GAC3rm+", "FC", "STR2+"}) {
            Solver solver = new Solver("Choco");
            IntVar[] v = VF.enumeratedArray("v", 3, 1, 4, solver);
            solver.post(ICF.table(v, TuplesFactory.allEquals(v), op));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "1s")
    public void test59() {
        Solver solver = new Solver("Choco");
        IntVar[] X = VF.boundedArray("vs", 3, 1, 4, solver);
        IntVar S = VF.bounded("S", 0, 3, solver);
        solver.post(ICF.tree(X, S, -1));

        Solver copy = solver.duplicateModel();

        solver.findAllSolutions();
        copy.findAllSolutions();

        Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
        Assert.assertEquals(copy.toString(), solver.toString());
        Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
    }

    @Test(groups = "1s")
    public void test60() {
        for (boolean strong : new boolean[]{true, false}) {
            Solver solver = new Solver("Choco");
            IntVar[] X = VF.boundedArray("vs", 5, 1, 5, solver);
            IntVar S = VF.bounded("S", 0, 3, solver);
            solver.post(ICF.tsp(X, S, new int[][]{{0,1,2,3,4},{1,0,1,2,3},{2,1,0,1,2},{3,2,1,0,1},{4,3,2,1,0}}, strong));

            Solver copy = solver.duplicateModel();

            solver.findAllSolutions();
            copy.findAllSolutions();

            Assert.assertEquals(copy.getNbVars(), solver.getNbVars());
            Assert.assertEquals(copy.getNbCstrs(), solver.getNbCstrs());
            Assert.assertEquals(copy.toString(), solver.toString());
            Assert.assertEquals(copy.getMeasures().getSolutionCount(), solver.getMeasures().getSolutionCount());
        }
    }
}
