package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 26/01/2016.
 */
public class PropCondisTest {

    @Test(groups="1s")
    public void testCD1() throws ContradictionException {
        Solver s = new Solver();
        IntVar a = VF.enumerated("A", 0, 10, s);
        BoolVar b1 = ICF.arithm(a, "=", 9).reif();
        BoolVar b2 = ICF.arithm(a, "=", 10).reif();
        SatFactory.addConstructiveDisjunction(b1,b2);
        s.propagate();
        Assert.assertEquals(a.getDomainSize(), 2);
        Assert.assertEquals(a.getLB(), 9);
        Assert.assertEquals(a.getUB(), 10);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s")
    public void testCD2() throws ContradictionException {
        Solver s = new Solver();
        IntVar X = VF.enumerated("X", 0, 10, s);
        IntVar Y = VF.enumerated("Y", 0, 10, s);
        Constraint c1 = ICF.arithm(X, "-", Y, "<=", -9);
        Constraint c2 = ICF.arithm(Y, "-", X, "<=", -9);

        SatFactory.addConstructiveDisjunction(c1.reif(),c2.reif());
        s.propagate();
        Assert.assertEquals(X.getDomainSize(), 4);
        Assert.assertEquals(Y.getDomainSize(), 4);
        Assert.assertTrue(X.contains(0));
        Assert.assertTrue(X.contains(1));
        Assert.assertTrue(X.contains(9));
        Assert.assertTrue(X.contains(10));
        Assert.assertTrue(Y.contains(0));
        Assert.assertTrue(Y.contains(1));
        Assert.assertTrue(Y.contains(9));
        Assert.assertTrue(Y.contains(10));
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 6);
    }


    @Test(groups="1m")
    public void test3() {
        Random rnd = new Random();
        for (int n = 1; n < 20; n += 1) {
                System.out.printf("Size: %d\n", n);
                Solver or = modelPb(n, n, rnd, false);
                Solver cd = modelPb(n, n, rnd, true);
                or.set(ISF.lexico_LB((IntVar[]) or.getHook("decvars")));
                cd.set(ISF.lexico_LB((IntVar[]) cd.getHook("decvars")));
                or.findOptimalSolution(ResolutionPolicy.MINIMIZE);
                cd.findOptimalSolution(ResolutionPolicy.MINIMIZE);
                Assert.assertEquals(cd.getSolutionRecorder().getLastSolution().getIntVal((IntVar) cd.getObjectives()[0]),
                        or.getSolutionRecorder().getLastSolution().getIntVal((IntVar) or.getObjectives()[0]));
                Assert.assertEquals(cd.getMeasures().getSolutionCount(), or.getMeasures().getSolutionCount(), "wrong nb of solutions");
                Assert.assertTrue(or.getMeasures().getNodeCount() >= cd.getMeasures().getNodeCount(), "wrong nb of nodes");
        }
    }

    @Test(groups="30m")
    public void test4() {
        Random rnd = new Random();
        for (int n = 1; n < 5; n += 1) {
            for (int seed = 0; seed < 20; seed += 1) {
                System.out.printf("Size: %d (%d)\n", n, seed);
                Solver or = modelPb(n, seed, rnd, false);
                or.set(ISF.random((IntVar[]) or.getHook("decvars"), seed));
                or.findAllSolutions();
                Solver cd = modelPb(n, seed, rnd, true);
                cd.set(ISF.random((IntVar[]) cd.getHook("decvars"), seed));
                cd.findAllSolutions();
                Assert.assertEquals(cd.getMeasures().getSolutionCount(), or.getMeasures().getSolutionCount(), "wrong nb of solutions");
                Assert.assertTrue(or.getMeasures().getNodeCount() >= cd.getMeasures().getNodeCount(), "wrong nb of nodes");
            }
        }
    }


    private Solver modelPb(int size, long seed, Random rnd, boolean cd){
        rnd.setSeed(seed);
        int[] os = new int[size * 2];
        int[] ls = new int[size * 2];
        os[0] = rnd.nextInt(5);
        ls[0] = 3 + rnd.nextInt(7);
        for (int j = 1; j < os.length; j++) {
            os[j] = 1 + os[j - 1] + ls[j - 1] + rnd.nextInt(5);
            ls[j] = 3 + rnd.nextInt(4);
        }
        Solver solver = new Solver();
        IntVar[] OS = VF.enumeratedArray("O", size, 0, os[2*size-1] + ls[2*size-1], solver);
        IntVar[] LS = VF.enumeratedArray("L", size, 1, 10, solver);
        for (int i = 0; i < size - 1; i++) {
            solver.post(ICF.sum(new IntVar[]{OS[i], LS[i]}, "<", OS[i + 1]));
        }
        for (int i = 0; i < size; i++) {
            BoolVar[] disjunction = new BoolVar[os.length];
            for (int j = 0; j < os.length; j++) {
                disjunction[j] = LogicalConstraintFactory.and(
                        ICF.arithm(OS[i], ">", os[j]),
                        ICF.arithm(OS[i], "+", LS[i], "<", os[j] + ls[j])
                ).reif();
            }
            if(cd) {
                SatFactory.addConstructiveDisjunction(disjunction);
            }else {
                SatFactory.addBoolOrArrayEqualTrue(disjunction);
            }
        }
        IntVar horizon = VF.bounded("H", 0, os[2*size-1] + ls[2*size-1], solver);
        solver.post(ICF.sum(new IntVar[]{OS[size-1],LS[size-1]},horizon));
        solver.setObjectives(horizon);
        solver.addHook("decvars", ArrayUtils.append(OS, LS));
        Chatterbox.showShortStatistics(solver);
        return solver;
    }


}