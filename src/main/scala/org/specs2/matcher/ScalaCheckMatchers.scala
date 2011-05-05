package org.specs2
package matcher

import org.scalacheck.{ Prop, Arg, Test, Arbitrary, Shrink }
import org.scalacheck.util.StdRand
import org.scalacheck.Prop._
import org.scalacheck.Test.{ Params, Proved, Passed, Failed, Exhausted, GenException, PropException, Result }
import org.scalacheck.Pretty._
import org.scalacheck.Pretty
import scala.collection.Map
import io.ConsoleOutput


/**
 * The ScalaCheckMatchers trait provides matchers which allow to
 * assess properties multiple times with generated data.
 * @see the <a href="http://code.google.com/p/scalacheck/">ScalaCheck project</a>
 */
trait ScalaCheckMatchers extends ConsoleOutput with ScalaCheckFunctions with ScalaCheckParameters
   with FunctionPropertyImplicits
   with ResultPropertyImplicits
   with ApplicableArbitraries { outer =>

  /**
   * transform a Function returning a MatchResult (or anything which can be converted to a Prop) as a ScalaCheck property
   */
  implicit def check[T, R](result: T => R)(implicit toProp: R => Prop, a: Arbitrary[T], s: Shrink[T]): Prop = Prop.forAll((t: T) => result(t))

  implicit def check[T1, T2, R](result: (T1, T2) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1], a2: Arbitrary[T2], s2: Shrink[T2]): Prop =
    Prop.forAll((t1: T1, t2: T2) => result(t1, t2))

  implicit def check[T1, T2, T3, R](result: (T1, T2, T3) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1], a2: Arbitrary[T2], s2: Shrink[T2], a3: Arbitrary[T3], s3: Shrink[T3]): Prop =
    Prop.forAll((t1: T1, t2: T2, t3: T3) => result(t1, t2, t3))

  implicit def check[T1, T2, T3, T4, R](result: (T1, T2, T3, T4) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1], a2: Arbitrary[T2], s2: Shrink[T2], a3: Arbitrary[T3], s3: Shrink[T3], a4: Arbitrary[T4], s4: Shrink[T4]): Prop =
    Prop.forAll((t1: T1, t2: T2, t3: T3, t4: T4) => result(t1, t2, t3, t4))

  implicit def check[T1, T2, T3, T4, T5, R](result: (T1, T2, T3, T4, T5) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1], a2: Arbitrary[T2], s2: Shrink[T2], a3: Arbitrary[T3], s3: Shrink[T3], a4: Arbitrary[T4], s4: Shrink[T4], a5: Arbitrary[T5], s5: Shrink[T5]): Prop =
    Prop.forAll((t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) => result(t1, t2, t3, t4, t5))

  implicit def check[T1, T2, T3, T4, T5, T6, R](result: (T1, T2, T3, T4, T5, T6) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1],a2: Arbitrary[T2], s2: Shrink[T2], a3: Arbitrary[T3], s3: Shrink[T3], a4: Arbitrary[T4], s4: Shrink[T4], a5: Arbitrary[T5], s5: Shrink[T5], a6: Arbitrary[T6], s6: Shrink[T6]): Prop =
    Prop.forAll((t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) => result(t1, t2, t3, t4, t5, t6))

  implicit def check[T1, T2, T3, T4, T5, T6, T7, R](result: (T1, T2, T3, T4, T5, T6, T7) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1], a2: Arbitrary[T2], s2: Shrink[T2],a3: Arbitrary[T3], s3: Shrink[T3], a4: Arbitrary[T4], s4: Shrink[T4], a5: Arbitrary[T5], s5: Shrink[T5], a6: Arbitrary[T6], s6: Shrink[T6], a7: Arbitrary[T7], s7: Shrink[T7]): Prop =
    Prop.forAll((t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7) => result(t1, t2, t3, t4, t5, t6, t7))

  implicit def check[T1, T2, T3, T4, T5, T6, T7, T8, R](result: (T1, T2, T3, T4, T5, T6, T7, T8) => R)(implicit toProp: R => Prop, a1: Arbitrary[T1], s1: Shrink[T1], a2: Arbitrary[T2], s2: Shrink[T2], a3: Arbitrary[T3], s3: Shrink[T3], a4: Arbitrary[T4], s4: Shrink[T4], a5: Arbitrary[T5], s5: Shrink[T5], a6: Arbitrary[T6], s6: Shrink[T6], a7: Arbitrary[T7], s7: Shrink[T7], a8: Arbitrary[T8], s8: Shrink[T8]): Prop =
    Prop.forAll((t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6, t7: T7, t8: T8) => result(t1, t2, t3, t4, t5, t6, t7, t8))
  
  /** execute a PartialFunction as a ScalaCheck property */
  implicit def check[T, S](f: PartialFunction[T, S])(implicit toProp: S => Prop, a: Arbitrary[T], s: Shrink[T]): Prop =
    PartialFunctionPropertyImplicits.partialFunctionToProp(f).forAll

  /** execute a ScalaCheck property */
  implicit def check(prop: Prop)(implicit p: Parameters): execute.Result = checkProperty(prop)(p)

  /** set specific execution parameters on a Property */
  implicit def setProperty(p: Prop) = new SetProperty(p)
  class SetProperty(prop: Prop) {
    def set(p: (Symbol, Int)*) = check(prop)(outer.set(p:_*))
    def display(p: (Symbol, Int)*) = check(prop)(outer.display(p:_*))
  }
  /**
   * checks if the property is true for each generated value, and with the specified
   * generation parameters <code>p</code>. <code>p</code> is transformed into a scalacheck parameters
   * and indicates if the generation should be verbose or not
   */
  private[specs2] def checkProperty(prop: Prop)(implicit p: Parameters): execute.Result = {
    checkScalaCheckProperty(prop)(Params(p(minTestsOk), p(maxDiscarded), p(minSize), p(maxSize), StdRand, p(workers)), p.verbose)
  }

  /**
   * checks if the property is true for each generated value, and with the specified
   * scalacheck parameters. If verbose is true, then print the results on the console
   */
  private [matcher] def checkScalaCheckProperty(prop: =>Prop)(params: Params, verbose: Boolean): execute.Result = {
    // will print the result of each test if verbose = true
    val callback = new Test.TestCallback {
      override def onPropEval(name: String, threadXdx: Int, succeeded: Int, discarded: Int): Unit = {
        if (verbose) {
          if (discarded == 0)
            printf("\rPassed %d tests", succeeded)
          else
            printf("\rPassed %d tests; %d discarded", succeeded, discarded)
          flush
        }
      }
    }

    // check the property with ScalaCheck
    val results = checkProp(params, prop, callback)

    // display the final result if verbose = true
    if (verbose) {
      val s = prettyTestRes(results)(defaultPrettyParams)
      printf("\r%s %s%s\n", if (results.passed) "+" else "!", s, List.fill(70 - s.length)(" ").mkString(""))
    }
    results match {
      case Result(Proved(as), succeeded, discarded, _, _) => execute.Success(noCounterExample(succeeded), succeeded)
      case Result(Passed, succeeded, discarded, _, _)     => execute.Success(noCounterExample(succeeded), succeeded)
      case r @ Result(GenException(e), n, _, _, _)        => execute.Failure(prettyTestRes(r)(defaultPrettyParams), "", e.getStackTrace().toList)
      case r @ Result(Exhausted, n, _, _, _)              => execute.Failure(prettyTestRes(r)(defaultPrettyParams))
      case Result(Failed(args, labels), n, _, _, _)       =>
        execute.Failure("A counter-example is "+counterExample(args)+" (" + afterNTries(n) + afterNShrinks(args) + ")" + failedLabels(labels))
      case Result(PropException(args, ex, labels), n, _, _, _) =>
        ex match {
          case execute.FailureException(f) =>
            execute.Failure("A counter-example is "+counterExample(args)+" (" + afterNTries(n) + afterNShrinks(args) + ")" + failedLabels(labels+f.message))
          case e: java.lang.Exception         =>
            execute.Error("A counter-example is "+counterExample(args)+": " + ex + " ("+afterNTries(n)+")"+ failedLabels(labels), e)
          case throwable    => throw ex
        }

    }
  }
  // depending on the result, return the appropriate success status and messages
  // the failure message indicates a counter-example to the property
  private[matcher] def noCounterExample(n: Int) = "The property passed without any counter-example " + afterNTries(n)
  private[matcher] def afterNTries(n: Int) = "after " + (if (n <= 1) n + " try" else n + " tries")
  private[matcher] def afterNShrinks(args: List[Arg[_]]) = {
    if (args.forall(_.shrinks == 0))  ""
    else
      args.map { arg =>
        if (arg.origArg != arg.arg) "'"+arg.origArg +"' -> '"+arg.arg+"'"
        else " = "
     }.mkString(" - shrinked (", ",", ")")
  }

  private [matcher] def counterExample(args: List[Arg[_]]) = {
    if (args.size == 1)
      args.map(a => if (a.arg == null) "null" else a.arg.toString).mkString("'", "", "'")
    else if (args.exists(_.arg.toString.isEmpty))
      args.map(_.arg).mkString("['", "', '", "']")
    else
      args.map(_.arg).mkString("[", ", ", "]")
  }
  private [matcher] def failedLabels(labels: Set[String]) = {
    if (labels.isEmpty)  ""  
    else labels.mkString("\n", ", ", "\n")
  }
}
object ScalaCheckMatchers extends ScalaCheckMatchers
/**
 * This trait adds some syntactic sugar to transform function
 * to properties by appending forAll
 */
