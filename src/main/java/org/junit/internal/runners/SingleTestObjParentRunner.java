package org.junit.internal.runners;

import java.util.List;

import net.jonbell.crij.runtime.CRIJInstrumented;
import org.junit.After;
import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

public abstract class SingleTestObjParentRunner<T> extends ParentRunner<T>  {
    protected SingleTestObjParentRunner(Class<?> testClass) throws InitializationError{
        super(testClass);
    }
    
    @Override
    public void run(final RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier,
                getDescription());
        try {
            Statement statement = classBlock0(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.addFailedAssumption(e);
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        }
    }

    protected final Statement classBlock0(final RunNotifier notifier) {
        try {
//            System.out.println("Class block created");
            Statement statement = classBlock(notifier);
            if (!areAllChildrenIgnored()) {
                statement = withBefores(statement, createTest0());
                statement = withBeforeClasses(statement);
                statement = withAfters(statement, createTest0());
                statement = withAfterClasses(statement);
                statement = withClassRules(statement);
            }
            return statement;
        } catch (Throwable e) {
            return new Fail(e);
        }

    }


    private boolean areAllChildrenIgnored() {
        for (T child : getFilteredChildren()) {
            if (!isIgnored(child)) {
                return false;
            }
        }
        return true;
    }



    protected Object lastTestObj;
    protected TestClass lastTestClz;

    /**
     * Returns a new fixture for running a test. Default implementation executes
     * the test class's no-argument constructor (validation should have ensured
     * one exists).
     */
    protected final Object createTest0() throws Exception {
        
        if (getTestClass() != lastTestClz) {
            this.lastTestClz = getTestClass();
            this.lastTestObj = createTest();
            System.out.println("##Generating new test object for class: " + this.lastTestClz.getName());
        }
        return this.lastTestObj;
    }
    
    protected abstract Object createTest() throws Exception;
    

    protected Statement withBefores(Statement statement, Object target) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(
                Before.class);
        return befores.isEmpty() ? statement : new RunBefores(statement,
                befores, target);
    }

    protected Statement withAfters(Statement statement, Object target) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(
                After.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters,
                target);
    }
}
