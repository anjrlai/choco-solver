/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.nary.among;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TIntHashSet;
import memory.IEnvironment;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;

import java.util.Arrays;

/**
 * Incremental propagator for Among Constraint:
 * Counts the number of decision variables which take a value in the input value set
 * GCCAT:
 * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
 * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 8/02/14
 */
public class PropAmongGAC_GoodImpl extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int nb_vars;        // number of decision variables (excludes the cardinality variable)
    private final int[] values;        // value set (array)
    private TIntHashSet setValues;    // value set (set)
    private ISet poss;                // variable set possibly assigned to a value in the value set
    private IStateInt nbSure;        // number of variables that are assigned to such value for sure

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Creates a propagator for Among:
     * Counts the number of decision variables which take a value in the input value set
     *
     * @param variables {decision variables, cardinality variable}
     * @param values    input value set
     */
    public PropAmongGAC_GoodImpl(IntVar[] variables, int[] values) {
        super(variables, PropagatorPriority.LINEAR, true);
        nb_vars = variables.length - 1;
        IEnvironment environment = solver.getEnvironment();
        this.setValues = new TIntHashSet(values);
        this.values = setValues.toArray();
        Arrays.sort(this.values);
        poss = SetFactory.makeStoredSet(SetType.SWAP_ARRAY, nb_vars, environment);
        nbSure = environment.makeInt(0);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int idx) {
        if (idx == nb_vars) {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        }
        return EventType.INT_ALL_MASK();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            poss.clear();
            int nbMandForSure = 0;
            for (int i = 0; i < nb_vars; i++) {
                IntVar var = vars[i];
                int nb = 0;
                for (int j : values) {
                    if (var.contains(j)) {
                        nb++;
                    }
                }
                if (nb == var.getDomainSize()) {
                    nbMandForSure++;
                } else if (nb > 0) {
                    poss.add(i);
                }
            }
            nbSure.set(nbMandForSure);
        }
        filter();
    }

    @Override
    public void propagate(int vidx, int evtmask) throws ContradictionException {
        if (vidx == nb_vars) {
            forcePropagate(EventType.CUSTOM_PROPAGATION);
        } else {
            if (poss.contain(vidx)) {
                IntVar var = vars[vidx];
                int nb = 0;
                for (int j : values) {
                    if (var.contains(j)) {
                        nb++;
                    }
                }
                if (nb == var.getDomainSize()) {
                    nbSure.add(1);
                    poss.remove(vidx);
                    vars[nb_vars].updateLowerBound(nbSure.get(), aCause);
                } else if (nb == 0) {
                    poss.remove(vidx);
                    vars[nb_vars].updateUpperBound(poss.getSize() + nbSure.get(), aCause);
                }
            }
            forcePropagate(EventType.CUSTOM_PROPAGATION);
        }
    }

    protected void filter() throws ContradictionException {
        int lb = nbSure.get();
        int ub = poss.getSize() + lb;
        vars[nb_vars].updateLowerBound(lb, aCause);
        vars[nb_vars].updateUpperBound(ub, aCause);
        if (vars[nb_vars].isInstantiated() && lb < ub) {
            if (vars[nb_vars].getValue() == lb) {
                backPropRemPoss();
            } else if (vars[nb_vars].getValue() == ub) {
                backPropForcePoss();
            }
        }
    }

    protected void backPropRemPoss() throws ContradictionException {
        for (int i = poss.getFirstElement(); i >= 0; i = poss.getNextElement()) {
            IntVar v = vars[i];
            if (v.hasEnumeratedDomain()) {
                for (int value : values) {
                    v.removeValue(value, this);
                }
                poss.remove(i);
            } else {
                int newLB = v.getLB();
                int newUB = v.getUB();
                for (int val = v.getLB(); val <= newUB; val = v.nextValue(val)) {
                    if (setValues.contains(val)) {
                        newLB = val + 1;
                    } else {
                        break;
                    }
                }
                for (int val = newUB; val >= newLB; val = v.previousValue(val)) {
                    if (setValues.contains(val)) {
                        newUB = val - 1;
                    } else {
                        break;
                    }
                }
                v.updateLowerBound(newLB, this);
                v.updateUpperBound(newUB, this);
                if (newLB > values[values.length - 1] || newUB < values[0]) {
                    poss.remove(i);
                }
            }
        }
    }

    protected void backPropForcePoss() throws ContradictionException {
        for (int i = poss.getFirstElement(); i >= 0; i = poss.getNextElement()) {
            IntVar v = vars[i];
            if (v.hasEnumeratedDomain()) {
                for (int val = v.getLB(); val <= v.getUB(); val = v.nextValue(val)) {
                    if (!setValues.contains(val)) {
                        v.removeValue(val, this);
                    }
                }
                poss.remove(i);
                nbSure.add(1);
            } else {
                v.updateLowerBound(values[0], this);
                v.updateUpperBound(values[values.length - 1], this);
                int newLB = v.getLB();
                int newUB = v.getUB();
                for (int val = v.getLB(); val <= newUB; val = v.nextValue(val)) {
                    if (!setValues.contains(val)) {
                        newLB = val + 1;
                    } else {
                        break;
                    }
                }
                for (int val = newUB; val >= newLB; val = v.previousValue(val)) {
                    if (!setValues.contains(val)) {
                        newUB = val - 1;
                    } else {
                        break;
                    }
                }
                v.updateLowerBound(newLB, this);
                v.updateUpperBound(newUB, this);
                if (v.isInstantiated()) {
                    poss.remove(i);
                    nbSure.add(1);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int min = 0;
        int max = 0;
        int nbInst = vars[nb_vars].isInstantiated() ? 1 : 0;
        for (int i = 0; i < nb_vars; i++) {
            IntVar var = vars[i];
            if (var.isInstantiated()) {
                nbInst++;
                if (setValues.contains(var.getValue())) {
                    min++;
                    max++;
                }
            } else {
                int nb = 0;
                for (int j : values) {
                    if (var.contains(j)) {
                        nb++;
                    }
                }
                if (nb == var.getDomainSize()) {
                    min++;
                    max++;
                } else if (nb > 0) {
                    max++;
                }
            }
        }
        if (min > vars[nb_vars].getUB() || max < vars[nb_vars].getLB()) {
            return ESat.FALSE;
        }
        if (nbInst == nb_vars + 1) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AMONG(");
        sb.append("[");
        for (int i = 0; i < nb_vars; i++) {
            if (i > 0) sb.append(",");
            sb.append(vars[i].toString());
        }
        sb.append("],{");
        sb.append(Arrays.toString(values));
        sb.append("},");
        sb.append(vars[nb_vars].toString()).append(")");
        return sb.toString();
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropAmongGAC_GoodImpl(aVars, this.values));
        }
    }
}