trait FunctionPropertyImplicits {
  /** transform a function returning a boolean to a property by appending forAll */
  implicit def functionToProp[T](f: T => Boolean)(implicit a: Arbitrary[T], s: Shrink[T]): Prop = functionToForAll(f).forAll
  implicit def functionToForAll[T](f: T => Boolean)(implicit a: Arbitrary[T], s: Shrink[T]): FunctionForAll[T] = new FunctionForAll(f)(a, s)
  class FunctionForAll[T](f: T => Boolean)(implicit a: Arbitrary[T], s: Shrink[T]) {
    def forAll: Prop = Prop.forAll(f)
  }
  /** transform a function returning a boolean to a property by appending forAll */
  implicit def functionToProp2[T1, T2](f: (T1, T2) => Boolean): FunctionForAll2[T1, T2] = new FunctionForAll2(f)
  class FunctionForAll2[T1, T2](f: (T1, T2) => Boolean) {
    def forAll(implicit
      a1: Arbitrary[T1], s1: Shrink[T1],
      a2: Arbitrary[T2], s2: Shrink[T2]
      ): Prop = Prop.forAll(f)
  }
  /** transform a function returning a boolean to a property by appending forAll */
  implicit def functionToProp3[T1, T2, T3](f: (T1, T2, T3) => Boolean): FunctionForAll3[T1, T2, T3] = new FunctionForAll3(f)
  class FunctionForAll3[T1, T2, T3](f: (T1, T2, T3) => Boolean) {
    def forAll(implicit
      a1: Arbitrary[T1], s1: Shrink[T1],
      a2: Arbitrary[T2], s2: Shrink[T2],
      a3: Arbitrary[T3], s3: Shrink[T3]
      ): Prop = Prop.forAll(f)
  }
}
/**
 * This trait adds some syntactic sugar to transform partial functions to properties by appending forAll
 */
