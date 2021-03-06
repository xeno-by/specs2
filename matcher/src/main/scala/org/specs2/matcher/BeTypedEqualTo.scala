package org.specs2
package matcher

import execute._
import text.Quote._
import text.NotNullStrings._
import text.Trim._
import scala.collection.GenTraversableOnce

/**
 * Typed equality Matcher
 */
class BeTypedEqualTo[T](t: =>T, equality: (T, T) => Boolean = (t1:T, t2:T) => t1 == t2) extends AdaptableMatcher[T] {
  outer =>

  import AnyMatchers._

  protected val ok: String => String = identity
  protected val ko: String => String = identity

  def adapt(f: T => T, okFunction: String => String, koFunction: String => String) = {
    new BeTypedEqualTo(f(t), equality) {
      override def apply[S <: T](s: Expectable[S]): MatchResult[S] = {
        val originalValues = s"\nOriginal values\n  Expected: '$t'\n  Actual  : '${s.value}'"
        result(super.apply(s.map(f)).updateMessage(_ + originalValues), s)
      }

      override protected val ok: String => String = okFunction compose outer.ok
      override protected val ko: String => String = koFunction compose outer.ko
    }
  }

  /**
   * we perform 2 kinds of check, depending on the elements to compare
   *
   *  - unordered sequences (maps, sets) are being compared with a matching algorithm
   *  - arrays are being compared with the deep equality and a matching algorithm is used for missing elements
   *  - sequences are being compared with the regular equality and a matching algorithm is used for missing elements
   *  - other objects are being compared using the regular equality
   *
   * @return a MatchResult describing the outcome of the match
   */
  def apply[S <: T](b: Expectable[S]): MatchResult[S] = {
    val (actual, expected) = (b.value, t)

    (actual, expected) match {
      case (e1: Map[_,_], e2: Map[_,_])                           => unorderedSeqEquality(e1.toSeq, e2.toSeq, b, expected)
      case (e1: Set[_],   e2: Set[_])                             => unorderedSeqEquality(e1.toSeq, e2.toSeq, b, expected)
      case (e1: Array[_], e2: Array[_])                           => arrayEquality(e1, e2, b, expected)

      case (e1: GenTraversableOnce[_], e2: GenTraversableOnce[_]) if foreachIsDefined(e2) =>
        traversableEquality(e1.seq.toSeq, e2.seq.toSeq, b, expected)
      case (e1: GenTraversableOnce[_], e2: GenTraversableOnce[_]) =>
        untraversableEquality(e1, b, expected)

      case other                                                  => otherEquality(expected, b)
    }
  }

  private def unorderedSeqEquality[S <: T](actualSeq: Seq[Any], expectedSeq: Seq[Any], expectable: Expectable[S], expectedValue: Any): MatchResult[S] = {
    val (isEqual, missing) = missingElements(actualSeq, expectedSeq)
    val (haveDifferentClasses, qa, db) = describe(expectedValue, expectable, isEqual)
    val additionalInfo = if (haveDifferentClasses) "" else missing

    result(isEqual, ok(print(qa, " is equal to ", db)), ko(print(qa, " is not equal to ", db) ++ additionalInfo), expectable)
  }

  private def arrayEquality[S <: T](actual: Array[_], expected: Array[_], expectable: Expectable[S], expectedValue: Any): MatchResult[S] = {
    val isEqual = actual.deep == expected.deep
    val (haveDifferentClasses, qa, db) = describe(expected, expectable, isEqual)
    val missing = if (isEqual || haveDifferentClasses) "" else missingElements(actual, expected)._2

    result(isEqual, ok(print(qa, " is equal to ", db)), ko(print(qa, " is not equal to ", db) ++ missing), expectable)
  }

  private def traversableEquality[S <: T](actualSeq: Seq[Any], expectedSeq: Seq[Any], expectable: Expectable[S], expectedValue: Any): MatchResult[S] = {
    val isEqual = actualSeq == expectedSeq
    val (haveDifferentClasses, qa, db) = describe(expectedValue, expectable, isEqual)
    val missing = if (isEqual || haveDifferentClasses) "" else missingElements(actualSeq, expectedSeq)._2

    result(isEqual, ok(print(qa, " is equal to ", db)), ko(print(qa, " is not equal to ", db) ++ missing), expectable)
  }

  private def untraversableEquality[S <: T](actualSeq: GenTraversableOnce[_], expectable: Expectable[S], expectedValue: Any): MatchResult[S] = {
    val isEqual = actualSeq == expectedValue
    val (haveDifferentClasses, qa, db) = describe(expectedValue, expectable, isEqual)

    result(isEqual, ok(print(qa, " is equal to ", db)), ko(print(qa, " is not equal to ", db)), expectable)
  }

  /** @return true if foreach is not defined on a collection */
  private def foreachIsDefined(seq: GenTraversableOnce[_]) =
    try { seq.foreach(identity); true }
    catch { case _: Exception => false }

  private def otherEquality[S <: T](expected: T, b: Expectable[S]): MatchResult[S] = {
    val actual = b.value

    def isEqual = equality(actual, expected)
    lazy val (haveDifferentClasses, qa, db) = describe(expected, b, isEqual)
    result(isEqual, ok(print(qa, " is equal to ", db)), ko(print(qa, " is not equal to ", db)), b, expected.notNull, actual.notNull)
  }

  /**
   * @return a description of the (same string representation, actual value, expected value)
   */
  private def describe(expected: Any, b: Expectable[_], isEqual: Boolean): (Boolean, String, String) = {
    val actual = b.value

    (b.description, q(expected)) match {
      case (x, y) if !isEqual && x == y =>
        val (actualWithClass, expectedWithClass) = (actual.notNullWithClass, expected.notNullWithClass)
        if (actualWithClass == expectedWithClass) (true, b.describe(actual.notNullWithClass(showAll = true)), q(expected.notNullWithClass(showAll = true)))
        else (true, b.describe(actualWithClass), q(expectedWithClass))

      case (x, y) => (false, x, y)
    }
  }

  // print actual and expected values and a message relating them
  private def print(b: String, msg: String, a: String): String =
    Seq(b, msg, a).mkString("\n\n".unless(Seq(a, b).forall(_.size <= 40)))

  private def missingElements(actual: Seq[Any], expected: Seq[Any]): (Boolean, String) = {
    val (matched, missingFromActual) = BestMatching.findBestMatch(actual, expected, (t: Any, v: Any) => ===(t).apply(Expectable(v)), eachCheck = true)
    val (okValues, koValues)         = matched.partition(_._3.isSuccess)
    val missingFromExpected          = koValues.map(_._1)
    val isEqual                      = missingFromActual.isEmpty && missingFromExpected.isEmpty
    // display pairs nicely
    val display = (a: Any) => a match {
      case (k, v) => s"$k -> $v"
      case _      => a.notNull
    }

    val missings = if (isEqual) "" else
      (if (missingFromActual.nonEmpty) "\n\nMissing values"+missingFromActual.map(display).mkString("\n", "\n", "\n") else "") ++
        (if (missingFromExpected.nonEmpty) "\nAdditional values"+missingFromExpected.map(display).mkString("\n", "\n", "\n\n") else "")

    (isEqual, missings)
  }

  def expected = t
}

