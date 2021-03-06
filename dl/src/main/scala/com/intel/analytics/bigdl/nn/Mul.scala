/*
 * Licensed to Intel Corporation under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Intel Corporation licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.analytics.bigdl.nn

import com.intel.analytics.bigdl.nn.abstractnn.TensorModule
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric
import com.intel.analytics.bigdl.utils.RandomGenerator._

import scala.reflect.ClassTag

/**
 * multiply a single scalar factor to the incoming data
 */

@SerialVersionUID(7706562484586989118L)
class Mul[T: ClassTag](implicit ev: TensorNumeric[T]) extends TensorModule[T] {

  val weight = Tensor[T](1)
  val gradWeight = Tensor[T](1)

  reset()

  override def reset(): Unit = {
    // stdv should be 1 / math.sqrt(weight.size(1)), as weight's size(1) is 1, so stdv is 1.0
    val stdv = 1.0
    weight.apply1(_ => ev.fromType[Double](RNG.uniform(-stdv, stdv)))
    zeroGradParameters()
  }

  override def updateOutput(input: Tensor[T]): Tensor[T] = {
    output.resizeAs(input).copy(input)
    output.mul(weight(Array(1)))
    output
  }

  override def updateGradInput(input: Tensor[T], gradOutput: Tensor[T]): Tensor[T] = {
    gradInput.resizeAs(input).zero()
    gradInput.add(weight(Array(1)), gradOutput)
    gradInput
  }


  override def accGradParameters(input: Tensor[T], gradOutput: Tensor[T],
  scale: Double = 1.0): Unit = {
    gradWeight.add(ev.times(input.dot(gradOutput), ev.fromType(scale)))
  }

  override def zeroGradParameters(): Unit = {
    gradWeight.zero()
  }

  override def parameters(): (Array[Tensor[T]], Array[Tensor[T]]) = {
    (Array(this.weight), Array(this.gradWeight))
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[Mul[T]]

  override def equals(other: Any): Boolean = other match {
    case that: Mul[T] =>
      super.equals(that) &&
        (that canEqual this) &&
        weight == that.weight &&
        gradWeight == that.gradWeight
    case _ => false
  }

  override def hashCode(): Int = {
    def getHashCode(a: Any): Int = if (a == null) 0 else a.hashCode()
    val state = Seq(super.hashCode(), weight, gradWeight)
    state.map(getHashCode).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString(): String = {
    s"nn.Mul"
  }
}

object Mul {
  def apply[@specialized(Float, Double) T: ClassTag]()
      (implicit ev: TensorNumeric[T]) : Mul[T] = {
    new Mul[T]()
  }
}