trait PartialFunctionPropertyImplicits {
  /** transform a partial function returning a boolean to a property by appending forAll */
  implicit def partialFunctionToProp[T, S](f: PartialFunction[T, S]): PartialFunctionForAll[T, S] = new PartialFunctionForAll(f)
  class PartialFunctionForAll[T, S](f: PartialFunction[T, S]) {
    def forAll(implicit toProp: S => Prop, a: Arbitrary[T], s: Shrink[T]): Prop = Prop.forAll(f)
  }
}
object PartialFunctionPropertyImplicits extends PartialFunctionPropertyImplicits
trait ResultPropertyImplicits {

  implicit def matchResultToProp(m: MatchResult[_]): Prop = resultProp(m.toResult)
  implicit def resultToProp[T](t: T)(implicit toResult: T => execute.Result): Prop = resultProp(toResult(t))
  private def resultProp(r: execute.Result): Prop = {
    r match {
      case execute.Failure(ko, _, _, _) => false :| ko
      case execute.Error(ko, _)         => false :| ko
	    case _                            => true  :| r.message
	  }
  }
}

/**
 * This trait enables some syntactic sugar when it is necessary to pass several arbitrary instances
 */
trait ApplicableArbitraries { this: ScalaCheckMatchers =>

  implicit def applicableArbitrary[T](a: Arbitrary[T]): ApplicableArbitrary[T] = ApplicableArbitrary(a)
  case class ApplicableArbitrary[T](a: Arbitrary[T]) {
    def apply[R](f: T => R)(implicit toProp: R => Prop, s: Shrink[T]) = check(f)(toProp, a, s)
  }
  implicit def applicableArbitrary2[T1, T2](a: (Arbitrary[T1], Arbitrary[T2])) = ApplicableArbitrary2(a._1, a._2)
  case class ApplicableArbitrary2[T1, T2](a1: Arbitrary[T1], a2: Arbitrary[T2]) {
    def apply[R](f: (T1, T2) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2]) = check(f)(toProp, a1, s1, a2, s2)
  }
  implicit def applicableArbitrary3[T1, T2, T3](a: (Arbitrary[T1], Arbitrary[T2], Arbitrary[T3])) = ApplicableArbitrary3(a._1, a._2, a._3)
  case class ApplicableArbitrary3[T1, T2, T3](a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3]) {
    def apply[R](f: (T1, T2, T3) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2], s3: Shrink[T3]) = check(f)(toProp, a1, s1, a2, s2, a3, s3)
  }
  implicit def applicableArbitrary4[T1, T2, T3, T4](a: (Arbitrary[T1], Arbitrary[T2], Arbitrary[T3], Arbitrary[T4])) = ApplicableArbitrary4(a._1, a._2, a._3, a._4)
  case class ApplicableArbitrary4[T1, T2, T3, T4](a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4]) {
    def apply[R](f: (T1, T2, T3, T4) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2], s3: Shrink[T3], s4: Shrink[T4]) = check(f)(toProp, a1, s1, a2, s2, a3, s3, a4, s4)
  }
  implicit def applicableArbitrary5[T1, T2, T3, T4, T5](a: (Arbitrary[T1], Arbitrary[T2], Arbitrary[T3], Arbitrary[T4], Arbitrary[T5])) = ApplicableArbitrary5(a._1, a._2, a._3, a._4, a._5)
  case class ApplicableArbitrary5[T1, T2, T3, T4, T5](a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4], a5: Arbitrary[T5]) {
    def apply[R](f: (T1, T2, T3, T4, T5) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2], s3: Shrink[T3], s4: Shrink[T4], s5: Shrink[T5]) = check(f)(toProp, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5)
  }
  implicit def applicableArbitrary6[T1, T2, T3, T4, T5, T6](a: (Arbitrary[T1], Arbitrary[T2], Arbitrary[T3], Arbitrary[T4], Arbitrary[T5], Arbitrary[T6])) = ApplicableArbitrary6(a._1, a._2, a._3, a._4, a._5, a._6)
  case class ApplicableArbitrary6[T1, T2, T3, T4, T5, T6](a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4], a5: Arbitrary[T5], a6: Arbitrary[T6]) {
    def apply[R](f: (T1, T2, T3, T4, T5, T6) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2], s3: Shrink[T3], s4: Shrink[T4], s5: Shrink[T5], s6: Shrink[T6]) = check(f)(toProp, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6)
  }
  implicit def applicableArbitrary7[T1, T2, T3, T4, T5, T6, T7](a: (Arbitrary[T1], Arbitrary[T2], Arbitrary[T3], Arbitrary[T4], Arbitrary[T5], Arbitrary[T6], Arbitrary[T7])) = ApplicableArbitrary7(a._1, a._2, a._3, a._4, a._5, a._6, a._7)
  case class ApplicableArbitrary7[T1, T2, T3, T4, T5, T6, T7](a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4], a5: Arbitrary[T5], a6: Arbitrary[T6], a7: Arbitrary[T7]) {
    def apply[R](f: (T1, T2, T3, T4, T5, T6, T7) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2], s3: Shrink[T3], s4: Shrink[T4], s5: Shrink[T5], s6: Shrink[T6], s7: Shrink[T7]) = check(f)(toProp, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6, a7, s7)
  }
  implicit def applicableArbitrary8[T1, T2, T3, T4, T5, T6, T7, T8](a: (Arbitrary[T1], Arbitrary[T2], Arbitrary[T3], Arbitrary[T4], Arbitrary[T5], Arbitrary[T6], Arbitrary[T7], Arbitrary[T8])) = ApplicableArbitrary8(a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8)
  case class ApplicableArbitrary8[T1, T2, T3, T4, T5, T6, T7, T8](a1: Arbitrary[T1], a2: Arbitrary[T2], a3: Arbitrary[T3], a4: Arbitrary[T4], a5: Arbitrary[T5], a6: Arbitrary[T6], a7: Arbitrary[T7], a8: Arbitrary[T8]) {
    def apply[R](f: (T1, T2, T3, T4, T5, T6, T7, T8) => R)(implicit toProp: R => Prop, s1: Shrink[T1], s2: Shrink[T2], s3: Shrink[T3], s4: Shrink[T4], s5: Shrink[T5], s6: Shrink[T6], s7: Shrink[T7], s8: Shrink[T8]) = check(f)(toProp, a1, s1, a2, s2, a3, s3, a4, s4, a5, s5, a6, s6, a7, s7, a8, s8)
  }

}
/**
 * This trait is used to facilitate testing by mocking ScalaCheck functionalities
 */
