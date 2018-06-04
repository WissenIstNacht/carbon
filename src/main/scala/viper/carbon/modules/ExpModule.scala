/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package viper.carbon.modules

import viper.silver.{ast => sil}
import viper.silver.ast.utility.Expressions.{whenExhaling, whenInhaling}
import viper.carbon.boogie.{Exp, LocalVar, Stmt, TrueLit}
import viper.carbon.modules.components.{ComponentRegistry, DefinednessComponent}
import viper.silver.verifier.PartialVerificationError

/**
 * A module for translating Viper expressions.
 */
trait ExpModule extends Module with ComponentRegistry[DefinednessComponent] {
  def translateExp(exp: sil.Exp): Exp

  def translateExpInWand(exp: sil.Exp, allStateAssms: Exp = TrueLit(), inWand: Boolean): Exp

  def translateLocalVar(l: sil.LocalVar): LocalVar

  /**
   * Free assumptions about an expression.
   */
  def allFreeAssumptions(e: sil.Exp): Stmt

  /**
   * Check definedness of Viper expressions as they occur in the program.
   *
   * makeChecks provides the possibility of switching off most checks, to get 
   * only the side-effects (unfoldings) of unravelling the expression. Note that
   * the parameter should be passed down through recursive calls (default true)
   */
  def checkDefinedness(e: sil.Exp, error: PartialVerificationError, makeChecks: Boolean = true,
                       allStateAssms: Exp = TrueLit(), inWand: Boolean = false, ignore: Boolean = false): Stmt

  /**
   * Check definedness of Viper assertions such as pre-/postconditions or invariants.
   * The implementation works by inhaling 'e' and checking the necessary properties
   * along the way.
   */
  def checkDefinednessOfSpecAndInhale(e: sil.Exp, error: PartialVerificationError, statesStack: List[Any] = null, allStateAssms: Exp = TrueLit(), inWand: Boolean = false): Stmt

  /**
   * Convert all InhaleExhale expressions to their exhale part and call checkDefinednessOfSpecAndInhale.
   */
  def checkDefinednessOfExhaleSpecAndInhale(expressions: Seq[sil.Exp],
                                            errorConstructor: (sil.Exp) => PartialVerificationError,
                                            statesStack: List[Any] = null, allStateAssms: Exp = TrueLit(), inWand: Boolean = false): Seq[Stmt] = {
    expressions map (e => {
      checkDefinednessOfSpecAndInhale(whenExhaling(e), errorConstructor(e))
    })
  }

  /**
   * Convert all InhaleExhale expressions to their inhale part and call checkDefinednessOfSpecAndInhale.
   */
  def checkDefinednessOfInhaleSpecAndInhale(expressions: Seq[sil.Exp],
                                            errorConstructor: (sil.Exp) => PartialVerificationError
                                           ,statesStack: List[Any] = null, allStateAssms: Exp = TrueLit(), inWand: Boolean = false): Seq[Stmt] = {
    expressions map (e => {
      checkDefinednessOfSpecAndInhale(whenInhaling(e), errorConstructor(e))
    })
  }

  /**
   * Check definedness of Viper assertions such as pre-/postconditions or invariants.
   * The implementation works by exhaling 'e' and checking the necessary properties
   * along the way.
   */
  def checkDefinednessOfSpecAndExhale(e: sil.Exp, definednessError: PartialVerificationError, exhaleError: PartialVerificationError,
                                      statesStack: List[Any] = null, allStateAssms: Exp = TrueLit(), inWand: Boolean = false): Stmt
}