trait ScalaCheckFunctions {
  def checkProp(params: Params, prop: =>Prop, callback: Test.TestCallback) = Test.check(params.copy(testCallback = callback), prop)
}
/**
 * This trait provides generation parameters to use with the <code>ScalaCheckMatchers</code>
 */
trait ScalaCheckParameters {
  /**
   * Values which can be used as Symbol aliases to specify ScalaCheck parameters<br>
   * The naming is a bit different, in order to keep short names for frequent use cases<ul>
   *  <code><li>minTestsOk == minSuccessfulTests
   *  <li>maxDiscarded == maxDiscardedTests
   *  <li>minSize and maxSize keep their name <code><ul>
   */
  val (minSize, maxSize, maxDiscarded, minTestsOk, workers) = ('minSize, 'maxSize, 'maxDiscarded, 'minTestsOk, 'workers)
   /**
    * default parameters. Uses ScalaCheck default values and doesn't print anything to the console
    */
   implicit def defaultParameters = new Parameters(setParams(Nil))

   /** default parameters to display pretty messages */		   
   val defaultPrettyParams = Pretty.defaultParams
   /**
    * Default values for ScalaCheck parameters
    */
   def defaultValues = Map(minTestsOk->100, maxDiscarded ->500, minSize->0, maxSize->100, workers->1)

   /** factory object to create parameters with verbose = false */
   object set extends Parameters(setParams(Nil)) {
     def apply(p: (Symbol, Int)*) = new Parameters(setParams(p))
   }
   /** factory object to create parameters with verbose = true */
   object display  extends Parameters(setParams(Nil)) {
     def apply(p: (Symbol, Int)*) = new Parameters(setParams(p)) { override def verbose = true }
     override def verbose = true
   }
   private def setParams(p: Seq[(Symbol, Int)]): Map[Symbol, Int] = {
     p.foldLeft(defaultValues) { (res: Map[Symbol, Int], pair: (Symbol, Int)) =>
       //  this is a useful check in case of print(null) or set(null)
       if (pair == null || pair._1 == null)
         throw new RuntimeException("null values are not accepted in scalacheck parameters: '"+pair+"'")
       res updated (pair._1, pair._2)
     }
  }
}
/**
 * This class is the base class for the display and set case classes.<br>
 * It contains a Map of generation parameters and indicates if the generation
 * must be verbose.
 */
case class Parameters(params: Map[Symbol, Int]) {
  def apply(s: Symbol) = params(s)
  def verbose = false
}